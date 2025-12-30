@echo off
echo ========================================
echo LAITOXX Android APK Installer
echo ========================================
echo.

REM Проверяем наличие APK
if not exist "app\build\outputs\apk\debug\app-debug.apk" (
    echo ERROR: APK not found!
    echo Please build the APK first using build_apk.bat
    echo.
    pause
    exit /b 1
)

echo APK file found: app\build\outputs\apk\debug\app-debug.apk
echo.

REM Проверяем подключение устройства
echo Checking for connected devices...
adb devices
echo.

echo Installing APK...
adb install -r app\build\outputs\apk\debug\app-debug.apk

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo APK INSTALLED SUCCESSFULLY!
    echo ========================================
    echo.
    echo You can now:
    echo 1. Open the app on your device
    echo 2. Run show_logs.bat to see real-time logs
    echo.
) else (
    echo.
    echo ========================================
    echo INSTALLATION FAILED!
    echo ========================================
    echo.
    echo Make sure:
    echo 1. USB debugging is enabled
    echo 2. Device is connected via ADB
    echo 3. You have accepted USB debugging permission
    echo.
)

pause
