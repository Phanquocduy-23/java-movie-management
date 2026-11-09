package vn.edu.nct.quanlyphim;

import java.util.Arrays;

import vn.edu.nct.quanlyphim.util.PasswordUtil;

public class PasswordCheck {

    public static void main(String[] args) {
        // Mat khau chi dung de kiem tra, khong phai tai khoan that.
        char[] correct = "DemoPassword_123!".toCharArray();
        char[] wrong = "WrongPassword_456!".toCharArray();
        char[] empty = new char[0];

        try {
            String firstHash = PasswordUtil.hash(correct);
            String secondHash = PasswordUtil.hash(correct);

            check(
                "Correct password",
                PasswordUtil.verify(correct, firstHash)
            );

            check(
                "Wrong password rejected",
                !PasswordUtil.verify(wrong, firstHash)
            );

            check(
                "Different random salts",
                !firstHash.equals(secondHash)
            );

            check(
                "Second hash also works",
                PasswordUtil.verify(correct, secondHash)
            );

            check(
                "Invalid stored hash rejected",
                !PasswordUtil.verify(correct, "invalid")
            );

            check(
                "Empty password rejected",
                !PasswordUtil.verify(empty, firstHash)
            );

            check(
                "Hash fits database column",
                firstHash.length() <= 255
            );

            System.out.println("ALL CHECKS PASSED");

        } finally {
            Arrays.fill(correct, '\0');
            Arrays.fill(wrong, '\0');
        }
    }

    private static void check(String name, boolean passed) {
        if (!passed) {
            throw new IllegalStateException("FAIL: " + name);
        }

        System.out.println("PASS: " + name);
    }
}