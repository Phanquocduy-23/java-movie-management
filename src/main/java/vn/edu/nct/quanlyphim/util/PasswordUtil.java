package vn.edu.nct.quanlyphim.util;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PasswordUtil {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final String FORMAT = "pbkdf2_sha256";

    private static final int ITERATIONS = 600_000;
    private static final int SALT_BYTES = 16;
    private static final int HASH_BYTES = 32;
    private static final int MAX_PASSWORD_LENGTH = 1024;

    private static final SecureRandom RANDOM = new SecureRandom();

    private PasswordUtil() {
    }

    public static String hash(char[] password) {
        if (password == null
                || password.length == 0
                || password.length > MAX_PASSWORD_LENGTH) {
            throw new IllegalArgumentException(
                "Độ dài mật khẩu không hợp lệ."
            );
        }

        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);

        byte[] derivedHash = derive(password, salt, ITERATIONS);

        try {
            return FORMAT + "$" + ITERATIONS + "$"
                + Base64.getEncoder().encodeToString(salt) + "$"
                + Base64.getEncoder().encodeToString(derivedHash);
        } finally {
            Arrays.fill(derivedHash, (byte) 0);
        }
    }

    public static boolean verify(char[] password, String storedHash) {
        if (password == null
                || password.length == 0
                || password.length > MAX_PASSWORD_LENGTH
                || storedHash == null
                || storedHash.length() > 255) {
            return false;
        }

        String[] parts = storedHash.split("\\$", -1);

        if (parts.length != 4 || !FORMAT.equals(parts[0])) {
            return false;
        }

        int iterations;
        byte[] salt;
        byte[] expectedHash;

        try {
            iterations = Integer.parseInt(parts[1]);

            if (iterations < ITERATIONS || iterations > 2_000_000) {
                return false;
            }

            salt = Base64.getDecoder().decode(parts[2]);
            expectedHash = Base64.getDecoder().decode(parts[3]);

        } catch (IllegalArgumentException e) {
            return false;
        }

        if (salt.length != SALT_BYTES
                || expectedHash.length != HASH_BYTES) {
            return false;
        }

        byte[] actualHash = derive(password, salt, iterations);

        try {
            return MessageDigest.isEqual(expectedHash, actualHash);
        } finally {
            Arrays.fill(actualHash, (byte) 0);
            Arrays.fill(expectedHash, (byte) 0);
        }
    }

    private static byte[] derive(
            char[] password,
            byte[] salt,
            int iterations) {

        PBEKeySpec specification = new PBEKeySpec(
            password,
            salt,
            iterations,
            HASH_BYTES * 8
        );

        try {
            SecretKeyFactory factory =
                SecretKeyFactory.getInstance(ALGORITHM);

            return factory.generateSecret(specification).getEncoded();

        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(
                "Không thực hiện được xử lý mật khẩu.",
                e
            );

        } finally {
            specification.clearPassword();
        }
    }
}