package com.example.aiend.common.util;

import com.example.aiend.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;

/**
 * 代理令牌工具类
 * 用于生成和解析代理模式的 ProxyToken
 * 
 * ProxyToken 格式: proxy.{base64编码的payload}.{签名}
 * Payload 包含: userId(被代理人ID)、realUserId(实际操作人ID)、isProxy标识、过期时间
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Slf4j
public final class ProxyTokenUtil {
    
    /**
     * 代理令牌前缀
     */
    private static final String PROXY_TOKEN_PREFIX = "proxy.";
    
    /**
     * 令牌有效期：2小时（毫秒）
     */
    private static final long TOKEN_EXPIRE_MILLIS = 2 * 60 * 60 * 1000L;
    
    /**
     * 令牌外部分隔符（分隔 prefix、payload、signature）
     * 使用 . 避免与 Base64 URL 编码中的 - 冲突
     */
    private static final String TOKEN_DELIMITER = ".";
    
    /**
     * Payload 内部分隔符（分隔 userId、realUserId 等字段）
     */
    private static final String PAYLOAD_DELIMITER = "-";
    
    /**
     * 私有构造方法，防止实例化
     */
    private ProxyTokenUtil() {
        throw new IllegalStateException("工具类不允许实例化");
    }
    
    /**
     * 生成代理令牌
     * 
     * @param userId 被代理人ID（老人ID）- 执行操作时使用此ID
     * @param realUserId 实际操作人ID（子女ID）- 用于审计日志
     * @return 代理令牌
     */
    public static String generateProxyToken(Long userId, Long realUserId) {
        if (userId == null || realUserId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }
        
        // 计算过期时间
        long expireTime = System.currentTimeMillis() + TOKEN_EXPIRE_MILLIS;
        
        // 构建 payload: userId-realUserId-isProxy-expireTime
        String payload = userId + PAYLOAD_DELIMITER + realUserId + PAYLOAD_DELIMITER + "1" + PAYLOAD_DELIMITER + expireTime;
        
        // Base64 编码 payload
        String encodedPayload = Base64.getUrlEncoder().withoutPadding().encodeToString(payload.getBytes());
        
        // 生成简单签名（实际项目中应使用更安全的签名算法）
        String signature = generateSignature(payload);
        
        // 组装最终令牌: proxy.{encodedPayload}.{signature}
        String token = PROXY_TOKEN_PREFIX + encodedPayload + TOKEN_DELIMITER + signature;
        
        log.debug("生成代理令牌，userId：{}，realUserId：{}，过期时间：{}", userId, realUserId, expireTime);
        
        return token;
    }
    
    /**
     * 解析代理令牌
     * 
     * @param token 代理令牌
     * @return 解析结果，包含 userId 和 realUserId
     * @throws BusinessException 当令牌无效或已过期时抛出
     */
    public static ProxyTokenPayload parseProxyToken(String token) {
        if (token == null || token.isEmpty()) {
            throw new BusinessException(401, "代理令牌不能为空");
        }
        
        // 移除 Bearer 前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        // 检查是否是代理令牌
        if (!token.startsWith(PROXY_TOKEN_PREFIX)) {
            throw new BusinessException(401, "无效的代理令牌格式");
        }
        
        try {
            // 移除前缀
            String content = token.substring(PROXY_TOKEN_PREFIX.length());
            
            // 分离 payload 和签名（使用 . 分隔）
            int lastDelimiterIndex = content.lastIndexOf(TOKEN_DELIMITER);
            if (lastDelimiterIndex <= 0) {
                throw new BusinessException(401, "无效的代理令牌格式");
            }
            
            String encodedPayload = content.substring(0, lastDelimiterIndex);
            String signature = content.substring(lastDelimiterIndex + 1);
            
            // Base64 解码 payload
            String payload = new String(Base64.getUrlDecoder().decode(encodedPayload));
            
            // 验证签名
            String expectedSignature = generateSignature(payload);
            if (!signature.equals(expectedSignature)) {
                throw new BusinessException(401, "代理令牌签名验证失败");
            }
            
            // 解析 payload: userId-realUserId-isProxy-expireTime
            String[] parts = payload.split(PAYLOAD_DELIMITER);
            if (parts.length != 4) {
                throw new BusinessException(401, "无效的代理令牌内容");
            }
            
            Long userId = Long.parseLong(parts[0]);
            Long realUserId = Long.parseLong(parts[1]);
            boolean isProxy = "1".equals(parts[2]);
            long expireTime = Long.parseLong(parts[3]);
            
            // 检查是否是代理令牌
            if (!isProxy) {
                throw new BusinessException(401, "无效的代理令牌");
            }
            
            // 检查是否过期
            if (System.currentTimeMillis() > expireTime) {
                throw new BusinessException(401, "代理令牌已过期，请重新开启代理模式");
            }
            
            log.debug("解析代理令牌成功，userId：{}，realUserId：{}", userId, realUserId);
            
            return new ProxyTokenPayload(userId, realUserId, isProxy, expireTime);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.warn("解析代理令牌失败，token：{}，错误：{}", token, e.getMessage());
            throw new BusinessException(401, "无效的代理令牌");
        }
    }
    
    /**
     * 判断是否是代理令牌
     * 
     * @param token 令牌字符串
     * @return 如果是代理令牌返回 true，否则返回 false
     */
    public static boolean isProxyToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }
        
        // 移除 Bearer 前缀（如果有）
        if (token.startsWith("Bearer ")) {
            token = token.substring(7);
        }
        
        return token.startsWith(PROXY_TOKEN_PREFIX);
    }
    
    /**
     * 生成签名
     * 简单实现，实际项目中应使用 HMAC-SHA256 等更安全的算法
     * 
     * @param payload 待签名内容
     * @return 签名字符串
     */
    private static String generateSignature(String payload) {
        // 使用简单的哈希实现，实际项目中应使用密钥进行 HMAC 签名
        int hash = payload.hashCode();
        // 转换为正数并取模，确保签名长度一致
        return String.valueOf(Math.abs(hash) % 10000000000L);
    }
    
    /**
     * 代理令牌载荷类
     * 封装从令牌中解析出的信息
     */
    public static class ProxyTokenPayload {
        
        /**
         * 被代理人ID（老人ID）- 执行操作时使用此ID
         */
        private final Long userId;
        
        /**
         * 实际操作人ID（子女ID）- 用于审计日志
         */
        private final Long realUserId;
        
        /**
         * 是否是代理模式
         */
        private final boolean isProxy;
        
        /**
         * 过期时间（毫秒时间戳）
         */
        private final long expireTime;
        
        public ProxyTokenPayload(Long userId, Long realUserId, boolean isProxy, long expireTime) {
            this.userId = userId;
            this.realUserId = realUserId;
            this.isProxy = isProxy;
            this.expireTime = expireTime;
        }
        
        public Long getUserId() {
            return userId;
        }
        
        public Long getRealUserId() {
            return realUserId;
        }
        
        public boolean isProxy() {
            return isProxy;
        }
        
        public long getExpireTime() {
            return expireTime;
        }
        
        @Override
        public String toString() {
            return "ProxyTokenPayload{" +
                    "userId=" + userId +
                    ", realUserId=" + realUserId +
                    ", isProxy=" + isProxy +
                    ", expireTime=" + expireTime +
                    '}';
        }
    }
}
