package com.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by peterchen on 2017/8/16.
 */
public interface IFileService {
    String upload(MultipartFile file,String path);
}
