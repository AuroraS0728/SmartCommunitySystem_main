package com.smartcommunity.common;

public final class RoleUtils {
    public static final int ROLE_OWNER = 1;
    public static final int ROLE_PROPERTY_MANAGER = 2;
    public static final int ROLE_WORKER = 3;
    public static final int ROLE_ADMIN = 4;

    private RoleUtils() {
    }

    public static boolean isOwner(Integer role) {
        return role != null && role == ROLE_OWNER;
    }

    public static boolean isWorker(Integer role) {
        return role != null && role == ROLE_WORKER;
    }

    public static boolean isPropertyAdmin(Integer role) {
        return role != null && (role == ROLE_PROPERTY_MANAGER || role == ROLE_ADMIN);
    }
}
