@echo off
setlocal
chcp 65001 >nul

:: -------------------------------
:: Configurar variables
:: -------------------------------
set "APP_NAME=Viajes Doñana - Almonte"
set "INPUT_DIR=launcher"
set "MAIN_JAR=ViajesDonana-1.0.jar"
set "MAIN_CLASS=ayuntamiento.viajes.app.App"
set "MODULE_PATH=launcher/javafx"
set "MODULES=javafx.controls,javafx.fxml,javafx.web"
set "OUTPUT_DIR=dist"
set "ICON_WIN=launcher/Almonte.ico"
set "APP_VERSION=1.0"
set "VENDOR=Ayuntamiento de Almonte"
set "COPYRIGHT=© 2025 Ayuntamiento de Almonte - Informatica Alcaldia. Todos los derechos reservados."
set "DESCRIPTION=Viajes Doñana"
set "UUID=bef8bda6-81cb-47f3-8413-1c278744a73a"

:: -------------------------------
:: Verificar que jpackage está disponible
:: -------------------------------
where jpackage >nul 2>&1
if errorlevel 1 (
    echo Error: jpackage no se encuentra en el PATH.
    echo Asegúrate de tener JDK 17+ instalado y jpackage accesible.
    pause
    exit /b 1
)

:: -------------------------------
:: Verificar que el JAR principal existe
:: -------------------------------
if not exist "%INPUT_DIR%\%MAIN_JAR%" (
    echo Error: No se encontro "%MAIN_JAR%" en "%INPUT_DIR%".
    pause
    exit /b 1
)

:: -------------------------------
:: Generar instalador para Windows (.exe)
:: -------------------------------
echo Generando instalador para Windows...
jpackage ^
    --name "%APP_NAME%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --type exe ^
    --module-path "%MODULE_PATH%" ^
    --add-modules "%MODULES%" ^
    --dest "%OUTPUT_DIR%" ^
    --app-version "%APP_VERSION%" ^
    --vendor "%VENDOR%" ^
    --icon "%ICON_WIN%" ^
    --copyright "%COPYRIGHT%" ^
    --description "%DESCRIPTION%" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "Ayuntamiento" ^
    --win-dir-chooser ^
    --win-upgrade-uuid "%UUID%"

:: -------------------------------
:: Mensaje final
:: -------------------------------
if %errorlevel% neq 0 (
    echo Error: jpackage fallo al generar el instalador.
) else (
    echo Paquetes creados en "%OUTPUT_DIR%"
)
pause
endlocal





