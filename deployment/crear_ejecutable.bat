@echo off
setlocal

:: Configurar variables
set APP_NAME=Gestor de Flota - Almonte
set INPUT_DIR=launcher
set MAIN_JAR=VehiculosAyuntamiento-1.0.jar
set MAIN_CLASS=ayuntamiento.vehiculos.app.App
set MODULE_PATH=launcher/javafx
set MODULES=javafx.controls,javafx.fxml,java.sql
set OUTPUT_DIR=dist
set ICON_WIN=launcher\Almonte.ico

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
    --description "Proyecto Vehiculos" ^
    --win-shortcut ^
    --win-menu ^
    --win-menu-group "Ayuntamiento" ^
    --win-dir-chooser ^
    --win-upgrade-uuid "4e7f9d60-d535-41ea-a77f-b9bdc566045c"


:: Generar instalador para macOS (.dmg)
echo Generando instalador para macOS...
jpackage ^
    --name "%APP_NAME%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%MAIN_JAR%" ^
    --main-class "%MAIN_CLASS%" ^
    --type dmg ^
    --module-path "%MODULE_PATH%" ^
    --add-modules "%MODULES%" ^
    --dest "%OUTPUT_DIR%" ^
    --icon "%ICON_MAC%" ^
    --app-version "2.0" ^
    --vendor "Ayuntamiento de Almonte" ^
    --copyright "© 2025 Ayuntamiento de Almonte - Cristian Delgado Cruz. Todos los derechos reservados." ^
    --description "Proyecto Archivero: Gestor de archivos de expedientes" ^
    --win-upgrade-uuid "bb1dd4c4-4186-41ad-9398-694496c3efb1" ^
    --resource-dir "%APPDATA_DIR%" 

echo.
echo Paquetes creados en %OUTPUT_DIR%
pause
endlocal






