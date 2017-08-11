package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Created by peterchen on 2017/8/10.
 */
@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;
    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String md5Password = MD5Util.MD5Encodeutf8(password);
        User user = userMapper.selectLogin(username,md5Password);
        if(user==null){
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登陆成功",user);
    }

    /**
     * 查看问题是否存在
     * @param username
     * @return
     */
    @Override
    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse<String> validResponse = this.checkVaild(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if(StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");
    }

    @Override
    public ServerResponse<String> forgetResetPassword(String username, String passwordNew, String forgetToken) {
        if(StringUtils.isBlank(forgetToken)){
            return ServerResponse.createByErrorMessage("参数错误，token需要传递");
        }
        ServerResponse validResponse = this.checkVaild(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);
        if(StringUtils.isBlank(token)){
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        if(StringUtils.equals(forgetToken,token)){
            String md5Password = MD5Util.MD5Encodeutf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,md5Password);
            if(rowCount>0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误，请重新获取");
        }

        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    /**
     * 检验问题答案是否正确
     * @param username
     * @param question
     * @param answer
     * @return
     */
    @Override
    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int resultCount = userMapper.checkAnswer(username,question,answer);
        if(resultCount>0){
            //说明问题及问题答案是这个用户的，并且是正确的
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    /**
     * 用户注册
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> register(User user) {
       ServerResponse validResponse = this.checkVaild(user.getUsername(),Const.USERNAME);
       if(!validResponse.isSuccess()){
           return validResponse;
       }
       validResponse = this.checkVaild(user.getEmail(),Const.EMAIL);
       if(!validResponse.isSuccess()){
           return validResponse;
       }

       user.setRole(Const.Role.ROLE_CUSTOMER);
       //md5加密
        user.setPassword(MD5Util.MD5Encodeutf8(user.getPassword()));
        int resultCount = userMapper.insert(user);
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    /**
     * 校验用户名或邮箱是否存在
     * @param str
     * @param type
     * @return
     */
    @Override
    public ServerResponse<String> checkVaild(String str, String type) {
        if(StringUtils.isNotBlank(type)){
            //开始校验
            if(Const.USERNAME.equals(type)){
                int resultCount = userMapper.checkUsername(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){
                int resultCount = userMapper.checkEmail(str);
                if(resultCount>0){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    /**
     * 登陆状态下的重置密码
     * @param passwordOld
     * @param passwordNew
     * @param user
     * @return
     */
    @Override
    public ServerResponse<String> resetPassword(String passwordOld, String passwordNew, User user) {
        //防止横向越权，要检验一下这个用户的旧密码，一定要指定是这个用户，因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0
        int resultCount = userMapper.checkPassword(MD5Util.MD5Encodeutf8(passwordOld),user.getId());
        if(resultCount==0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5Encodeutf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);
        if(updateCount>0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    @Override
    public ServerResponse<User> updateInfomation(User user) {
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount>0){
            return ServerResponse.createByErrorMessage("email已经存在，请更换email再尝试更新");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);
        if(updateCount>0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    @Override
    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if(user == null){
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }
}
