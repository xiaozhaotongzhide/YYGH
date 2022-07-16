package com.example.yygh.oss.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.File;

public interface FileService {
    String uplodad(MultipartFile file);

    String uplodad(File file, String zipName);
}
