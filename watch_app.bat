@echo off
echo ========================================
echo LAITOXX App Monitor
echo ========================================
echo.
echo Clearing logcat...
adb logcat -c
echo.
echo Starting live monitor...
echo Watching for: com.laitoxx.security
echo.
echo ========================================
echo.
adb logcat | findstr /i /c:"com.laitoxx.security" /c:"FATAL" /c:"AndroidRuntime" /c:"CrashHandler" /c:"MainActivity" /c:"PythonBridge" /c:"Chaquopy"
