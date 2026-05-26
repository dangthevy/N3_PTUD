package com.service;

public final class SmtpConfig {
    private SmtpConfig() {
    }

    // Ưu tiên biến môi trường / JVM property để tránh hard-code khi deploy.
    private static final String DEFAULT_EMAIL = "duongsatvietnam2026@gmail.com";
    private static final String DEFAULT_APP_PASSWORD = "affyytsgobcztfzn";

    public static String getEmail() {
        String fromEnv = firstNonBlank(
                System.getenv("SMTP_EMAIL"),
                System.getProperty("smtp.email"),
                DEFAULT_EMAIL
        );
        return fromEnv.trim();
    }

    public static String getAppPassword() {
        String raw = firstNonBlank(
                System.getenv("SMTP_APP_PASSWORD"),
                System.getProperty("smtp.app.password"),
                DEFAULT_APP_PASSWORD
        );
        // Gmail app password có thể copy kèm khoảng trắng theo block 4 ký tự.
        return raw.replace(" ", "").trim();
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.trim().isEmpty()) {
                return value;
            }
        }
        return "";
    }
}
