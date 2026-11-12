package com.danielospina.polizas.service;

import com.danielospina.polizas.dto.CrearRiesgoRequest;
import com.danielospina.polizas.dto.PolizaResponse;
import com.danielospina.polizas.exception.RecursoNoEncontradoException;
import com.danielospina.polizas.exception.ReglaNegocioException;
import com.danielospina.polizas.integration.OperacionPolizaEvent;
import com.danielospina.polizas.model.EstadoPoliza;
import com.danielospina.polizas.model.Poliza;
import com.danielospina.polizas.model.Riesgo;
import com.danielospina.polizas.model.TipoPoliza;
import com.danielospina.polizas.repository.PolizaRepository;
import com.danielospina.polizas.repository.RiesgoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PolizaServiceTest {

    @Mock
    private PolizaRepository polizaRepository;

    @Mock
    private RiesgoRepository riesgoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private PolizaService polizaService;

    @BeforeEach
    void setUp() {
        polizaService = new PolizaService(polizaRepository, riesgoRepository, eventPublisher, new BigDecimal("5.20"));
    }

    @Test
    void renovarActualizaLaPolizaYNotificaAlCore() {
        Poliza poliza = polizaIndividual(1L);
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(poliza));

        PolizaResponse respuesta = polizaService.renovar(1L);

        assertThat(respuesta.estado()).isEqualTo(EstadoPoliza.RENOVADA);
        assertThat(respuesta.canonMensual()).isEqualByComparingTo("1578000");
        verify(eventPublisher).publishEvent(new OperacionPolizaEvent("RENOVACION", 1L));
    }

    @Test
    void agregarRiesgoAPolizaIndividualEsRechazado() {
        when(polizaRepository.findById(1L)).thenReturn(Optional.of(polizaIndividual(1L)));
        CrearRiesgoRequest request = new CrearRiesgoRequest("Calle 1", "Juan", "Pedro", new BigDecimal("1000000"));

        assertThatThrownBy(() -> polizaService.agregarRiesgo(1L, request))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("colectivas");
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void polizaInexistenteLanzaNoEncontrado() {
        when(polizaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> polizaService.cancelar(99L))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    private Poliza polizaIndividual(Long id) {
        Poliza poliza = new Poliza("POL-1", TipoPoliza.INDIVIDUAL, "Carlos Pérez", LocalDate.of(2026, 1, 1), 12);
        poliza.agregarRiesgo(new Riesgo("Calle 45 # 12-30", "Carlos Pérez", "Ana Rodríguez", new BigDecimal("1500000")));
        ReflectionTestUtils.setField(poliza, "id", id);
        return poliza;
    }
}
