-- Datos de ejemplo que se cargan al iniciar la aplicacion

INSERT INTO polizas (numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES ('POL-0001', 'INDIVIDUAL', 'ACTIVA', 'Carlos Pérez', '2026-01-01', '2026-12-31', 12, 1500000, 18000000);

INSERT INTO polizas (numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES ('POL-0002', 'COLECTIVA', 'ACTIVA', 'Inmobiliaria Los Andes S.A.S.', '2026-03-01', '2027-02-28', 12, 4300000, 51600000);

INSERT INTO polizas (numero, tipo, estado, tomador, fecha_inicio, fecha_fin, meses_vigencia, canon_mensual, prima)
VALUES ('POL-0003', 'INDIVIDUAL', 'CANCELADA', 'Laura Gómez', '2025-06-01', '2026-05-31', 12, 1200000, 14400000);

INSERT INTO riesgos (poliza_id, direccion_inmueble, arrendatario, arrendador, canon_mensual, prima, estado)
VALUES (1, 'Calle 45 # 12-30, Bogotá', 'Carlos Pérez', 'Ana Rodríguez', 1500000, 18000000, 'ACTIVO');

INSERT INTO riesgos (poliza_id, direccion_inmueble, arrendatario, arrendador, canon_mensual, prima, estado)
VALUES (2, 'Carrera 7 # 72-10 Apto 301, Bogotá', 'Juan Martínez', 'Inversiones Torres S.A.S.', 1800000, 21600000, 'ACTIVO');

INSERT INTO riesgos (poliza_id, direccion_inmueble, arrendatario, arrendador, canon_mensual, prima, estado)
VALUES (2, 'Avenida 19 # 120-45 Apto 802, Bogotá', 'María López', 'Pedro Sánchez', 2500000, 30000000, 'ACTIVO');

INSERT INTO riesgos (poliza_id, direccion_inmueble, arrendatario, arrendador, canon_mensual, prima, estado)
VALUES (3, 'Calle 100 # 15-20, Bogotá', 'Laura Gómez', 'Jorge Ramírez', 1200000, 14400000, 'CANCELADO');
