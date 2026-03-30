package com.inventory.seguridad.domain;

import com.inventory.modelo.enums.Rol;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ConversorRol implements AttributeConverter<Rol, Integer> {
    @Override
    public Integer convertToDatabaseColumn(Rol rol) {
        if (rol == null) {
            return null;
        }
        return switch (rol) {
            case ADMIN -> 1;
            case MANAGER -> 2;
            case OPERATOR -> 3;
        };
    }

    @Override
    public Rol convertToEntityAttribute(Integer roleId) {
        if (roleId == null) {
            return null;
        }
        return switch (roleId) {
            case 1 -> Rol.ADMIN;
            case 2 -> Rol.MANAGER;
            case 3 -> Rol.OPERATOR;
            default -> throw new IllegalArgumentException("Rol desconocido para id_role: " + roleId);
        };
    }
}


