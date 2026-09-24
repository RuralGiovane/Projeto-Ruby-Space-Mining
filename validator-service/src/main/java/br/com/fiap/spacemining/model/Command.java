package br.com.fiap.spacemining.model;

public enum Command {
    RIGHT, LEFT, FRONT, BACK, OPEN, CLOSE;

    public static Command parse(String value) {
        try {
            return valueOf(value == null ? "" : value);
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Comando deve ser RIGHT, LEFT, FRONT, BACK, OPEN ou CLOSE.");
        }
    }
}
