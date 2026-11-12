package com.danielospina.polizas.model;

import com.danielospina.polizas.exception.ReglaNegocioException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Póliza de arrendamiento. Contiene las reglas de negocio que dependen de su
 * estado y de su tipo, para que ningún servicio pueda saltárselas.
 */
@Entity
@Table(name = "polizas")
public class Poliza {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String numero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoPoliza tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoPoliza estado;

    @Column(nullable = false)
    private String tomador;

    @Column(nullable = false)
    private LocalDate fechaInicio;

    @Column(nullable = false)
    private LocalDate fechaFin;

    @Column(nullable = false)
    private int mesesVigencia;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal canonMensual;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prima;

    @OneToMany(mappedBy = "poliza", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Riesgo> riesgos = new ArrayList<>();

    protected Poliza() {
        // Requerido por JPA
    }

    public Poliza(String numero, TipoPoliza tipo, String tomador, LocalDate fechaInicio, int mesesVigencia) {
        this.numero = numero;
        this.tipo = tipo;
        this.tomador = tomador;
        this.fechaInicio = fechaInicio;
        this.mesesVigencia = mesesVigencia;
        this.fechaFin = fechaInicio.plusMonths(mesesVigencia).minusDays(1);
        this.estado = EstadoPoliza.ACTIVA;
        this.canonMensual = BigDecimal.ZERO;
        this.prima = BigDecimal.ZERO;
    }

    /** Regla: una póliza individual solo puede tener 1 riesgo. */
    public void agregarRiesgo(Riesgo riesgo) {
        validarNoCancelada("agregar riesgos");
        if (tipo == TipoPoliza.INDIVIDUAL && !riesgosActivos().isEmpty()) {
            throw new ReglaNegocioException("Una póliza individual solo puede tener un riesgo");
        }
        riesgo.asignarPoliza(this);
        riesgos.add(riesgo);
        recalcularTotales();
    }

    /** Regla: no se puede renovar una póliza cancelada. Canon y prima aumentan según el IPC. */
    public void renovar(BigDecimal ipcPorcentaje) {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se puede renovar una póliza cancelada");
        }
        BigDecimal factor = BigDecimal.ONE.add(ipcPorcentaje.divide(CIEN, 6, RoundingMode.HALF_UP));
        riesgosActivos().forEach(riesgo -> riesgo.ajustarCanon(factor, mesesVigencia));

        this.fechaInicio = fechaFin.plusDays(1);
        this.fechaFin = fechaInicio.plusMonths(mesesVigencia).minusDays(1);
        this.estado = EstadoPoliza.RENOVADA;
        recalcularTotales();
    }

    /** Regla: la cancelación de una póliza cancela todos sus riesgos. */
    public void cancelar() {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("La póliza ya está cancelada");
        }
        riesgosActivos().forEach(Riesgo::cancelar);
        this.estado = EstadoPoliza.CANCELADA;
    }

    /** Cancela un riesgo de una póliza colectiva y actualiza canon y prima. */
    public void cancelarRiesgo(Riesgo riesgo) {
        validarNoCancelada("cancelar riesgos");
        if (tipo == TipoPoliza.INDIVIDUAL) {
            throw new ReglaNegocioException(
                    "El riesgo de una póliza individual no se cancela por separado: se debe cancelar la póliza");
        }
        riesgo.cancelar();
        recalcularTotales();
    }

    public List<Riesgo> riesgosActivos() {
        return riesgos.stream().filter(Riesgo::estaActivo).toList();
    }

    private void recalcularTotales() {
        this.canonMensual = riesgosActivos().stream()
                .map(Riesgo::getCanonMensual)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        this.prima = riesgosActivos().stream()
                .map(Riesgo::getPrima)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validarNoCancelada(String accion) {
        if (estado == EstadoPoliza.CANCELADA) {
            throw new ReglaNegocioException("No se pueden " + accion + " en una póliza cancelada");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNumero() {
        return numero;
    }

    public TipoPoliza getTipo() {
        return tipo;
    }

    public EstadoPoliza getEstado() {
        return estado;
    }

    public String getTomador() {
        return tomador;
    }

    public LocalDate getFechaInicio() {
        return fechaInicio;
    }

    public LocalDate getFechaFin() {
        return fechaFin;
    }

    public int getMesesVigencia() {
        return mesesVigencia;
    }

    public BigDecimal getCanonMensual() {
        return canonMensual;
    }

    public BigDecimal getPrima() {
        return prima;
    }

    public List<Riesgo> getRiesgos() {
        return List.copyOf(riesgos);
    }
}
