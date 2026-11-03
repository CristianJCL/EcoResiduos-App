# EcoResiduos - preparador para Windows 10/11 (PowerShell 5.1+).
# Usa el SDK oficial de Google y un APK ya compilado incluido en el ZIP.
param([string]$Carpeta = $PSScriptRoot)

$ErrorActionPreference = 'Stop'
$ProgressPreference = 'SilentlyContinue'
$PackageId = 'com.cristian.ecoresiduos'
$AvdName = 'EcoResiduos_Clase_API35'
$SystemImage = 'system-images;android-35;google_apis;x86_64'
if ($env:PROCESSOR_ARCHITECTURE -eq 'ARM64') {
    $SystemImage = 'system-images;android-35;google_apis;arm64-v8a'
}

function Paso([string]$Mensaje) {
    Write-Host "`n>> $Mensaje" -ForegroundColor Cyan
}
function ErrorClaro([string]$Mensaje) {
    throw $Mensaje
}
function RutaJava {
    $candidatas = @()
    if ($env:JAVA_HOME) { $candidatas += (Join-Path $env:JAVA_HOME 'bin\java.exe') }
    if ($env:ProgramFiles) { $candidatas += (Join-Path $env:ProgramFiles 'Android\Android Studio\jbr\bin\java.exe') }
    if ($env:LOCALAPPDATA) {
        $candidatas += (Join-Path $env:LOCALAPPDATA 'Programs\Android Studio\jbr\bin\java.exe')
        $candidatas += (Join-Path $env:LOCALAPPDATA 'Android Studio\jbr\bin\java.exe')
    }
    foreach ($ruta in $candidatas) {
        if (Test-Path -LiteralPath $ruta) { return $ruta }
    }
    $java = Get-Command 'java.exe' -ErrorAction SilentlyContinue
    if ($java) { return $java.Source }
    return $null
}
function EncontrarSdk {
    $candidatos = @()
    if ($env:ANDROID_HOME) { $candidatos += $env:ANDROID_HOME }
    if ($env:ANDROID_SDK_ROOT) { $candidatos += $env:ANDROID_SDK_ROOT }
    if ($env:LOCALAPPDATA) { $candidatos += (Join-Path $env:LOCALAPPDATA 'Android\Sdk') }
    if ($env:USERPROFILE) { $candidatos += (Join-Path $env:USERPROFILE 'AppData\Local\Android\Sdk') }
    $localProperties = Join-Path $Carpeta 'local.properties'
    if (Test-Path $localProperties) {
        $linea = Get-Content $localProperties | Where-Object { $_ -match '^sdk\.dir=' } | Select-Object -First 1
        if ($linea) { $candidatos += ($linea -replace '^sdk\.dir=', '' -replace '\\\\', '\') }
    }
    foreach ($dir in $candidatos) {
        if ($dir -and (Test-Path -LiteralPath $dir)) { return (Resolve-Path -LiteralPath $dir).Path }
    }
    if ($env:LOCALAPPDATA) { return (Join-Path $env:LOCALAPPDATA 'Android\Sdk') }
    ErrorClaro 'No pude localizar la carpeta de usuario de Windows (LOCALAPPDATA).'
}
function EncontrarSdkManager([string]$Sdk) {
    $base = Join-Path $Sdk 'cmdline-tools'
    $prioridad = Join-Path $base 'latest\bin\sdkmanager.bat'
    if (Test-Path $prioridad) { return $prioridad }
    if (Test-Path $base) {
        $opciones = @(Get-ChildItem -Path $base -Filter 'sdkmanager.bat' -Recurse -ErrorAction SilentlyContinue |
            Where-Object { $_.FullName -match '\\bin\\sdkmanager\.bat$' } |
            Sort-Object FullName -Descending)
        if ($opciones.Count -gt 0) { return $opciones[0].FullName }
    }
    return $null
}
function InstalarHerramientasComandos([string]$Sdk) {
    $url = 'https://dl.google.com/android/repository/commandlinetools-win-15859902_latest.zip'
    $shaEsperado = '90ae805d20434428bffcb699c290860f19bb5f66a67e6b330067e3de801fb04a'
    $temporal = Join-Path $env:TEMP ('ecoresiduos_sdk_' + [Guid]::NewGuid().ToString('N'))
    New-Item -ItemType Directory -Path $temporal -Force | Out-Null
    try {
        Paso 'Descargando herramientas oficiales de Google: primer uso.'
        Write-Host 'Se necesita Internet. El paquete supera 150 MB.'
        $archivo = Join-Path $temporal 'herramientas.zip'
        [Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
        Invoke-WebRequest -Uri $url -OutFile $archivo -UseBasicParsing
        $shaReal = (Get-FileHash -Path $archivo -Algorithm SHA256).Hash.ToLowerInvariant()
        if ($shaReal -ne $shaEsperado) { ErrorClaro 'Descarga de Google con SHA-256 incorrecto. Se canceló por seguridad.' }
        Expand-Archive -LiteralPath $archivo -DestinationPath (Join-Path $temporal 'extraido') -Force
        $origen = Join-Path $temporal 'extraido\cmdline-tools'
        if (!(Test-Path (Join-Path $origen 'bin\sdkmanager.bat'))) {
            ErrorClaro 'No se encontraron las herramientas de Android dentro del paquete.'
        }
        $destinoPadre = Join-Path $Sdk 'cmdline-tools'
        New-Item -ItemType Directory -Path $destinoPadre -Force | Out-Null
        $destino = Join-Path $destinoPadre 'latest'
        if (Test-Path $destino) { ErrorClaro 'La carpeta cmdline-tools\latest está incompleta. Revísala en el SDK Manager.' }
        Move-Item -LiteralPath $origen -Destination $destino
        return (Join-Path $destino 'bin\sdkmanager.bat')
    } finally {
        Remove-Item -LiteralPath $temporal -Recurse -Force -ErrorAction SilentlyContinue
    }
}
function Licencias([string]$Manager, [string]$Sdk) {
    Paso 'Aceptando licencias del SDK de Android para el emulador.'
    $respuestas = @(1..60 | ForEach-Object { 'y' })
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { $respuestas | & $Manager "--sdk_root=$Sdk" '--licenses' 2>&1 | Out-Host }
    finally { $ErrorActionPreference = $preferencia }
    if ($LASTEXITCODE -ne 0) { ErrorClaro 'No se pudieron aceptar las licencias del SDK. Revisa Internet o el SDK Manager.' }
}
function InstalarPaquetes([string]$Manager, [string]$Sdk, [string[]]$Paquetes) {
    if ($Paquetes.Count -eq 0) { return }
    Paso ('Instalando componentes oficiales: ' + ($Paquetes -join ', '))
    $preferencia = $ErrorActionPreference
    $ErrorActionPreference = 'Continue'
    try { & $Manager "--sdk_root=$Sdk" @Paquetes 2>&1 | Out-Host }
    finally { $ErrorActionPreference = $preferencia }
    if ($LASTEXITCODE -ne 0) { ErrorClaro 'No se pudo instalar el SDK. Revisa Internet, espacio libre y el mensaje anterior.' }
}
function DispositivoListo([string]$Adb) {
    $salida = @(& $Adb devices 2>$null)
    foreach ($linea in $salida) {
        if ($linea -match '^(\S+)\s+device(?:\s|$)') { return $Matches[1] }
    }
    return $null
}
function EsperarInicio([string]$Adb, [string]$Serial) {
    Paso 'Esperando a que Android inicie.'
    for ($i=0; $i -lt 200; $i++) {
        $valor = @(& $Adb '-s' $Serial 'shell' 'getprop' 'sys.boot_completed' 2>$null)
        if (($valor -join '').Trim() -eq '1') { return }
        if (($i % 12) -eq 0) { Write-Host 'Iniciando Android...' }
        Start-Sleep -Seconds 3
    }
    ErrorClaro 'Android no pudo iniciar. Revisa RAM, espacio y virtualización en Android Studio > Device Manager.'
}
try {
    Paso 'Comprobando Android Studio y el APK.'
    $Carpeta = [IO.Path]::GetFullPath($Carpeta)
    $apk = Join-Path $Carpeta 'EcoResiduos.apk'
    $java = RutaJava
    if (!$java) { ErrorClaro 'No se encontró Java. Abre Android Studio para completar su instalación.' }
    $env:JAVA_HOME = Split-Path (Split-Path $java -Parent) -Parent
    $env:PATH = (Join-Path $env:JAVA_HOME 'bin') + ';' + $env:PATH

    $sdk = EncontrarSdk
    New-Item -ItemType Directory -Path $sdk -Force | Out-Null
    $env:ANDROID_HOME = $sdk
    $env:ANDROID_SDK_ROOT = $sdk
    Write-Host "SDK: $sdk"
    $adb = Join-Path $sdk 'platform-tools\adb.exe'
    $emulator = Join-Path $sdk 'emulator\emulator.exe'
    $manager = EncontrarSdkManager $sdk

    if (!(Test-Path $apk)) {
        $apkGenerado = Join-Path $Carpeta 'app\build\outputs\apk\debug\app-debug.apk'
        if (Test-Path $apkGenerado) { $apk = $apkGenerado }
        elseif (Test-Path (Join-Path $Carpeta 'gradlew.bat')) {
            Paso 'Compilando APK desde el código fuente por primera vez.'
            & (Join-Path $Carpeta 'gradlew.bat') '-p' $Carpeta 'assembleDebug' 2>&1 | Out-Host
            if ($LASTEXITCODE -ne 0 -or !(Test-Path $apkGenerado)) {
                ErrorClaro 'Gradle falló. Descarga el paquete que ya incluye EcoResiduos.apk.'
            }
            $apk = $apkGenerado
        }
        else { ErrorClaro 'No se encontró EcoResiduos.apk. Descomprime el ZIP COMPLETO junto con este BAT y el PS1.' }
    }
    Write-Host "Aplicación encontrada: $apk"

    if (!(Test-Path $adb)) {
        if (!$manager) { $manager = InstalarHerramientasComandos $sdk }
        Licencias $manager $sdk
        InstalarPaquetes $manager $sdk @('platform-tools')
    }
    if (!(Test-Path $adb)) { ErrorClaro 'No se instaló adb.exe. Abre Android Studio > SDK Manager.' }
    & $adb 'start-server' | Out-Host

    $serial = DispositivoListo $adb
    if ($serial) {
        Write-Host "Teléfono o emulador disponible: $serial"
    } else {
        if (!(Test-Path $emulator)) {
            if (!$manager) { $manager = InstalarHerramientasComandos $sdk }
            Licencias $manager $sdk
            InstalarPaquetes $manager $sdk @('emulator')
        }
        if (!(Test-Path $emulator)) { ErrorClaro 'No se pudo instalar Android Emulator.' }

        $avds = @(& $emulator '-list-avds' 2>$null | Where-Object { $_ -and $_.Trim() })
        $avdElegido = $null
        if ($avds -contains $AvdName) { $avdElegido = $AvdName }
        elseif ($avds.Count -gt 0) { $avdElegido = [string]$avds[0] }

        if (!$avdElegido) {
            if (!$manager) { $manager = InstalarHerramientasComandos $sdk }
            $avdManager = Join-Path (Split-Path (Split-Path $manager -Parent) -Parent) 'bin\avdmanager.bat'
            if (!(Test-Path $avdManager)) { ErrorClaro 'No se encontró avdmanager.bat en el SDK.' }
            Licencias $manager $sdk
            InstalarPaquetes $manager $sdk @('platform-tools','emulator',$SystemImage)
            Paso 'Creando teléfono Android virtual.'
            $preferencia = $ErrorActionPreference
            $ErrorActionPreference = 'Continue'
            try { 'no' | & $avdManager 'create' 'avd' '--name' $AvdName '--package' $SystemImage '--force' 2>&1 | Out-Host }
            finally { $ErrorActionPreference = $preferencia }
            if ($LASTEXITCODE -ne 0) { ErrorClaro 'No se pudo crear el emulador. Revisa Android Studio > Device Manager.' }
            $avdElegido = $AvdName
        }

        $chequeo = @(& $emulator '-accel-check' 2>&1)
        $texto = $chequeo -join ' '
        if ($LASTEXITCODE -ne 0 -or $texto -match 'not installed|not usable|not supported|acceleration.*disabled') {
            Write-Host 'AVISO: Si el emulador no inicia, activa Virtualización y Plataforma de hipervisor de Windows (WHPX) y reinicia.' -ForegroundColor Yellow
            Write-Host 'No cambiaré la BIOS ni reiniciaré Windows.'
        }
        Paso "Iniciando emulador: $avdElegido"
        $proceso = Start-Process -FilePath $emulator -ArgumentList @('-avd', $avdElegido, '-no-boot-anim') -PassThru
        for ($i=0; $i -lt 150; $i++) {
            $serial = DispositivoListo $adb
            if ($serial) { break }
            if ($proceso.HasExited) { ErrorClaro 'El emulador se cerró. Activa la virtualización o prueba Device Manager.' }
            if (($i % 12) -eq 0) { Write-Host 'Preparando emulador...' }
            Start-Sleep -Seconds 3
        }
        if (!$serial) { ErrorClaro 'ADB no detectó un emulador. Revisa Android Studio > Device Manager.' }
    }

    EsperarInicio $adb $serial
    Paso "Instalando EcoResiduos en $serial (sin borrar registros anteriores)."
    & $adb '-s' $serial 'install' '-r' $apk 2>&1 | Out-Host
    if ($LASTEXITCODE -ne 0) { ErrorClaro 'No se instaló el APK. Revisa almacenamiento, versión Android o firmas de otra instalación.' }

    Paso 'Abriendo EcoResiduos.'
    & $adb '-s' $serial 'shell' 'am' 'start' '-n' "$PackageId/.MainActivity" 2>&1 | Out-Host
    if ($LASTEXITCODE -ne 0) { ErrorClaro 'No se pudo abrir EcoResiduos. Revisa el mensaje de ADB.' }
    Write-Host "`nLISTO: EcoResiduos se instaló y abrió." -ForegroundColor Green
    Write-Host 'En próximos usos, el BAT reutiliza el SDK y emulador disponibles.'
    exit 0
} catch {
    Write-Host "`nERROR: $($_.Exception.Message)" -ForegroundColor Red
    Write-Host 'No se borraron los registros ni el código del proyecto.'
    exit 1
}
