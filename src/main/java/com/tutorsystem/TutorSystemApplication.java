package com.tutorsystem;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.tutorsystem.mapper")
public class TutorSystemApplication {

    public static void main(String[] args) {
        SpringApplication.run(TutorSystemApplication.class, args);
    }
}
