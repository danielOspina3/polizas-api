package com.danielospina.polizas.integration;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Envía el evento al CORE solo después de que la transacción se confirma.
 * Así nunca se notifica al CORE un cambio que terminó revirtiéndose.
 */
@Component
public class CoreEventListener {

    private final CoreClient coreClient;

    public CoreEventListener(CoreClient coreClient) {
        this.coreClient = coreClient;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alConfirmarOperacion(OperacionPolizaEvent evento) {
        coreClient.enviarEvento(evento.evento(), evento.polizaId());
    }
}
