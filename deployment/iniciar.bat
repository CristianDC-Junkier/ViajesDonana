@echo off
setlocal EnableDelayedExpansion

:: Configurar variables
set JAR_NAME=launcher/VehiculosAyuntamiento-1.0.jar
set MAIN_CLASS=ayuntamiento.vehiculos.app.App
set LIB_DIR=libs
set JAVAFX_DIR=launcher/javafx
set MODULES=javafx.controls,javafx.fxml

:: Verificar que el JAR existe
if not exist "%JAR_NAME%" (
    echo Error: No se encontró %JAR_NAME%.
    echo Asegúrate de haber construido el proyecto antes de ejecutar este script.
    pause
    exit /b 1
)

:: Construir el classpath con todas las librerías en "libs"
set CLASSPATH=%JAR_NAME%
for %%i in ("%LIB_DIR%\*.jar") do (
    set CLASSPATH=!CLASSPATH!;%%i
)

:: Ejecutar la aplicación
java ^
    --module-path "%JAVAFX_DIR%" ^
    --add-modules %MODULES% ^
    -cp "%CLASSPATH%" ^
    -jar "%JAR_NAME%"

:: Mantener la ventana abierta
pause
endlocal
