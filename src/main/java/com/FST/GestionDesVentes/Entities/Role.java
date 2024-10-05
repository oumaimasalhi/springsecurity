package com.FST.GestionDesVentes.Entities;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collections;
import lombok.Getter;
import lombok.RequiredArgsConstructor;



@RequiredArgsConstructor
public enum Role {


	USER(Set.of()), // Empty set for USER role since no permissions are assigned
    ADMIN(
            Set.of(
                    Permission.ADMIN_READ,
                    Permission.ADMIN_UPDATE,
                    Permission.ADMIN_DELETE,
                    Permission.ADMIN_CREATE,
                    Permission.MANAGER_READ,
                    Permission.MANAGER_UPDATE,
                    Permission.MANAGER_DELETE,
                    Permission.MANAGER_CREATE
            )
    ),
    MANAGER(
            Set.of(
                    Permission.MANAGER_READ,
                    Permission.MANAGER_UPDATE,
                    Permission.MANAGER_DELETE,
                    Permission.MANAGER_CREATE
            )
    );

    private final Set<Permission> permissions;

    // Explicit constructor for Role enum
    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    // Manually adding the getPermissions() method
    public Set<Permission> getPermissions() {
        return permissions;
    }

    // Method to get authorities based on role and permissions
    public List<SimpleGrantedAuthority> getAuthorities() {
        List<SimpleGrantedAuthority> authorities = getPermissions().stream()
                .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
                .collect(Collectors.toList());

        // Add role-based authority (e.g., "ROLE_ADMIN", "ROLE_USER")
        authorities.add(new SimpleGrantedAuthority("ROLE_" + this.name()));
        return authorities;
    }
}