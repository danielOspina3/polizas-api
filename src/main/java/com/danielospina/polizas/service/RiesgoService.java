package com.danielospina.polizas.service;

import com.danielospina.polizas.dto.RiesgoResponse;
import com.danielospina.polizas.exception.RecursoNoEncontradoException;
import com.danielospina.polizas.integration.OperacionPolizaEvent;
import com.danielospina.polizas.model.Poliza;
import com.danielospina.polizas.model.Riesgo;
import com.danielospina.polizas.repository.RiesgoRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class RiesgoService {

    private final RiesgoRepository riesgoRepository;
    private final ApplicationEventPublisher eventPublisher;

    public RiesgoService(RiesgoRepository riesgoRepository, ApplicationEventPublisher eventPublisher) {
        this.riesgoRepository = riesgoRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public RiesgoResponse cancelar(Long riesgoId) {
        Riesgo riesgo = riesgoRepository.findById(riesgoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe el riesgo con id " + riesgoId));
        Poliza poliza = riesgo.getPoliza();
        poliza.cancelarRiesgo(riesgo);
        eventPublisher.publishEvent(new OperacionPolizaEvent("CANCELACION_RIESGO", poliza.getId()));
        return RiesgoResponse.from(riesgo);
    }
}
