package com.mmall.service;

import com.mmall.common.ServerResponse;

/**
 * Created by peterchen on 2017/8/20.
 */
public interface IOrderService {

    ServerResponse pay(Long orderNo,Integer userId,String path);
}
