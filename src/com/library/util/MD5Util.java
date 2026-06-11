package com.library.util;

import java.security.MessageDigest;

/**
 * 密码加密工具类。
 * <p>
 * 登录密码不以明文保存，统一使用 MD5(明文 + 固定盐值) 的方式存储，
 * 盐值 {@link #SALT} 必须与数据库初始化脚本 db/library.sql 中的 @salt 保持一致，
 * 否则会导致用脚本写入的演示账号无法登录。
 */
public final class MD5Util {

    /** 加密盐值，与 SQL 脚本中的 @salt 一致 */
    public static final String SALT = "lib@2024";

    private MD5Util() {
    }

    /**
     * 对明文密码加盐后做 MD5，返回 32 位小写十六进制字符串。
     * @param plain 明文密码
     * @return 加密后的密码
     */
    public static String encrypt(String plain) {
        if (plain == null) {
            plain = "";
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest((plain + SALT).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                // 每个字节转成两位十六进制，不足两位前补 0
                String hex = Integer.toHexString(b & 0xFF);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("MD5 加密失败", e);
        }
    }
}
