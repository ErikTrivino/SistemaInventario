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
    private String usuario;
    
    @Column(name = "accion", nullable = false)
    private String accion; // CREATE, UPDATE, DELETE
    
    @Column(name = "entidad", nullable = false)
    private String entidad;
    
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;
    
    @Column(name = "detalles", columnDefinition = "TEXT")
    private String detalles;
}
