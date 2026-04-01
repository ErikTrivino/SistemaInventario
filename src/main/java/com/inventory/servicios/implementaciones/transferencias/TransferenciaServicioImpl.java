package com.inventory.servicios.implementaciones.transferencias;

import com.inventory.eventos.PublicadorEventos;
import com.inventory.servicios.interfaces.transferencias.TransferenciaServicio;
import com.inventory.servicios.interfaces.inventario.InventarioServicio;
import com.inventory.repositorios.transferencias.TransferenciaRepositorio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import com.inventory.modelo.dto.transferencias.*;
import com.inventory.modelo.entidades.transferencias.Transferencia;
import com.inventory.modelo.entidades.transferencias.DetalleTransferencia;
import com.inventory.modelo.entidades.logistica.Envio;
import com.inventory.modelo.entidades.transferencias.EstadoTransferencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Implementación refactorizada que soporta transferencias multiproducto y
 * seguimiento logístico.
 */
@Service
@RequiredArgsConstructor
public class TransferenciaServicioImpl implements TransferenciaServicio {
    private final TransferenciaRepositorio transferRepository;
    private final InventarioServicio inventoryService;
    private final AuditoriaServicio auditService;
    private final PublicadorEventos eventPublisher;

    @Override
    @Transactional
    public TransferenciaInformacionDTO requestTransfer(TransferenciaCrearDTO dto, Long userId) {
        // 1. Crear el cabezal de la transferencia
        Transferencia transfer = Transferencia.builder()
                .sucursalOrigenId(dto.idSucursalOrigen())
                .sucursalDestinoId(dto.idSucursalDestino())
                .usuarioSolicitaId(userId)
                .fechaSolicitud(LocalDateTime.now())
                .estado(EstadoTransferencia.SOLICITADO.name())
                .build();

        // 2. Crear los detalles para cada producto solicitado
        List<DetalleTransferencia> detalles = dto.items().stream()
                .map(item -> DetalleTransferencia.builder()
                        .transferencia(transfer)
                        .productoId(item.idProducto())
                        .cantidadSolicitada(item.cantidad())
                        .build())
                .collect(Collectors.toList());

        transfer.setDetalles(detalles);
        Transferencia saved = transferRepository.save(transfer);

        auditService.registrarAccion(userId.toString(), "REQUEST_TRANSFER", "Transferencia", saved.getId(),
                "Solicitud de transferencia con los productos: " + dto.items().size());

        eventPublisher.publicarTransferenciaCreada(saved, userId.toString());

        return toInfo(saved);
    }

    @Override
    @Transactional
    public TransferenciaInformacionDTO prepareTransfer(TransferenciaPrepararDTO dto) {
        Transferencia transfer = transferRepository.findById(dto.idTransferencia()).orElseThrow();

        // En una implementación real, se recibiría una lista de cantidades confirmadas
        // por producto.
        // Por simplicidad en este ajuste de errores, confirmamos todos al 100% o según
        // un DTO expandido.
        transfer.getDetalles().forEach(detalle -> {
            detalle.setCantidadConfirmada(detalle.getCantidadSolicitada());
        });

        transfer.setEstado("PREPARADO");
        auditService.registrarAccion("1", "PREPARE_TRANSFER", "Transferencia", transfer.getId(),
                "Productos preparados para despacho");
        return toInfo(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public TransferenciaInformacionDTO shipTransfer(TransferenciaConfirmarEnvioDTO dto) {
        Transferencia transfer = transferRepository.findById(dto.idTransferencia()).orElseThrow();

        // RF-19: Descontar stock de origen para cada producto en la transferencia
        for (DetalleTransferencia detalle : transfer.getDetalles()) {
            inventoryService.updateStock(
                    detalle.getProductoId(),
                    transfer.getSucursalOrigenId(),
                    detalle.getCantidadConfirmada().doubleValue(),
                    "OUT",
                    "Salida por Transferencia #" + transfer.getId(),
                    transfer.getUsuarioSolicitaId() != null
                        ? transfer.getUsuarioSolicitaId().toString()
                        : "sistema");
        }

        // Crear registro de envío (Logística)
        Envio envio = Envio.builder()
                .transferencia(transfer)
                .fechaDespacho(LocalDateTime.now())
                .estado(com.inventory.modelo.enums.EstadoLogistico.EN_TRANSITO)
                .build();

        transfer.setEnvio(envio);
        transfer.setEstado(EstadoTransferencia.EN_TRANSITO.name());
        Transferencia savedShip = transferRepository.save(transfer);

        auditService.registrarAccion("1", "SHIP_TRANSFER", "Transferencia", savedShip.getId(), "Mercancía en tránsito");
        eventPublisher.publicarTransferenciaCreada(savedShip,
                transfer.getUsuarioSolicitaId() != null
                    ? transfer.getUsuarioSolicitaId().toString()
                    : "sistema");
        return toInfo(savedShip);
    }

    @Override
    @Transactional
    public TransferenciaInformacionDTO receiveTransfer(TransferenciaRecepcionDTO dto) {
        Transferencia transfer = transferRepository.findById(dto.idTransferencia()).orElseThrow();

        // RF-20: Sumar inventario en destino para cada producto
        for (DetalleTransferencia detalle : transfer.getDetalles()) {
            // En una recepción real se recibiría cantidad por item, aquí asumimos total por
            // simplicidad del fix
            detalle.setCantidadRecibida(detalle.getCantidadConfirmada());

            inventoryService.updateStock(
                    detalle.getProductoId(),
                    transfer.getSucursalDestinoId(),
                    detalle.getCantidadRecibida().doubleValue(),
                    "IN",
                    "Entrada por Transferencia #" + transfer.getId(),
                    transfer.getUsuarioSolicitaId() != null
                        ? transfer.getUsuarioSolicitaId().toString()
                        : "sistema");
        }

        if (transfer.getEnvio() != null) {
            transfer.getEnvio().setFechaRecepcionReal(LocalDateTime.now());
            transfer.getEnvio().setEstado(com.inventory.modelo.enums.EstadoLogistico.RECIBIDO);
        }

        transfer.setEstado(EstadoTransferencia.RECIBIDO.name());
        Transferencia savedReceive = transferRepository.save(transfer);

        auditService.registrarAccion("1", "RECEIVE_TRANSFER", "Transferencia", savedReceive.getId(),
                "Recepción completada");
        eventPublisher.publicarTransferenciaCreada(savedReceive,
                transfer.getUsuarioSolicitaId() != null
                    ? transfer.getUsuarioSolicitaId().toString()
                    : "sistema");
        return toInfo(savedReceive);
    }

    @Override
    public Page<TransferenciaInformacionDTO> getTransfers(Long branchId, String status, LocalDateTime startDate,
            LocalDateTime endDate, Integer pagina, Integer porPagina) {
        int pageNumber = (pagina != null) ? pagina : 0;
        int pageSize = (porPagina != null && porPagina > 0) ? porPagina : 10;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        return transferRepository.findHistoricalTransfers(branchId, status, startDate, endDate, pageable)
                .map(this::toInfo);
    }

    private TransferenciaInformacionDTO toInfo(Transferencia t) {
        List<TransferenciaInformacionDTO.ResumenDetalleDTO> items = t.getDetalles().stream()
                .map(d -> new TransferenciaInformacionDTO.ResumenDetalleDTO(
                        d.getProductoId(), d.getCantidadSolicitada(), d.getCantidadConfirmada(),
                        d.getCantidadRecibida(), d.getMotivoDiferencia()))
                .collect(Collectors.toList());

        TransferenciaInformacionDTO.EnvioInfoDTO envioInfo = null;
        if (t.getEnvio() != null) {
            envioInfo = new TransferenciaInformacionDTO.EnvioInfoDTO(
                    t.getEnvio().getId(), t.getEnvio().getFechaDespacho(),
                    t.getEnvio().getTiempoEstimadoEntrega(), t.getEnvio().getFechaRecepcionReal(),
                    t.getEnvio().getEstado().name());
        }

        return new TransferenciaInformacionDTO(
                t.getId(), t.getSucursalOrigenId(), t.getSucursalDestinoId(),
                t.getEstado(), t.getFechaSolicitud(), items, envioInfo);
    }
}
