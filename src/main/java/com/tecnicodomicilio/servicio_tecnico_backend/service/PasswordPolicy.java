package com.tecnicodomicilio.servicio_tecnico_backend.service;

public final class PasswordPolicy {

    private PasswordPolicy() {}

    public static void validar(String password) {
        if (password == null || password.length() < 8) {
            throw new RuntimeException("La contraseña debe tener al menos 8 caracteres");
        }
        if (password.length() > 64) {
            throw new RuntimeException("La contraseña no puede pasar de 64 caracteres");
        }
        if (password.chars().anyMatch(Character::isWhitespace)) {
            throw new RuntimeException("La contraseña no puede tener espacios");
        }
        if (password.chars().noneMatch(Character::isUpperCase)) {
            throw new RuntimeException("La contraseña debe incluir una letra mayúscula");
        }
        if (password.chars().noneMatch(Character::isLowerCase)) {
            throw new RuntimeException("La contraseña debe incluir una letra minúscula");
        }
        if (password.chars().noneMatch(Character::isDigit)) {
            throw new RuntimeException("La contraseña debe incluir un número");
        }
    }
}