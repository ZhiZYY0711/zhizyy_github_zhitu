package com.zhitu.auth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zhitu.auth.dto.LoginRequest;
import com.zhitu.auth.dto.LoginResponse;
import com.zhitu.auth.dto.RefreshTokenRequest;
import com.zhitu.auth.entity.SysRefreshToken;
import com.zhitu.auth.entity.SysTenant;
import com.zhitu.auth.entity.SysUser;
import com.zhitu.auth.mapper.SysRefreshTokenMapper;
import com.zhitu.auth.mapper.SysTenantMapper;
import com.zhitu.auth.mapper.SysUserMapper;
import com.zhitu.common.core.exception.BusinessException;
import com.zhitu.common.core.result.ResultCode;
import com.zhitu.common.redis.util.RedisUtils;
import com.zhitu.common.security.util.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.OffsetDateTime;
import java.util.HexFormat;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TOKEN_KEY_PREFIX = "token:access:";

    private final SysUserMapper userMapper;
    private final SysTenantMapper tenantMapper;
    private final SysRefreshTokenMapper refreshTokenMapper;
    private final JwtUtils jwtUtils;
    private final PasswordEncoder passwordEncoder;
    private final RedisUtils redisUtils;

    /**
     * 统一登录
     */
    @Transactional
    public LoginResponse login(LoginRequest req) {
        // 1. 查用户
        SysUser user = userMapper.selectOne(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, req.getUsername())
                .eq(SysUser::getRole, req.getRole()));

        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 校验状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 3. 校验密码
        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        // 4. 校验租户
        if (user.getTenantId() != 0) {
            SysTenant tenant = tenantMapper.selectById(user.getTenantId());
            if (tenant == null || tenant.getStatus() != 1) {
                throw new BusinessException(ResultCode.TENANT_DISABLED);
            }
        }

        // 5. 生成 token
        String accessToken = jwtUtils.generateAccessToken(
                user.getId(), user.getRole(), user.getSubRole(), user.getTenantId());
        String refreshToken = jwtUtils.generateRefreshToken(user.getId());

        // 6. 存 access_token 到 Redis（用于主动吊销）
        redisUtils.set(TOKEN_KEY_PREFIX + user.getId(), accessToken,
                jwtUtils.getAccessTokenExpiration(), TimeUnit.SECONDS);

        // 7. 存 refresh_token 哈希到数据库
        SysRefreshToken tokenRecord = new SysRefreshToken();
        tokenRecord.setUserId(user.getId());
        tokenRecord.setTokenHash(sha256(refreshToken));
        tokenRecord.setExpiresAt(OffsetDateTime.now()
                .plusSeconds(jwtUtils.getRefreshTokenExpiration()));
        tokenRecord.setCreatedAt(OffsetDateTime.now());
        refreshTokenMapper.insert(tokenRecord);

        // 8. 更新最后登录时间
        user.setLastLoginAt(OffsetDateTime.now());
        userMapper.updateById(user);

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .subRole(user.getSubRole())
                        .tenantId(user.getTenantId().toString())
                        .build())
                .build();
    }

    /**
     * 刷新 access_token
     */
    @Transactional
    public LoginResponse refresh(RefreshTokenRequest req) {
        String refreshToken = req.getRefreshToken();

        // 1. 验证 token 格式和签名
        if (!jwtUtils.isValid(refreshToken)) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        // 2. 查数据库验证哈希
        String hash = sha256(refreshToken);
        SysRefreshToken record = refreshTokenMapper.selectOne(
                new LambdaQueryWrapper<SysRefreshToken>()
                        .eq(SysRefreshToken::getTokenHash, hash));

        if (record == null || record.getExpiresAt().isBefore(OffsetDateTime.now())) {
            throw new BusinessException(ResultCode.REFRESH_TOKEN_INVALID);
        }

        // 3. 查用户
        SysUser user = userMapper.selectById(record.getUserId());
        if (user == null || user.getStatus() != 1) {
            throw new BusinessException(ResultCode.USER_DISABLED);
        }

        // 4. 生成新 access_token
        String newAccessToken = jwtUtils.generateAccessToken(
                user.getId(), user.getRole(), user.getSubRole(), user.getTenantId());

        // 5. 更新 Redis
        redisUtils.set(TOKEN_KEY_PREFIX + user.getId(), newAccessToken,
                jwtUtils.getAccessTokenExpiration(), TimeUnit.SECONDS);

        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getAccessTokenExpiration())
                .userInfo(LoginResponse.UserInfo.builder()
                        .id(user.getId().toString())
                        .username(user.getUsername())
                        .role(user.getRole())
                        .subRole(user.getSubRole())
                        .tenantId(user.getTenantId().toString())
                        .build())
                .build();
    }

    /**
     * 登出
     */
    @Transactional
    public void logout(Long userId, String refreshToken) {
        // 删除 Redis 中的 access_token
        redisUtils.delete(TOKEN_KEY_PREFIX + userId);

        // 删除数据库中的 refresh_token
        if (refreshToken != null) {
            refreshTokenMapper.delete(new LambdaQueryWrapper<SysRefreshToken>()
                    .eq(SysRefreshToken::getTokenHash, sha256(refreshToken)));
        }
    }

    private String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }
}
