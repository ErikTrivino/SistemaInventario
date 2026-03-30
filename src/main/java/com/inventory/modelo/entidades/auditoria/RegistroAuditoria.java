package com.inventory.modelo.entidades.auditoria;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "registros_auditoria")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroAuditoria {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "nombre_usuario", nullable = false)
    private String user;
    
    @Column(name = "accion", nullable = false)
    private String action; // CREATE, UPDATE, DELETE
    
    @Column(name = "entidad", nullable = false)
    private String entity;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "detalles", columnDefinition = "TEXT")
    private String details;
}




