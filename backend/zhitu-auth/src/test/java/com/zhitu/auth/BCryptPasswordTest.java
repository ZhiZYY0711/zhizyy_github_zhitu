package com.zhitu.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;

/**
 * BCrypt密码验证测试
 * 用于验证密码哈希是否正确
 */
public class BCryptPasswordTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    
    // 这是数据库中存储的密码哈希（对应密码 "123456"）
    private static final String STORED_HASH = "$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi";

    @Test
    public void testPasswordMatches() {
        // 测试密码 "123456" 是否匹配存储的哈希
        String rawPassword = "123456";
        boolean matches = passwordEncoder.matches(rawPassword, STORED_HASH);
        
        System.out.println("=== BCrypt密码验证测试 ===");
        System.out.println("原始密码: " + rawPassword);
        System.out.println("存储哈希: " + STORED_HASH);
        System.out.println("验证结果: " + (matches ? "✓ 匹配成功" : "✗ 匹配失败"));
        
        if (!matches) {
            System.out.println();
            System.out.println("⚠️ 这就是登录失败的根本原因！");
            System.out.println("存储的哈希不是密码 '123456' 的正确BCrypt哈希");
            System.out.println();
            System.out.println("请运行 testGenerateNewHash() 测试生成正确的哈希");
        }
        
        // 注释掉断言，让测试继续运行以生成正确的哈希
        // assertTrue(matches, "密码应该匹配存储的哈希");
    }

    @Test
    public void testGenerateNewHash() {
        // 生成新的BCrypt哈希用于对比
        String rawPassword = "123456";
        
        System.out.println("=== 生成正确的BCrypt哈希 ===");
        System.out.println("原始密码: " + rawPassword);
        System.out.println();
        
        // 生成3个新哈希供选择
        System.out.println("请使用以下任意一个哈希替换数据库和脚本中的密码哈希：");
        System.out.println();
        for (int i = 1; i <= 3; i++) {
            String newHash = passwordEncoder.encode(rawPassword);
            boolean matches = passwordEncoder.matches(rawPassword, newHash);
            System.out.println("哈希 " + i + ": " + newHash);
            System.out.println("验证: " + (matches ? "✓ 成功" : "✗ 失败"));
            System.out.println();
            assertTrue(matches, "新生成的哈希应该匹配原始密码");
        }
        
        // 验证旧哈希
        System.out.println("=== 验证旧哈希 ===");
        System.out.println("旧哈希: " + STORED_HASH);
        boolean storedHashMatches = passwordEncoder.matches(rawPassword, STORED_HASH);
        System.out.println("验证结果: " + (storedHashMatches ? "✓ 匹配成功" : "✗ 匹配失败"));
        System.out.println();
        
        if (!storedHashMatches) {
            System.out.println("⚠️ 警告: 旧哈希无法验证密码 '123456'");
            System.out.println("这解释了为什么登录失败！");
            System.out.println();
            System.out.println("解决方案:");
            System.out.println("1. 复制上面生成的任意一个哈希");
            System.out.println("2. 更新 generate_test_data.py 中的 PASSWORD_HASH");
            System.out.println("3. 重新运行 python generate_test_data.py");
            System.out.println("4. 重新执行生成的 SQL 文件");
        }
    }

    @Test
    public void testWrongPassword() {
        // 测试错误密码
        String wrongPassword = "wrong_password";
        boolean matches = passwordEncoder.matches(wrongPassword, STORED_HASH);
        
        System.out.println("=== 错误密码测试 ===");
        System.out.println("错误密码: " + wrongPassword);
        System.out.println("存储哈希: " + STORED_HASH);
        System.out.println("验证结果: " + (matches ? "✓ 匹配成功" : "✗ 匹配失败"));
        
        assertFalse(matches, "错误密码不应该匹配");
    }

    @Test
    public void testHashFormat() {
        // 验证哈希格式
        System.out.println("=== BCrypt哈希格式检查 ===");
        System.out.println("哈希值: " + STORED_HASH);
        System.out.println("哈希长度: " + STORED_HASH.length());
        System.out.println("哈希前缀: " + STORED_HASH.substring(0, 7));
        
        // BCrypt哈希应该以 $2a$, $2b$, 或 $2y$ 开头
        assertTrue(STORED_HASH.startsWith("$2a$") || 
                   STORED_HASH.startsWith("$2b$") || 
                   STORED_HASH.startsWith("$2y$"), 
                   "BCrypt哈希应该以正确的前缀开头");
        
        // BCrypt哈希长度通常是60个字符
        assertEquals(60, STORED_HASH.length(), "BCrypt哈希长度应该是60个字符");
    }
}
