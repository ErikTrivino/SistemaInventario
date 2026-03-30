    package com.inventory.servicios.interfaces.transferencias;
    import com.inventory.modelo.dto.transferencias.TransferenciaCrearDTO;
    import com.inventory.modelo.dto.transferencias.TransferenciaInformacionDTO;
    import com.inventory.modelo.dto.transferencias.TransferenciaRecepcionParcialDTO;

    public interface TransferenciaServicio {
        TransferenciaInformacionDTO requestTransfer(TransferenciaCrearDTO dto);
        void approveTransfer(Long transferId);
        void shipTransfer(Long transferId);
        void receiveTransfer(Long transferId);
        void receivePartialTransfer(Long transferId, TransferenciaRecepcionParcialDTO recepcionParcialDTO);
    }



