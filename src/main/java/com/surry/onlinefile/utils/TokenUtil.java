package com.surry.onlinefile.utils;

import io.jsonwebtoken.*;

public class TokenUtil {

    /**
     * 密钥
     */
    private static final String TOKEN_ENCRY_KEY = "MDk4ZjjZiY20NjIxDM3kM2NhZU0ZTgzjMjYyN2I0ZjY";

    /**
     * 取token方法 :userId 是要存到token的用户信息，如有需要可以添加更多
     * @param userId
     * @return
     */
    public static String getToken(Long userId) {
//        long currentTime = System.currentTimeMillis();
        //头信息
        return Jwts.builder().setHeaderParam("typ", "JWT")
                .setHeaderParam("alg", "HS256")
                //载荷
                .claim("userId", userId)
//                //过期时间戳
//                .setExpiration(new Date(currentTime + TOKEN_TIME_OUT * 1000))
//                //jwt编号:随机产生
//                .setId(UUID.randomUUID().toString())
                //签名
                .signWith(SignatureAlgorithm.HS256, TOKEN_ENCRY_KEY) //加密方式
                .compact();
    }

    /**
     * 获取token中的claims信息
     * @param token
     * @return
     */
    private static Jws<Claims> getJws(String token) {
        return Jwts.parser()
                .setSigningKey(TOKEN_ENCRY_KEY)
                .parseClaimsJws(token);
    }

    /**
     * 获取payload body信息(指的是tocken中Payload部分)
     * @param token
     * @return Claims 是Map
     */
    public static Claims getClaims(String token) {
        try {
            return getJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return null;
        }
    }


    /**
     * 检查token
     * 1. 检查token的完整性和有效期
     * 2. 检查失败会报错
     * 3. 检查成功返回tocken的playload内容
     */
    public static Claims checkToken(String token) {
        try {
            Claims claims = getClaims(token);
            if (claims == null) {
                throw new RuntimeException("token解析失败");
            }
            return claims;
        } catch (ExpiredJwtException ex) {
            throw new RuntimeException("token已经失效");
        } catch (Exception e) {
            throw new RuntimeException("token解析失败");
        }
    }

}
