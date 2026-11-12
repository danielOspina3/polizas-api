package com.danielospina.polizas.model;

import com.danielospina.polizas.exception.ReglaNegocioException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PolizaTest {

    private static final BigDecimal IPC = new BigDecimal("5.20");

    @Test
    void renovarIncrementaCanonYPrimaSegunIpc() {
        Poliza poliza = polizaIndividualConRiesgo();

        poliza.renovar(IPC);

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.RENOVADA);
        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("1578000");
        assertThat(poliza.getPrima()).isEqualByComparingTo("18936000");
        assertThat(poliza.getFechaInicio()).isEqualTo(LocalDate.of(2027, 1, 1));
        assertThat(poliza.getFechaFin()).isEqualTo(LocalDate.of(2027, 12, 31));
    }

    @Test
    void noSePuedeRenovarUnaPolizaCancelada() {
        Poliza poliza = polizaIndividualConRiesgo();
        poliza.cancelar();

        assertThatThrownBy(() -> poliza.renovar(IPC))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("cancelada");
    }

    @Test
    void cancelarPolizaCancelaTodosSusRiesgos() {
        Poliza poliza = new Poliza("POL-2", TipoPoliza.COLECTIVA, "Inmobiliaria", LocalDate.of(2026, 1, 1), 12);
        poliza.agregarRiesgo(new Riesgo("Calle 1", "Juan", "Pedro", new BigDecimal("1000000")));
        poliza.agregarRiesgo(new Riesgo("Calle 2", "María", "Ana", new BigDecimal("2000000")));

        poliza.cancelar();

        assertThat(poliza.getEstado()).isEqualTo(EstadoPoliza.CANCELADA);
        assertThat(poliza.getRiesgos()).allMatch(riesgo -> riesgo.getEstado() == EstadoRiesgo.CANCELADO);
    }

    @Test
    void polizaIndividualSoloPuedeTenerUnRiesgo() {
        Poliza poliza = polizaIndividualConRiesgo();

        assertThatThrownBy(() -> poliza.agregarRiesgo(new Riesgo("Calle 2", "Carlos", "Ana", new BigDecimal("900000"))))
                .isInstanceOf(ReglaNegocioException.class)
                .hasMessageContaining("un riesgo");
    }

    @Test
    void colectivaSumaCanonYPrimaDeSusRiesgosActivos() {
        Poliza poliza = new Poliza("POL-3", TipoPoliza.COLECTIVA, "Inmobiliaria", LocalDate.of(2026, 1, 1), 12);
        Riesgo primero = new Riesgo("Calle 1", "Juan", "Pedro", new BigDecimal("1000000"));
        poliza.agregarRiesgo(primero);
        poliza.agregarRiesgo(new Riesgo("Calle 2", "María", "Ana", new BigDecimal("2000000")));

        poliza.cancelarRiesgo(primero);

        assertThat(poliza.getCanonMensual()).isEqualByComparingTo("2000000");
        assertThat(poliza.getPrima()).isEqualByComparingTo("24000000");
    }

    private Poliza polizaIndividualConRiesgo() {
        Poliza poliza = new Poliza("POL-1", TipoPoliza.INDIVIDUAL, "Carlos Pérez", LocalDate.of(2026, 1, 1), 12);
        poliza.agregarRiesgo(new Riesgo("Calle 45 # 12-30", "Carlos Pérez", "Ana Rodríguez", new BigDecimal("1500000")));
        return poliza;
    }
}
