@echo off
setlocal
chcp 65001

:: Configurar variables
set APP_NAME=Viajes Doñana - Almonte
set INPUT_DIR=launcher
set MAIN_JAR=ViajesDonana-1.0.jar
set MAIN_CLASS=ayuntamiento.viajes.app.App
set MODULE_PATH=launcher/javafx
set MODULES=javafx.controls,javafx.fxml,javafx.web
set OUTPUT_DIR=dist
set ICON_WIN=launcher/Almonte.ico

:: Generar instalador para Windows (.exe)
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
    --app-version "1.0" ^
    --vendor "Ayuntamiento de Almonte" ^
    --icon "%ICON_WIN%" ^
    --copyright "© 2025 Ayuntamiento de Almonte - Informatica Alcaldia. Todos los derechos reservados." ^
    --description "Viajes Doñana" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "Ayuntamiento" ^
    --win-dir-chooser ^
    --win-upgrade-uuid "bef8bda6-81cb-47f3-8413-1c278744a73a"

echo.
echo Paquetes creados en %OUTPUT_DIR%
pause
endlocal






