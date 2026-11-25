# SysVentas_Ventas con PostgreSQL

1. En DBeaver, selecciona la conexión de PostgreSQL que usarás para el sistema.
2. Abre y ejecuta `sysventas_ventas_schema.sql`. El script crea el esquema aislado `sysventas_ventas`; no elimina ni modifica tablas de otros esquemas.
3. En IntelliJ, crea una configuración Maven con `javafx:run` y configura estas variables de entorno:

   - `SYSVENTAS_DB_URL=jdbc:postgresql://HOST:PUERTO/BASE?currentSchema=sysventas_ventas&sslmode=require`
   - `SYSVENTAS_DB_USER=USUARIO`
   - `SYSVENTAS_DB_PASSWORD=CONTRASEÑA`

   Usa host, puerto, base y usuario de la conexión de DBeaver. Escribe la contraseña localmente y no la agregues al código ni al repositorio.
4. Ejecuta la aplicación. Los repositorios usan el esquema `sysventas_ventas`.

El DDL conserva las tablas y relaciones del modelo Architect y añade algunos campos que la aplicación actual necesita (por ejemplo, credenciales del usuario, datos del cliente y stock/libre). Varios tipos de ID se normalizan a `BIGINT` para que las claves foráneas coincidan entre tablas.
