package com.omra.platform.util;

import com.omra.platform.entity.enums.UserRole;

/**
 * Thread-local context for current request: agency id and user role.
 * Used for multi-tenant filtering and RBAC.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> AGENCY_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<UserRole> USER_ROLE = new ThreadLocal<>();

    private TenantContext() {}

    public static void setAgencyId(Long agencyId) {
        AGENCY_ID.set(agencyId);
    }

    public static Long getAgencyId() {
        return AGENCY_ID.get();
    }

    public static void setUserId(Long userId) {
        USER_ID.set(userId);
    }

    public static Long getUserId() {
        return USER_ID.get();
    }

    public static void setAdminId(Long adminId) {
        ADMIN_ID.set(adminId);
    }

    public static Long getAdminId() {
        return ADMIN_ID.get();
    }

    public static void setUserRole(UserRole role) {
        USER_ROLE.set(role);
    }

    public static UserRole getUserRole() {
        return USER_ROLE.get();
    }

    public static void clear() {
        AGENCY_ID.remove();
        USER_ID.remove();
        ADMIN_ID.remove();
        USER_ROLE.remove();
    }

    /** True if current request is platform Admin (logged in via /api/admin/auth/login). */
    public static boolean isAdmin() {
        return ADMIN_ID.get() != null;
    }

    /** True if SUPER_ADMIN (either Admin or User with SUPER_ADMIN role). Can create agencies and activate/deactivate. */
    public static boolean isSuperAdmin() {
        return UserRole.SUPER_ADMIN == USER_ROLE.get();
    }

    /** True if user belongs to an agency (admin, service, or pilgrimage companion). */
    public static boolean isAgencyUser() {
        UserRole role = USER_ROLE.get();
        return role == UserRole.AGENCY_ADMIN || role == UserRole.AGENCY_AGENT || role == UserRole.PILGRIM_COMPANION;
    }
}
