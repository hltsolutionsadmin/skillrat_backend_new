package com.skillrat.user.security;

import org.springframework.security.access.prepost.PreAuthorize;
import java.lang.annotation.*;

/**
 * Custom annotation to check if the current user is a business admin.
 * Can be used at the class or method level to secure business admin endpoints.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@PreAuthorize("@businessSecurityService.isBusinessAdmin(authentication, #businessId)")
public @interface BusinessAdminRequired {
    String businessId() default "";
}
