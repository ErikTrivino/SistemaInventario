package com.inventory.modelo.enums;

/**
 * Enum de roles del sistema de inventario.
 * Usado en los claims del token JWT para controlar el acceso
 * a los distintos prefijos de ruta.
 *
 *   ADMIN    → /api/admin/**
 *   MANAGER  → /api/manager/**
 *   OPERATOR → /api/operator/**
 */
public enum Rol {
    ADMIN,
    MANAGER,
    OPERATOR
}


