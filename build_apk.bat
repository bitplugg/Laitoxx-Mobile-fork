@echo off
echo ========================================
echo LAITOXX Android APK Builder
echo ========================================
echo.

REM Set Java 17
set JAVA_HOME=C:\Java\jdk-17
set PATH=C:\Java\jdk-17\bin;%PATH%

REM Set Python 3.12 (CRITICAL: must be before Python 3.13 in PATH)
set PATH=C:\python312;C:\python312\Scripts;%PATH%

REM Remove Python 3.13 from PATH temporarily
set PATH=%PATH:C:\Users\ShShu\AppData\Local\Programs\Python\Python313\Scripts;=%
set PATH=%PATH:C:\Users\ShShu\AppData\Local\Programs\Python\Python313;=%
set PATH=%PATH:C:\Users\ShShu\AppData\Local\Microsoft\WindowsApps;=%

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
gradlew.bat clean assembleDebug --no-daemon --stacktrace

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

pause
