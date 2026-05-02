package com.toiec.demo.enums;


public enum Quality {
    BLACK(0, "Hoàn toàn quên"),
    FORGET(1, "Sai"),
    HARD(2, "Khó"),
    GOOD(3, "Tốt"),
    EASY(4, "Dễ");

    public final int value;
    public final String description;

    Quality(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static Quality fromInt(int value) {
        for (Quality q : values()) {
            if (q.value == value) return q;
        }
        throw new IllegalArgumentException("Invalid quality: " + value);
    }
}