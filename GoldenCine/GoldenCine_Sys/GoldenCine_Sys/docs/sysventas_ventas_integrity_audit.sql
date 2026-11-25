-- Run in DBeaver against the database containing schema sysventas_ventas.
-- Every orphan_count must be 0. Foreign keys should also be present/enabled.
SELECT 'sala.id_tipo_sala -> tipo_sala.id_tipo_sala' AS relation, COUNT(*) AS orphan_count
FROM sysventas_ventas.sala c WHERE c.id_tipo_sala IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.tipo_sala p WHERE p.id_tipo_sala = c.id_tipo_sala)
UNION ALL
SELECT 'peliculas.id_genero -> genero.id_genero', COUNT(*)
FROM sysventas_ventas.peliculas c WHERE c.id_genero IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.genero p WHERE p.id_genero = c.id_genero)
UNION ALL
SELECT 'cartelera.idpelicula -> peliculas.id_peliculas', COUNT(*)
FROM sysventas_ventas.cartelera c WHERE c.idpelicula IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.peliculas p WHERE p.id_peliculas = c.idpelicula)
UNION ALL
SELECT 'cartelera.idsala -> sala.idsala', COUNT(*)
FROM sysventas_ventas.cartelera c WHERE c.idsala IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.sala p WHERE p.idsala = c.idsala)
UNION ALL
SELECT 'cartelera.id_usuario -> usuario.id_usuario', COUNT(*)
FROM sysventas_ventas.cartelera c WHERE c.id_usuario IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.usuario p WHERE p.id_usuario = c.id_usuario)
UNION ALL
SELECT 'promociones.id_servicio -> cartelera.id_servicio', COUNT(*)
FROM sysventas_ventas.promociones c WHERE c.id_servicio IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.cartelera p WHERE p.id_servicio = c.id_servicio)
UNION ALL
SELECT 'venta.id_cliente -> cliente.id_cliente', COUNT(*)
FROM sysventas_ventas.venta c WHERE c.id_cliente IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.cliente p WHERE p.id_cliente = c.id_cliente)
UNION ALL
SELECT 'venta.id_usuario -> usuario.id_usuario', COUNT(*)
FROM sysventas_ventas.venta c WHERE c.id_usuario IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.usuario p WHERE p.id_usuario = c.id_usuario)
UNION ALL
SELECT 'venta_detalle_service.id_venta -> venta.id_venta', COUNT(*)
FROM sysventas_ventas.venta_detalle_service c WHERE c.id_venta IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.venta p WHERE p.id_venta = c.id_venta)
UNION ALL
SELECT 'venta_detalle_service.id_servicio -> cartelera.id_servicio', COUNT(*)
FROM sysventas_ventas.venta_detalle_service c WHERE c.id_servicio IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.cartelera p WHERE p.id_servicio = c.id_servicio)
UNION ALL
SELECT 'venta_detalle_product.id_venta -> venta.id_venta', COUNT(*)
FROM sysventas_ventas.venta_detalle_product c WHERE c.id_venta IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.venta p WHERE p.id_venta = c.id_venta)
UNION ALL
SELECT 'venta_detalle_product.id_producto -> producto.id_producto', COUNT(*)
FROM sysventas_ventas.venta_detalle_product c WHERE c.id_producto IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.producto p WHERE p.id_producto = c.id_producto)
UNION ALL
SELECT 'producto.id_tipo -> tipo.id_tipo', COUNT(*)
FROM sysventas_ventas.producto c WHERE c.id_tipo IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.tipo p WHERE p.id_tipo = c.id_tipo)
UNION ALL
SELECT 'carrito_venta.id_cliente -> cliente.id_cliente', COUNT(*)
FROM sysventas_ventas.carrito_venta c WHERE c.id_cliente IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.cliente p WHERE p.id_cliente = c.id_cliente)
UNION ALL
SELECT 'carrito_venta.id_usuario -> usuario.id_usuario', COUNT(*)
FROM sysventas_ventas.carrito_venta c WHERE c.id_usuario IS NOT NULL
  AND NOT EXISTS (SELECT 1 FROM sysventas_ventas.usuario p WHERE p.id_usuario = c.id_usuario)
ORDER BY relation;

-- Expected required fields must also have zero NULL values.
SELECT 'peliculas.id_genero' AS field, COUNT(*) AS null_count FROM sysventas_ventas.peliculas WHERE id_genero IS NULL
UNION ALL SELECT 'sala.id_tipo_sala', COUNT(*) FROM sysventas_ventas.sala WHERE id_tipo_sala IS NULL
UNION ALL SELECT 'producto.id_tipo', COUNT(*) FROM sysventas_ventas.producto WHERE id_tipo IS NULL
UNION ALL SELECT 'cartelera.tipo_funcion', COUNT(*) FROM sysventas_ventas.cartelera WHERE tipo_funcion IS NULL
ORDER BY field;

-- List actual foreign keys and validation status (expect 15 validated rows).
SELECT conname AS constraint_name,
       conrelid::regclass AS child_table,
       confrelid::regclass AS parent_table,
       convalidated AS validated
FROM pg_constraint
WHERE contype = 'f'
  AND connamespace = 'sysventas_ventas'::regnamespace
ORDER BY child_table, constraint_name;
