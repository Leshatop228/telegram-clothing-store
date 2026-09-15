package com.labubu.telegramclothingstore.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class TelegramAuthService {

    private static final Pattern TELEGRAM_USER_ID_PATTERN =
            Pattern.compile("\"id\"\\s*:\\s*(\\d+)");

    private final String botToken;
    private final Set<Long> adminIds;

    public TelegramAuthService(
            @Value("${TELEGRAM_BOT_TOKEN}") String botToken,
            @Value("${TELEGRAM_ADMIN_IDS}") String adminIdsRaw
    ) {
        this.botToken = botToken;

        this.adminIds = Arrays.stream(adminIdsRaw.split(","))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public void requireAdmin(String initData) {
        Long telegramUserId = validateAndGetUserId(initData);

        if (!adminIds.contains(telegramUserId)) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Недостаточно прав для изменения каталога"
            );
        }
    }

    private Long validateAndGetUserId(String initData) {
        if (initData == null || initData.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Откройте админку внутри Telegram"
            );
        }

        Map<String, String> params = parseQuery(initData);
        String receivedHash = params.remove("hash");

        if (receivedHash == null || receivedHash.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "В Telegram initData отсутствует hash"
            );
        }

        String dataCheckString = params.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("\n"));

        byte[] secretKey = hmacSha256(
                "WebAppData".getBytes(StandardCharsets.UTF_8),
                botToken.getBytes(StandardCharsets.UTF_8)
        );

        byte[] calculatedHash = hmacSha256(
                secretKey,
                dataCheckString.getBytes(StandardCharsets.UTF_8)
        );

        if (!MessageDigest.isEqual(calculatedHash, hexToBytes(receivedHash))) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Подпись Telegram не прошла проверку"
            );
        }

        long authDate;
        try {
            authDate = Long.parseLong(params.getOrDefault("auth_date", "0"));
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Некорректная дата авторизации Telegram"
            );
        }

        long now = Instant.now().getEpochSecond();

        if (authDate <= 0 || now - authDate > 3600) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Авторизация истекла — закройте и заново откройте приложение"
            );
        }

        String userJson = params.get("user");

        if (userJson == null || userJson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Telegram не передал данные пользователя"
            );
        }

        Matcher matcher = TELEGRAM_USER_ID_PATTERN.matcher(userJson);

        if (!matcher.find()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Telegram не передал ID пользователя"
            );
        }

        try {
            return Long.parseLong(matcher.group(1));
        } catch (NumberFormatException exception) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Некорректный ID пользователя Telegram"
            );
        }
    }

    private Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> result = new LinkedHashMap<>();

        for (String pair : rawQuery.split("&")) {
            int separator = pair.indexOf('=');

            if (separator <= 0) {
                continue;
            }

            String key = URLDecoder.decode(
                    pair.substring(0, separator),
                    StandardCharsets.UTF_8
            );

            String value = URLDecoder.decode(
                    pair.substring(separator + 1),
                    StandardCharsets.UTF_8
            );

            result.put(key, value);
        }

        return result;
    }

    private byte[] hmacSha256(byte[] key, byte[] value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));

            return mac.doFinal(value);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Не удалось проверить подпись Telegram",
                    exception
            );
        }
    }

    private byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Некорректный hash Telegram"
            );
        }

        byte[] bytes = new byte[hex.length() / 2];

        for (int i = 0; i < hex.length(); i += 2) {
            int high = Character.digit(hex.charAt(i), 16);
            int low = Character.digit(hex.charAt(i + 1), 16);

            if (high < 0 || low < 0) {
                throw new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Некорректный hash Telegram"
                );
            }

            bytes[i / 2] = (byte) ((high << 4) + low);
        }

        return bytes;
    }
}
