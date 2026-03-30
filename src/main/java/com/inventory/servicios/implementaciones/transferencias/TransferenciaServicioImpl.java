package com.inventory.servicios.implementaciones.transferencias;

import com.inventory.servicios.interfaces.transferencias.TransferenciaServicio;
import com.inventory.servicios.interfaces.inventario.InventarioServicio;
import com.inventory.repositorios.transferencias.TransferenciaRepositorio;
import com.inventory.servicios.interfaces.auditoria.AuditoriaServicio;
import com.inventory.modelo.dto.transferencias.*;
import com.inventory.modelo.entidades.transferencias.Transferencia;
import com.inventory.modelo.entidades.transferencias.EstadoTransferencia;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransferenciaServicioImpl implements TransferenciaServicio {
    private final TransferenciaRepositorio transferRepository;
    private final InventarioServicio inventoryService;
    private final AuditoriaServicio auditService;

    @Override
    @Transactional
    public TransferenciaInformacionDTO requestTransfer(TransferenciaCrearDTO dto, Long userId) {
        Transferencia transfer = Transferencia.builder()
                .sucursalOrigenId(dto.idSucursalOrigen())
                .sucursalDestinoId(dto.idSucursalDestino())
                .productoId(dto.idProducto())
                .cantidad(dto.cantidadSolicitada())
                .usuarioSolicitaId(userId)
                .fechaSolicitud(LocalDateTime.now())
                .estado(EstadoTransferencia.SOLICITADO.name())
                .build();
        Transferencia saved = transferRepository.save(transfer);
        auditService.logAction(userId, "REQUEST_TRANSFER", "Transferencia", saved.getId(), "Transferencia Solicitada");
        return toInfo(saved);
    }

    @Override
    @Transactional
    public TransferenciaInformacionDTO prepareTransfer(TransferenciaPrepararDTO dto) {
        Transferencia transfer = transferRepository.findById(dto.idTransferencia()).orElseThrow();
        // Validar sucursal origen - el controlador puede inyectar esto o se asume validez de momento
        transfer.setCantidadConfirmada(dto.cantidadConfirmada());
        transfer.setEstado("PREPARADO"); // RF-21: Preparar envío
        
        auditService.logAction(1L, "PREPARE_TRANSFER", "Transferencia", transfer.getId(), "Preparado para envíar: " + dto.cantidadConfirmada());
        return toInfo(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public TransferenciaInformacionDTO shipTransfer(TransferenciaConfirmarEnvioDTO dto) {
        Transferencia transfer = transferRepository.findById(dto.idTransferencia()).orElseThrow();
        if (transfer.getCantidadConfirmada() == null) {
            throw new RuntimeException("Debe prepararse la transferencia confirmando cantidad antes de enviar.");
        }
        
        // RF-19: Sólo en origen, descuenta inventario origen atómicamente
        inventoryService.updateStock(
            transfer.getProductoId(), 
            transfer.getSucursalOrigenId(), 
            transfer.getCantidadConfirmada().doubleValue(), 
            "OUT", 
            "Envío Transferencia #" + transfer.getId()
        );

        transfer.setEstado(EstadoTransferencia.EN_TRANSITO.name());
        auditService.logAction(1L, "SHIP_TRANSFER", "Transferencia", transfer.getId(), "Enviado");
        return toInfo(transferRepository.save(transfer));
    }

    @Override
    @Transactional
    public TransferenciaInformacionDTO receiveTransfer(TransferenciaRecepcionDTO dto) {
        Transferencia transfer = transferRepository.findById(dto.idTransferencia()).orElseThrow();
        
        // RF-20: Sumar inventario en recepción destino
        inventoryService.updateStock(
            transfer.getProductoId(), 
            transfer.getSucursalDestinoId(), 
            dto.cantidadRecibida().doubleValue(), 
            "IN", 
            "Recepcioón Transferencia #" + transfer.getId()
        );

        transfer.setCantidadRecibida(dto.cantidadRecibida());
        transfer.setFechaRecepcionReal(LocalDateTime.now());
        
        // Registrar diferencias
        if (transfer.getCantidadConfirmada().compareTo(dto.cantidadRecibida()) != 0) {
            transfer.setEstado(EstadoTransferencia.FALTANTES.name());
        } else {
            transfer.setEstado(EstadoTransferencia.RECIBIDO.name());
        }
        
        auditService.logAction(1L, "RECEIVE_TRANSFER", "Transferencia", transfer.getId(), "Recibido " + dto.cantidadRecibida());
        return toInfo(transferRepository.save(transfer));
    }

    @Override
    public List<TransferenciaInformacionDTO> getTransfers(Long branchId, String status, LocalDateTime startDate, LocalDateTime endDate) {
        return transferRepository.findHistoricalTransfers(branchId, status, startDate, endDate).stream().map(this::toInfo).toList();
    }

    private TransferenciaInformacionDTO toInfo(Transferencia t) {
        return new TransferenciaInformacionDTO(
                t.getId(),
                t.getSucursalOrigenId(),
                t.getSucursalDestinoId(),
                t.getProductoId(),
                t.getCantidad(),
                t.getCantidadConfirmada(),
                t.getCantidadRecibida(),
                t.getEstado(),
                t.getFechaSolicitud(),
                t.getFechaEnvioEstimada(),
                t.getFechaRecepcionReal()
        );
    }
}



