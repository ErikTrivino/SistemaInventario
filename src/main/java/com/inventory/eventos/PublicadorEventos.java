    package com.inventory.eventos;
    import org.springframework.context.ApplicationEventPublisher;
    import org.springframework.stereotype.Component;
    import lombok.RequiredArgsConstructor;

    @Component
    @RequiredArgsConstructor
    public class PublicadorEventos {
        private final ApplicationEventPublisher publisher;

        public void publishStockUpdate(Object source) {
            publisher.publishEvent(new StockActualizadoEvento(source));
        }

        public void publishSale(Object source) {
            publisher.publishEvent(new VentaCompletadaEvento(source));
        }

        public void publishTransfer(Object source) {
            publisher.publishEvent(new TransferenciaCreadaEvento(source));
        }
    }



