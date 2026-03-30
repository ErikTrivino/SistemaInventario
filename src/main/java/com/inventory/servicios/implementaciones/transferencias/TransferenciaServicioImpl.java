    package com.inventory.servicios.implementaciones.transferencias;
    import com.inventory.servicios.interfaces.transferencias.TransferenciaServicio;
    import com.inventory.servicios.interfaces.inventario.InventarioServicio;
    import com.inventory.repositorios.transferencias.TransferenciaRepositorio;
    import com.inventory.modelo.dto.transferencias.TransferenciaCrearDTO;
    import com.inventory.modelo.dto.transferencias.TransferenciaInformacionDTO;
    import com.inventory.modelo.dto.transferencias.TransferenciaRecepcionParcialDTO;
    import com.inventory.modelo.entidades.transferencias.Transferencia;
    import com.inventory.modelo.entidades.transferencias.EstadoTransferencia;
    import org.springframework.stereotype.Service;
    import org.springframework.transaction.annotation.Transactional;
    import lombok.RequiredArgsConstructor;

    @Service
    @RequiredArgsConstructor
    public class TransferenciaServicioImpl implements TransferenciaServicio {
        private final TransferenciaRepositorio transferRepository;
        private final InventarioServicio inventoryService;

        @Override
        @Transactional
        public TransferenciaInformacionDTO requestTransfer(TransferenciaCrearDTO dto) {
            Transferencia transfer = Transferencia.builder()
                    .originBranchId(dto.idSucursalOrigen())
                    .destinationBranchId(dto.idSucursalDestino())
                    .productId(dto.idProducto())
                    .quantity(dto.cantidad())
                    .requestUserId(dto.idUsuarioSolicita())
                    .requestDate(java.time.LocalDateTime.now())
                    .estimatedDate(dto.fechaEnvioEstimada())
                    .build();
            transfer.setStatus(EstadoTransferencia.SOLICITADO.name());
            return toInfo(transferRepository.save(transfer));
        }

        @Override
        @Transactional
        public void approveTransfer(Long transferId) {
            Transferencia transfer = transferRepository.findById(transferId).orElseThrow();
            // valida stock origen
            transfer.setStatus(EstadoTransferencia.APROBADO.name());
            transferRepository.save(transfer);
        }

        @Override
        @Transactional
        public void shipTransfer(Long transferId) {
            Transferencia transfer = transferRepository.findById(transferId).orElseThrow();
            // descuenta inventario origen
            transfer.setStatus(EstadoTransferencia.EN_TRANSITO.name());
            transferRepository.save(transfer);
        }

        @Override
        @Transactional
        public void receiveTransfer(Long transferId) {
            Transferencia transfer = transferRepository.findById(transferId).orElseThrow();
            // suma inventario destino
            transfer.setStatus(EstadoTransferencia.RECIBIDO.name());
            transferRepository.save(transfer);
        }

        @Override
        @Transactional
        public void receivePartialTransfer(Long transferId, TransferenciaRecepcionParcialDTO recepcionParcialDTO) {
            Transferencia transfer = transferRepository.findById(transferId).orElseThrow();
            // registra faltantes, suma recibidos
            transfer.setStatus(EstadoTransferencia.FALTANTES.name());
            transferRepository.save(transfer);
        }

        private TransferenciaInformacionDTO toInfo(Transferencia transfer) {
            return new TransferenciaInformacionDTO(
                    transfer.getId(),
                    transfer.getOriginBranchId(),
                    transfer.getDestinationBranchId(),
                    transfer.getProductId(),
                    transfer.getQuantity(),
                    transfer.getStatus(),
                    transfer.getRequestDate(),
                    transfer.getEstimatedDate(),
                    transfer.getReceivedDate()
            );
        }
    }



