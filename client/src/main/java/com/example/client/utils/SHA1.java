package com.example.client.utils;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Утилитный класс для генерации SHA-1 хэша из строки.
 * <p>
 * Предоставляет статический метод {@link #hash(String)} для вычисления
 * SHA-1 дайджеста входной строки в шестнадцатеричном (hex) представлении.
 * </p>
 */
public class SHA1 {
    /**
     * Вычисляет SHA-1 хэш заданной строки.
     *
     * @param input строка, которую необходимо хэшировать, не должна быть null
     * @return строковое представление SHA-1 хэша в формате hex (без префикса)
     * @throws RuntimeException если алгоритм SHA-1 недоступен в текущей среде выполнения
     */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-1");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            StringBuilder hex = new StringBuilder();
            for (byte b : hashBytes) {
                hex.append(String.format("%02x", b));
            }

            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-1 не найден", e);
        }
    }
}