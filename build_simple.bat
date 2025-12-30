@echo off
set JAVA_HOME=C:\Java\jdk-17
set PATH=C:\Java\jdk-17\bin;%PATH%
gradlew.bat assembleDebug --no-daemon --stacktrace
