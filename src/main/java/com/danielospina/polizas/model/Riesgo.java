package com.danielospina.polizas.model;

import com.danielospina.polizas.exception.ReglaNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Riesgo asegurado: un inmueble arrendado, con su arrendatario (asegurado)
 * y su arrendador (beneficiario).
 */
@Entity
@Table(name = "riesgos")
public class Riesgo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "poliza_id", nullable = false)
    private Poliza poliza;

    @Column(nullable = false)
    private String direccionInmueble;

    @Column(nullable = false)
    private String arrendatario;

    @Column(nullable = false)
    private String arrendador;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoRiesgo estado;

    protected Riesgo() {
        // Requerido por JPA
    }

    public Riesgo(String direccionInmueble, String arrendatario, String arrendador, BigDecimal canonMensual) {
        this.direccionInmueble = direccionInmueble;
        this.arrendatario = arrendatario;
        this.arrendador = arrendador;
        this.canonMensual = canonMensual.setScale(2, RoundingMode.HALF_UP);
        this.prima = BigDecimal.ZERO;
        this.estado = EstadoRiesgo.ACTIVO;
    }

    /** Prima = canon mensual x meses de vigencia de la póliza. */
    void asignarPoliza(Poliza poliza) {
        this.poliza = poliza;
        this.prima = canonMensual.multiply(BigDecimal.valueOf(poliza.getMesesVigencia()));
    }

    /** Aplica el incremento del IPC. El canon se redondea al peso y se guarda con 2 decimales. */
    void ajustarCanon(BigDecimal factor, int mesesVigencia) {
        this.canonMensual = canonMensual.multiply(factor)
                .setScale(0, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
        this.prima = canonMensual.multiply(BigDecimal.valueOf(mesesVigencia));
    }

    void cancelar() {
        if (estado == EstadoRiesgo.CANCELADO) {
            throw new ReglaNegocioException("El riesgo ya está cancelado");
        }
        this.estado = EstadoRiesgo.CANCELADO;
    }

    public boolean estaActivo() {
        return estado == EstadoRiesgo.ACTIVO;
    }

    public Long getId() {
        return id;
    }

    public Poliza getPoliza() {
        return poliza;
    }

    public String getDireccionInmueble() {
        return direccionInmueble;
    }

    public String getArrendatario() {
        return arrendatario;
    }

    public String getArrendador() {
        return arrendador;
    }

    public BigDecimal getCanonMensual() {
        return canonMensual;
    }

    public BigDecimal getPrima() {
        return prima;
    }

    public EstadoRiesgo getEstado() {
        return estado;
    }
}
