package com.inventory.eventos;

import org.springframework.context.ApplicationEvent;

public class VentaCompletadaEvento extends ApplicationEvent {
    public VentaCompletadaEvento(Object source) {
        super(source);
    }
}



