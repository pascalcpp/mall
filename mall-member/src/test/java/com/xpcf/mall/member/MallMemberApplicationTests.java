package com.xpcf.mall.member;

import org.apache.commons.codec.digest.Md5Crypt;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@SpringBootTest
class MallMemberApplicationTests {


    public static void main(String[] args) {
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String cryptPassword = "$2a$10$ydw9L6IuaIQyvQaTm0McdeYCoMOYQgP9JBneF2IPu8RS.0Yy6huVS";
//        $2a$10$ydw9L6IuaIQyvQaTm0McdeYCoMOYQgP9JBneF2IPu8RS.0Yy6huVS
//        $2a$10$ydw9L6IuaIQyvQaTm0McdeYCoMOYQgP9JBneF2IPu8RS.0Yy6huVS
//        $2a$10$ydw9L6IuaIQyvQaTm0McdeYCoMOYQgP9JBneF2IPu8RS.0Yy6huVS
        System.out.println(passwordEncoder.matches("123456", cryptPassword));
    }

}
