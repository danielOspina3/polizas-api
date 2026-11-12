package com.danielospina.polizas.repository;

import com.danielospina.polizas.model.EstadoPoliza;
import com.danielospina.polizas.model.Poliza;
import com.danielospina.polizas.model.TipoPoliza;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    /** Filtra por tipo y estado; si un filtro llega vacío, no se aplica. */
    @Query("""
            SELECT p FROM Poliza p
            WHERE (:tipo IS NULL OR p.tipo = :tipo)
              AND (:estado IS NULL OR p.estado = :estado)
            ORDER BY p.id
            """)
    List<Poliza> buscar(@Param("tipo") TipoPoliza tipo, @Param("estado") EstadoPoliza estado);
}
