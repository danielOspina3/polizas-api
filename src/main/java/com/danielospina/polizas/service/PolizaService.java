package com.danielospina.polizas.service;

import com.danielospina.polizas.dto.CrearRiesgoRequest;
import com.danielospina.polizas.dto.PolizaResponse;
import com.danielospina.polizas.dto.RiesgoResponse;
import com.danielospina.polizas.exception.RecursoNoEncontradoException;
import com.danielospina.polizas.exception.ReglaNegocioException;
import com.danielospina.polizas.integration.OperacionPolizaEvent;
import com.danielospina.polizas.model.EstadoPoliza;
import com.danielospina.polizas.model.Poliza;
import com.danielospina.polizas.model.Riesgo;
import com.danielospina.polizas.model.TipoPoliza;
import com.danielospina.polizas.repository.PolizaRepository;
import com.danielospina.polizas.repository.RiesgoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class PolizaService {

    private final PolizaRepository polizaRepository;
    private final RiesgoRepository riesgoRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final BigDecimal ipcPorcentaje;

    public PolizaService(PolizaRepository polizaRepository,
                         RiesgoRepository riesgoRepository,
                         ApplicationEventPublisher eventPublisher,
                         @Value("${app.renovacion.ipc-porcentaje}") BigDecimal ipcPorcentaje) {
        this.polizaRepository = polizaRepository;
        this.riesgoRepository = riesgoRepository;
        this.eventPublisher = eventPublisher;
        this.ipcPorcentaje = ipcPorcentaje;
    }

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(TipoPoliza tipo, EstadoPoliza estado) {
        return polizaRepository.buscar(tipo, estado).stream()
                .map(PolizaResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<RiesgoResponse> listarRiesgos(Long polizaId) {
        buscarPoliza(polizaId);
        return riesgoRepository.findByPolizaIdOrderByIdAsc(polizaId).stream()
                .map(RiesgoResponse::from)
                .toList();
    }

    @Transactional
    public PolizaResponse renovar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.renovar(ipcPorcentaje);
        eventPublisher.publishEvent(new OperacionPolizaEvent("RENOVACION", polizaId));
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public PolizaResponse cancelar(Long polizaId) {
        Poliza poliza = buscarPoliza(polizaId);
        poliza.cancelar();
        eventPublisher.publishEvent(new OperacionPolizaEvent("CANCELACION", polizaId));
        return PolizaResponse.from(poliza);
    }

    @Transactional
    public RiesgoResponse agregarRiesgo(Long polizaId, CrearRiesgoRequest request) {
        Poliza poliza = buscarPoliza(polizaId);
        if (poliza.getTipo() != TipoPoliza.COLECTIVA) {
            throw new ReglaNegocioException("Solo se pueden agregar riesgos a pólizas colectivas");
        }
        Riesgo riesgo = new Riesgo(request.direccionInmueble(), request.arrendatario(),
                request.arrendador(), request.canonMensual());
        poliza.agregarRiesgo(riesgo);
        Riesgo guardado = riesgoRepository.save(riesgo);
        eventPublisher.publishEvent(new OperacionPolizaEvent("INCLUSION_RIESGO", polizaId));
        return RiesgoResponse.from(guardado);
    }

    private Poliza buscarPoliza(Long polizaId) {
        return polizaRepository.findById(polizaId)
                .orElseThrow(() -> new RecursoNoEncontradoException("No existe la póliza con id " + polizaId));
    }
}
