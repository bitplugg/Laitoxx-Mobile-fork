@echo off
echo ============================================
echo LAITOXX Startup Checker
echo ============================================
echo.
echo This will show ALL logs from app startup
echo.
echo Clearing old logs...
adb logcat -c
echo.
echo Install and start the app NOW, then press any key...
pause
echo.
echo Capturing last 500 lines of log...
adb logcat -d -t 500 > startup_log.txt
echo.
echo ============================================
echo Checking for errors...
echo ============================================
echo.
findstr /i "FATAL" startup_log.txt
echo.
findstr /i "AndroidRuntime" startup_log.txt
echo.
findstr /i "Exception" startup_log.txt | findstr /v "NoClassDefFoundError java.lang.ClassNotFoundException"
echo.
echo ============================================
echo Checking MainActivity...
echo ============================================
findstr /i "MainActivity" startup_log.txt
echo.
echo ============================================
echo Checking PythonBridge...
echo ============================================
findstr /i "PythonBridge" startup_log.txt
echo.
echo ============================================
echo Checking Theme...
echo ============================================
findstr /i "Theme" startup_log.txt
echo.
echo Full log saved to: startup_log.txt
echo.
echo Open startup_log.txt to see complete output
echo.
pause
