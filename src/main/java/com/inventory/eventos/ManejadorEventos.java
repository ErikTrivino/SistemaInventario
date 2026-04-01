package com.inventory.eventos;

import com.inventory.modelo.entidades.eventos.NotificacionEvento;
import com.inventory.repositorios.eventos.NotificacionEventoRepositorio;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Manejador central de eventos de dominio del sistema de inventario.
 *
 * <p>Cada método anotado con {@link EventListener} reacciona a un tipo específico
 * de evento publicado por {@link PublicadorEventos}.  Los métodos están marcados
 * con {@link Async} para que la lógica de notificación no bloquee la transacción
 * principal.</p>
 *
 * <p><b>Sin Kafka:</b> Las "notificaciones" se implementan con logging estructurado.
 * Cuando se integre un canal real (email, SMS, WebSocket, etc.) bastará con
 * reemplazar las llamadas de log por el servicio correspondiente.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ManejadorEventos {

    private final NotificacionEventoRepositorio notificacionRepositorio;

    // ─────────────────────────────────────────────────────────────────────────
    // STOCK ACTUALIZADO
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reacciona cuando el stock de un producto cae por debajo del mínimo.
     *
     * <p>Acciones que se realizan:</p>
     * <ol>
     *   <li>Log de advertencia con los datos del producto y sucursal.</li>
     *   <li>Punto de extensión para enviar notificación push/email al encargado.</li>
     * </ol>
     */
    @Async
    @EventListener
    public void manejarStockActualizado(StockActualizadoEvento evento) {
        if (evento.getProductoId() == null) {
            // Evento legacy sin payload; ignorar silenciosamente.
            return;
        }

        log.warn(
            "[ALERTA BAJO STOCK] Producto='{}' (ID={}) | Sucursal ID={} | " +
            "Stock actual={} | Stock mínimo={} | Usuario responsable='{}'",
            evento.getNombreProducto(),
            evento.getProductoId(),
            evento.getSucursalId(),
            evento.getStockActual(),
            evento.getStockMinimo(),
            evento.getUsuarioResponsable()
        );

        // TODO: integrar servicio de notificaciones real
        // notificacionServicio.enviarAlertaBajoStock(
        //     evento.getSucursalId(),
        //     evento.getProductoId(),
        //     evento.getNombreProducto(),
        //     evento.getStockActual(),
        //     evento.getStockMinimo()
        // );

        notificarBajoStock(evento);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // VENTA COMPLETADA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reacciona cuando se registra una venta exitosamente.
     *
     * <p>Acciones que se realizan:</p>
     * <ol>
     *   <li>Log informativo con el resumen de la venta.</li>
     *   <li>Punto de extensión para generar el comprobante o enviar confirmación al cliente.</li>
     * </ol>
     */
    @Async
    @EventListener
    public void manejarVentaCompletada(VentaCompletadaEvento evento) {
        if (evento.getVentaId() == null) {
            return;
        }

        log.info(
            "[VENTA COMPLETADA] Venta ID={} | Sucursal ID={} | Vendedor ID={} | " +
            "Total={} | Fecha={} | Usuario responsable='{}'",
            evento.getVentaId(),
            evento.getSucursalId(),
            evento.getVendedorId(),
            evento.getTotal(),
            evento.getFechaVenta(),
            evento.getUsuarioResponsable()
        );

        // TODO: integrar servicio de notificaciones real
        // notificacionServicio.enviarConfirmacionVenta(
        //     evento.getVentaId(),
        //     evento.getVendedorId(),
        //     evento.getTotal()
        // );

        notificarVentaCompletada(evento);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TRANSFERENCIA CREADA
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reacciona cuando se crea o actualiza el estado de una transferencia entre sucursales.
     *
     * <p>Acciones que se realizan:</p>
     * <ol>
     *   <li>Log informativo con el flujo origen → destino y el estado actual.</li>
     *   <li>Punto de extensión para notificar al responsable de la sucursal destino.</li>
     * </ol>
     */
    @Async
    @EventListener
    public void manejarTransferenciaCreada(TransferenciaCreadaEvento evento) {
        if (evento.getTransferenciaId() == null) {
            return;
        }

        log.info(
            "[TRANSFERENCIA {}] ID={} | Origen={} → Destino={} | Fecha={} | Usuario responsable='{}'",
            evento.getEstado(),
            evento.getTransferenciaId(),
            evento.getSucursalOrigenId(),
            evento.getSucursalDestinoId(),
            evento.getFechaSolicitud(),
            evento.getUsuarioResponsable()
        );

        // TODO: integrar servicio de notificaciones real
        // notificacionServicio.notificarNuevaTransferencia(
        //     evento.getTransferenciaId(),
        //     evento.getSucursalDestinoId(),
        //     evento.getEstado()
        // );

        notificarTransferenciaCreada(evento);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Métodos auxiliares de "notificación" (simulados con logging)
    // En producción estos se reemplazarían por llamadas reales.
    // ─────────────────────────────────────────────────────────────────────────

    private void notificarBajoStock(StockActualizadoEvento evento) {
        String mensaje = String.format(
            "Alerta de bajo stock: Producto='%s' (ID=%d) en Sucursal ID=%d. Stock actual (%s) es menor al mínimo (%s).",
            evento.getNombreProducto(), evento.getProductoId(), evento.getSucursalId(),
            evento.getStockActual(), evento.getStockMinimo()
        );
        
        log.info("[NOTIFICACIÓN] " + mensaje + " Acción ejecutada por usuario='{}'.", evento.getUsuarioResponsable());

        NotificacionEvento entity = NotificacionEvento.builder()
                .tipoEvento("BAJO_STOCK")
                .sucursalId(evento.getSucursalId())
                .entidadId(evento.getProductoId())
                .mensaje(mensaje)
                .usuarioResponsable(evento.getUsuarioResponsable())
                .build();
        notificacionRepositorio.save(entity);
    }

    private void notificarVentaCompletada(VentaCompletadaEvento evento) {
        String mensaje = String.format(
            "Confirmación de venta: Venta #%d por $ %s registrada en Sucursal ID=%d.",
            evento.getVentaId(), evento.getTotal(), evento.getSucursalId()
        );

        log.info("[NOTIFICACIÓN] " + mensaje + " por usuario='{}'.", evento.getUsuarioResponsable());

        NotificacionEvento entity = NotificacionEvento.builder()
                .tipoEvento("VENTA")
                .sucursalId(evento.getSucursalId())
                .entidadId(evento.getVentaId())
                .mensaje(mensaje)
                .usuarioResponsable(evento.getUsuarioResponsable())
                .build();
        notificacionRepositorio.save(entity);
    }

    private void notificarTransferenciaCreada(TransferenciaCreadaEvento evento) {
        String mensaje = String.format(
            "Aviso de transferencia: Nueva transferencia #%d en estado '%s'. Origen=%d → Destino=%d.",
            evento.getTransferenciaId(), evento.getEstado(),
            evento.getSucursalOrigenId(), evento.getSucursalDestinoId()
        );

        log.info("[NOTIFICACIÓN] " + mensaje + ", solicitada por usuario='{}'.", evento.getUsuarioResponsable());

        NotificacionEvento entity = NotificacionEvento.builder()
                .tipoEvento("TRANSFERENCIA")
                .sucursalId(evento.getSucursalDestinoId()) // Notificamos a la de destino o ambas? Usamos destino por ahora.
                .entidadId(evento.getTransferenciaId())
                .mensaje(mensaje)
                .usuarioResponsable(evento.getUsuarioResponsable())
                .build();
        notificacionRepositorio.save(entity);
    }
}
