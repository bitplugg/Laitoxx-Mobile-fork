@echo off
echo ========================================
echo Getting crash logs from device
echo ========================================
echo.
echo Clearing old logs...
adb logcat -c
echo.
echo Please start the app now and wait for it to crash...
echo Press Ctrl+C when done
echo.
pause
echo.
echo Capturing logs...
adb logcat -d > crash_log.txt
echo.
echo Filtering for errors...
echo.
echo ========================================
echo FATAL ERRORS:
echo ========================================
findstr /i "FATAL" crash_log.txt
echo.
echo ========================================
echo AndroidRuntime:
echo ========================================
findstr /i "AndroidRuntime" crash_log.txt
echo.
echo ========================================
echo CrashHandler:
echo ========================================
findstr /i "CrashHandler" crash_log.txt
echo.
echo ========================================
echo Python errors:
echo ========================================
findstr /i "Python" crash_log.txt
echo.
echo Full log saved to: crash_log.txt
echo.
pause
