@echo off
TITLE SERVIDOR DE BASE DE DATOS (Puerto 1099)
echo Arrancando Base de Datos...
echo IMPORTANTE: No cierres esta ventana.
echo ---------------------------------------

:: Classpath: Necesitamos el proyecto Comun (interfaces/Trino) y el proyecto BD
java -cp SD_Practica_Comun/bin;SD_Practica_BD/bin bd.Basededatos

pause