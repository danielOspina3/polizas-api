package com.danielospina.polizas.dto;

import com.danielospina.polizas.model.EstadoRiesgo;
import com.danielospina.polizas.model.Riesgo;

import java.math.BigDecimal;

public record RiesgoResponse(
        Long id,
        Long polizaId,
        String direccionInmueble,
        String arrendatario,
        String arrendador,
        BigDecimal canonMensual,
        BigDecimal prima,
        EstadoRiesgo estado) {

    public static RiesgoResponse from(Riesgo riesgo) {
        return new RiesgoResponse(
                riesgo.getId(),
                riesgo.getPoliza().getId(),
                riesgo.getDireccionInmueble(),
                riesgo.getArrendatario(),
                riesgo.getArrendador(),
                riesgo.getCanonMensual(),
                riesgo.getPrima(),
                riesgo.getEstado());
    }
}
