@echo off
echo ========================================
echo LAITOXX Android APK Builder
echo ========================================
echo.

REM Set Java 17
set JAVA_HOME=C:\Java\jdk-17
set PATH=C:\Java\jdk-17\bin;%PATH%

REM Set Python 3.12
set PATH=C:\python312;%PATH%

echo Checking Java version...
java -version
echo.

echo Checking Python version...
python --version
echo.

echo Starting Gradle build...
echo This may take several minutes on first build...
echo.

REM Clean and build
call gradlew.bat clean assembleDebug --no-daemon --stacktrace

if %ERRORLEVEL% EQU 0 (
    echo.
    echo ========================================
    echo BUILD SUCCESSFUL!
    echo ========================================
    echo.
    echo APK Location:
    echo app\build\outputs\apk\debug\app-debug.apk
    echo.
    echo File size:
    dir app\build\outputs\apk\debug\app-debug.apk | find "app-debug.apk"
    echo.
) else (
    echo.
    echo ========================================
    echo BUILD FAILED!
    echo ========================================
    echo Check errors above
    echo.
)
