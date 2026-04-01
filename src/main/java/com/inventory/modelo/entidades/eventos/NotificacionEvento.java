package com.inventory.modelo.entidades.eventos;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones_eventos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificacionEvento {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento; // BAJO_STOCK, VENTA, TRANSFERENCIA
    
    @Column(name = "sucursal_id")
    private Long sucursalId;
    
    @Column(name = "entidad_id")
    private Long entidadId; // ID de la venta, transferencia, etc.
    
    @Column(name = "mensaje", columnDefinition = "TEXT")
    private String mensaje;
    
    @Column(name = "usuario_responsable")
    private String usuarioResponsable;
    
    @Column(name = "fecha_registro")
    private LocalDateTime fechaRegistro;

    @PrePersist
    protected void onCreate() {
        fechaRegistro = LocalDateTime.now();
    }
}
