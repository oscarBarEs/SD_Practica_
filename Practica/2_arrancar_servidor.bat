@echo off
TITLE SERVIDOR DE NEGOCIO (Gestor)
echo Arrancando Logica del Servidor...
echo Esperando a que la BD este lista...
echo ---------------------------------------

:: Classpath: Necesitamos Comun, BD (para las interfaces) y Servidor
java -cp SD_Practica_Comun/bin;SD_Practica_BD/bin;SD_Practica_Servidor/bin servidor.Servidor

pause