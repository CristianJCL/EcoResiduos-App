@echo off
setlocal EnableExtensions
chcp 65001 >nul
cd /d "%~dp0"
title EcoResiduos - Inicio automatico
color 0A

echo.
echo ============================================================
echo              ECORESIDUOS - INICIO AUTOMATICO
echo ============================================================
echo.
echo El asistente prepara Android Studio, el emulador y la app.
echo.

if not exist "%~dp0PREPARAR_ECORESIDUOS.ps1" (
    echo ERROR: Falta PREPARAR_ECORESIDUOS.ps1.
    echo Extrae el ZIP completo en una misma carpeta.
    goto ERROR
)
where powershell.exe >nul 2>&1
if errorlevel 1 (
    echo ERROR: No se encontro PowerShell de Windows.
    goto ERROR
)
powershell.exe -NoProfile -ExecutionPolicy Bypass -File "%~dp0PREPARAR_ECORESIDUOS.ps1"
if errorlevel 1 goto ERROR

echo.
echo EcoResiduos ya debe estar abierto. Puedes cerrar esta ventana.
pause
exit /b 0

:ERROR
echo.
echo No se pudo terminar la preparacion. Lee el error mostrado.
echo No se borraron tus registros ni el codigo.
pause
exit /b 1
