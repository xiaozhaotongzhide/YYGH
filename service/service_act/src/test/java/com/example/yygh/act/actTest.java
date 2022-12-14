package com.example.yygh.act;

import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.IOException;

public class actTest {
    public static void main(String[] args) throws IOException {
        ClassPathResource classPathResource = new ClassPathResource("luaFile/unlock.lua");
        System.out.println(classPathResource.isFile());
//        System.out.println(classPathResource.getFile());
        File file = new File("luaFile/unlock.lua");
        System.out.println(file.isFile());
    }
}
