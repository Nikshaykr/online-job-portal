package com.jobportal.util;

public class ValidationUtils {

    // 1. Email validation
    public static boolean isValidEmail(String email){
        if (email == null) return false;

        // Regular Expression to write email
        // return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");

        // New updated regex to check for missingDomains and multiple dots after @
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*\\.[A-Za-z]{2,}$");
    }

    // 2. Password validation method to throw an exception is psw is wrong
    public static void isValidPassword(String password) {
        if (password == null || password.length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
    }

}
