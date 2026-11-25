@echo off
setlocal EnableExtensions
rem ============================================================
rem  Genera GoldenCine.exe a partir del proyecto GoldenCine_Sys
rem  NO modifica ningun archivo del proyecto (solo crea la carpeta target).
rem
rem  Uso:  generar_exe.bat            -> version final (sin consola)
rem        generar_exe.bat consola    -> version de prueba (muestra errores)
rem        generar_exe.bat consola "D:\otra\ruta\GoldenCine_Sys"
rem ============================================================

set "MODO="
if /i "%~1"=="consola" set "MODO=--win-console"

set "PROYECTO=%~2"
if "%PROYECTO%"=="" set "PROYECTO=%~dp0GoldenCine_Sys"
if not exist "%PROYECTO%\pom.xml" (
    echo No encuentro pom.xml en "%PROYECTO%"
    echo Deja este .bat junto a la carpeta GoldenCine_Sys o pasa la ruta como segundo parametro.
    goto :error
)

rem --- Buscar JDK 21 (con jpackage) ---
if defined JAVA_HOME if exist "%JAVA_HOME%\bin\jpackage.exe" goto :java_ok
for /d %%D in ("%USERPROFILE%\.jdks\*21*") do if exist "%%~fD\bin\jpackage.exe" set "JAVA_HOME=%%~fD"
if not exist "%JAVA_HOME%\bin\jpackage.exe" (
    echo No encontre un JDK 21 con jpackage.
    echo Define JAVA_HOME antes de ejecutar, por ejemplo:
    echo    set "JAVA_HOME=C:\Users\TU_USUARIO\.jdks\ms-21.0.13"
    goto :error
)
:java_ok
set "PATH=%JAVA_HOME%\bin;%PATH%"
echo Usando JDK: %JAVA_HOME%

cd /d "%PROYECTO%" || goto :error

echo.
echo [1/4] Compilando el JAR con Maven wrapper...
call mvnw.cmd clean package -DskipTests || goto :error

echo.
echo [2/4] Reuniendo las librerias en target\jpackage-input ...
call mvnw.cmd org.apache.maven.plugins:maven-dependency-plugin:3.6.1:copy-dependencies -DoutputDirectory=target\jpackage-input -DincludeScope=runtime || goto :error
copy /y "target\SysVentas-1.0-SNAPSHOT.jar" "target\jpackage-input\" >nul || goto :error

echo.
echo [3/4] Creando GoldenCine.exe con jpackage...
if exist "target\exe" rmdir /s /q "target\exe"
jpackage --type app-image --name GoldenCine --input target\jpackage-input --main-jar SysVentas-1.0-SNAPSHOT.jar --main-class pe.edu.upeu.sysventas.App --dest target\exe %MODO% || goto :error

echo.
echo [4/4] LISTO:
echo    %PROYECTO%\target\exe\GoldenCine\GoldenCine.exe
echo.
if not defined SYSVENTAS_DB_URL      echo AVISO: falta la variable de entorno SYSVENTAS_DB_URL
if not defined SYSVENTAS_DB_USER     echo AVISO: falta la variable de entorno SYSVENTAS_DB_USER
if not defined SYSVENTAS_DB_PASSWORD echo AVISO: falta la variable de entorno SYSVENTAS_DB_PASSWORD
echo (Sin esas 3 variables el .exe no se conecta a PostgreSQL. Ver paso 5 de las instrucciones.)
pause
exit /b 0

:error
echo.
echo *** Ocurrio un error. Revisa el mensaje de arriba. ***
pause
exit /b 1
