package com.danielospina.polizas.controller;

import com.danielospina.polizas.dto.CrearRiesgoRequest;
import com.danielospina.polizas.dto.PolizaResponse;
import com.danielospina.polizas.dto.RiesgoResponse;
import com.danielospina.polizas.model.EstadoPoliza;
import com.danielospina.polizas.model.TipoPoliza;
import com.danielospina.polizas.service.PolizaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/polizas")
public class PolizaController {

    private final PolizaService polizaService;

    public PolizaController(PolizaService polizaService) {
        this.polizaService = polizaService;
    }

    @GetMapping
    public List<PolizaResponse> listar(@RequestParam(name = "tipo", required = false) TipoPoliza tipo,
                                       @RequestParam(name = "estado", required = false) EstadoPoliza estado) {
        return polizaService.listar(tipo, estado);
    }

    @GetMapping("/{id}/riesgos")
    public List<RiesgoResponse> listarRiesgos(@PathVariable("id") Long id) {
        return polizaService.listarRiesgos(id);
    }

    @PostMapping("/{id}/renovar")
    public PolizaResponse renovar(@PathVariable("id") Long id) {
        return polizaService.renovar(id);
    }

    @PostMapping("/{id}/cancelar")
    public PolizaResponse cancelar(@PathVariable("id") Long id) {
        return polizaService.cancelar(id);
    }

    @PostMapping("/{id}/riesgos")
    public ResponseEntity<RiesgoResponse> agregarRiesgo(@PathVariable("id") Long id,
                                                        @Valid @RequestBody CrearRiesgoRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(polizaService.agregarRiesgo(id, request));
    }
}
