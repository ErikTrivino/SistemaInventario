package com.inventory.eventos;

import org.springframework.context.ApplicationEvent;

public class TransferenciaCreadaEvento extends ApplicationEvent {
    public TransferenciaCreadaEvento(Object source) {
        super(source);
    }
}



