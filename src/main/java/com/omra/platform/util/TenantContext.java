package com.omra.platform.util;

import com.omra.platform.entity.enums.UserRole;

import java.util.Collections;
import java.util.List;

/**
 * Thread-local context for current request: agency id and user role.
 * Used for multi-tenant filtering and RBAC.
 */
public final class TenantContext {

    private static final ThreadLocal<Long> AGENCY_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> USER_ID = new ThreadLocal<>();
    private static final ThreadLocal<Long> ADMIN_ID = new ThreadLocal<>();
    private static final ThreadLocal<UserRole> USER_ROLE = new ThreadLocal<>();
    /** Main agency + direct sub-agency ids when the JWT agency is a root; otherwise a single id. Set per request after JWT parse. */
    private static final ThreadLocal<List<Long>> ACCESSIBLE_AGENCY_IDS = new ThreadLocal<>();

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

    public static void setAccessibleAgencyIds(List<Long> ids) {
        ACCESSIBLE_AGENCY_IDS.set(ids);
    }

    public static List<Long> getAccessibleAgencyIds() {
        return ACCESSIBLE_AGENCY_IDS.get();
    }

    /**
     * Non–super-admin: whether the given agency id is visible in this request (own agency or a sub of the user’s main).
     */
    public static boolean canAccessAgencyId(Long agencyId) {
        if (agencyId == null) {
            return false;
        }
        if (isSuperAdmin()) {
            return true;
        }
        List<Long> scoped = getAccessibleAgencyIds();
        if (scoped != null && !scoped.isEmpty()) {
            return scoped.contains(agencyId);
        }
        Long ctx = getAgencyId();
        return ctx != null && ctx.equals(agencyId);
    }

    /**
     * Tenant-scoped id list for {@code IN (...)} queries. Returns {@code null} only when the caller should use the
     * global super-admin code path (no agency in JWT).
     */
    public static List<Long> getScopedAgencyIdsForQueries() {
        if (isSuperAdmin() && getAgencyId() == null) {
            return null;
        }
        List<Long> scoped = getAccessibleAgencyIds();
        if (scoped != null && !scoped.isEmpty()) {
            return scoped;
        }
        Long ctx = getAgencyId();
        if (ctx != null) {
            return Collections.singletonList(ctx);
        }
        return Collections.emptyList();
    }

    public static void clear() {
        AGENCY_ID.remove();
        USER_ID.remove();
        ADMIN_ID.remove();
        USER_ROLE.remove();
        ACCESSIBLE_AGENCY_IDS.remove();
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
