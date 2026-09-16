package com.tutorsystem;

// 用于测试密文是否正确

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootTest
public class PasswordUtilsTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void testEncodeAndVerify() {
        // 原始明文密码
        String rawPassword = "tutor123";
        // 加密后的密码
        String encoded = passwordEncoder.encode(rawPassword);

        System.out.println("明文: " + rawPassword);
        System.out.println("密文: " + encoded);

        // 验证正确的密码
        boolean matches = passwordEncoder.matches(rawPassword, encoded);
        System.out.println("验证结果: " + matches); // 应为 true

        // 测试错误密码
        boolean wrong = passwordEncoder.matches("wrong", encoded);
        System.out.println("错误密码验证: " + wrong); // 应为 false
    }
}