package com.inventory.eventos;

import org.springframework.context.ApplicationEvent;

public class StockActualizadoEvento extends ApplicationEvent {
    public StockActualizadoEvento(Object source) {
        super(source);
    }
}



