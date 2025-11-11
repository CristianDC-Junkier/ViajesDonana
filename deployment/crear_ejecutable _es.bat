@echo off
setlocal

:: Configurar variables
set APP_NAME=Viajes Doñana - Almonte
set INPUT_DIR=launcher
set MAIN_JAR=ViajesDonana-1.4.jar
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
    --type app-image ^
    --module-path "%MODULE_PATH%" ^
    --add-modules "%MODULES%" ^
    --dest "%OUTPUT_DIR%" ^
    --app-version "1.3" ^
    --vendor "Ayuntamiento de Almonte" ^
    --icon "%ICON_WIN%" ^
    --copyright "© 2025 Ayuntamiento de Almonte - Informatica Alcaldia. Todos los derechos reservados." ^
    --description "Viajes Doñana"  

echo.
echo Paquetes creados en %OUTPUT_DIR%
	makensis /INPUTCHARSET UTF8 crear_instalador_es.nsi
echo Instalador creados en %OUTPUT_DIR%
pause
endlocal




