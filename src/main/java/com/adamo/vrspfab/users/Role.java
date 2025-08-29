package com.adamo.vrspfab.users;


public enum Role {
    ADMIN ("Admin"),
    CUSTOMER ("Customer"),
    GUEST ("Guest");

    private final String displayName;

    Role(String displayName) {
        this.displayName = displayName;
    }

    public String getName() {
        return displayName;
    }
}
