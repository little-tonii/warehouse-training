package com.training.warehouse.enumeric;

public enum Role {
    ADMIN(1),
    STAFF(2);

    private final long value;

    Role(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public static Role fromValue(long value) {
        for (Role r : Role.values()) {
            if (r.value == value) return r;
        }
        throw new IllegalArgumentException("Unknown Role value: " + value);
    }
}
