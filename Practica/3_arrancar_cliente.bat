@echo off
TITLE CLIENTE DE MICROBLOGGING
echo Iniciando Cliente...
echo ---------------------------------------

:: Classpath: Necesitamos Comun y Cliente
java -cp SD_Practica_Comun/bin;SD_Practica_Cliente/bin cliente.Usuario

pause