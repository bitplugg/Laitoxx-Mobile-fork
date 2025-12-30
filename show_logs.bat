@echo off
echo ========================================
echo LAITOXX Android Debug Logs
echo ========================================
echo.
echo Clearing old logs...
adb logcat -c
echo.
echo Starting logcat filter for LAITOXX app...
echo Press Ctrl+C to stop
echo.
echo ========================================
echo.
adb logcat | findstr /i "laitoxx CrashHandler MainActivity Python com.laitoxx.security AndroidRuntime FATAL"
