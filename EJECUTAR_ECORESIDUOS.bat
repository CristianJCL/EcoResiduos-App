@echo off
setlocal EnableExtensions EnableDelayedExpansion
chcp 65001 >nul
title EcoResiduos - Inicio rapido
cd /d "%~dp0"

echo =============================================
echo   ECORESIDUOS - INICIO RAPIDO PARA REVISION
echo =============================================
echo.

rem Primero localiza las herramientas que instala Android Studio.
set "SDK=%LOCALAPPDATA%\Android\Sdk"
if defined ANDROID_SDK_ROOT set "SDK=%ANDROID_SDK_ROOT%"
if defined ANDROID_HOME set "SDK=%ANDROID_HOME%"
set "ADB=%SDK%\platform-tools\adb.exe"
set "EMU=%SDK%\emulator\emulator.exe"
if not exist "%ADB%" (
    where adb.exe >nul 2>&1
    if not errorlevel 1 set "ADB=adb.exe"
)
if not exist "%SDK%\emulator\emulator.exe" (
    where emulator.exe >nul 2>&1
    if not errorlevel 1 set "EMU=emulator.exe"
)
where "%ADB%" >nul 2>&1
if errorlevel 1 (
    if not exist "%ADB%" (
        echo ERROR: No encontre ADB.
        echo Abre Android Studio una vez e instala Android SDK Platform-Tools.
        goto FAIL
    )
)

rem Usa un telefono con depuracion USB o un emulador ya iniciado.
set "LISTA=%TEMP%\eco_devices_%RANDOM%.txt"
"%ADB%" devices > "%LISTA%" 2>nul
set "DEVICE="
for /f "usebackq skip=1 tokens=1,2" %%D in ("%LISTA%") do (
    if "%%E"=="device" if not defined DEVICE set "DEVICE=%%D"
)
if defined DEVICE goto WAIT_BOOT

rem Si no hay dispositivo, abre automaticamente el primer emulador existente.
if not exist "%EMU%" (
    where emulator.exe >nul 2>&1
    if errorlevel 1 (
        echo No hay telefono conectado ni emulador activo.
        echo Conecta tu telefono con depuracion USB o abre un emulador desde Android Studio.
        goto FAIL
    )
)
set "AVDS=%TEMP%\eco_avds_%RANDOM%.txt"
"%EMU%" -list-avds > "%AVDS%" 2>nul
set "AVD="
for /f "usebackq delims=" %%V in ("%AVDS%") do if not defined AVD set "AVD=%%V"
del "%AVDS%" >nul 2>&1
if not defined AVD (
    echo No hay emuladores creados.
    echo En Android Studio crea uno en Device Manager y ejecuta de nuevo este archivo.
    goto FAIL
)
echo Abriendo emulador: !AVD!
start "" "%EMU%" -avd "!AVD!"
for /l %%I in (1,1,90) do (
    "%ADB%" devices > "%LISTA%" 2>nul
    for /f "usebackq skip=1 tokens=1,2" %%D in ("%LISTA%") do (
        if "%%E"=="device" if not defined DEVICE set "DEVICE=%%D"
    )
    if defined DEVICE goto WAIT_BOOT
    timeout /t 2 /nobreak >nul
)
echo El emulador no aparecio como dispositivo Android.
goto FAIL

:WAIT_BOOT
echo Dispositivo encontrado: %DEVICE%
for /l %%I in (1,1,90) do (
    "%ADB%" -s "%DEVICE%" shell getprop sys.boot_completed > "%LISTA%" 2>nul
    set "READY="
    for /f "usebackq delims=" %%B in ("%LISTA%") do set "READY=%%B"
    if "!READY!"=="1" goto FIND_APK
    timeout /t 2 /nobreak >nul
)
echo El dispositivo no termino de iniciar.
goto FAIL

:FIND_APK
rem Acepta un APK descargado o el APK generado por Gradle.
set "APK=%~dp0EcoResiduos.apk"
if exist "%APK%" goto INSTALL
set "APK=%~dp0app-debug.apk"
if exist "%APK%" goto INSTALL
set "APK=%~dp0app\build\outputs\apk\debug\app-debug.apk"
if exist "%APK%" goto INSTALL
if not exist "%~dp0gradlew.bat" (
    echo No encontre un APK ni el proyecto Android completo.
    echo Copia EcoResiduos.apk en esta carpeta o usa este BAT desde la raiz del repositorio.
    goto FAIL
)
echo Compilando el APK por primera vez con Gradle...
echo Esto puede necesitar Internet y el SDK de Android.
call "%~dp0gradlew.bat" assembleDebug
if errorlevel 1 (
    echo No se pudo compilar. Abre el proyecto en Android Studio para instalar SDK y Gradle.
    goto FAIL
)
if not exist "%APK%" (
    echo Gradle termino sin crear el APK esperado.
    goto FAIL
)

:INSTALL
echo Instalando %APK% ...
"%ADB%" -s "%DEVICE%" install -r "%APK%"
if errorlevel 1 (
    echo No se pudo instalar el APK. Revisa espacio, depuracion USB o compatibilidad Android.
    goto FAIL
)
echo Abriendo EcoResiduos...
"%ADB%" -s "%DEVICE%" shell am start -n com.cristian.ecoresiduos/.MainActivity
if errorlevel 1 goto FAIL
echo.
echo LISTO. EcoResiduos se abrio en el dispositivo.
del "%LISTA%" >nul 2>&1
pause
exit /b 0

:FAIL
del "%LISTA%" >nul 2>&1
echo.
echo No se modificaron tus registros ni el codigo del proyecto.
pause
exit /b 1
