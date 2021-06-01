package com.xpcf.mall.auth;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
class MallAuthServerApplicationTests {

    public static void main(String[] args) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        System.out.println(passwordEncoder.matches("123456", "$2a$10$P73AEEca3M4VafdYb76TJOuWWDY7kTbKy3rDDG24npag6WWGuliYK"));


    }
}
