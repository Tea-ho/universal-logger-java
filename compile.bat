@echo off
cd /d C:\Users\th\Desktop\testCode\universal-logger-java
mkdir build\temp_classes 2>nul

set CP=C:\Users\th\.m2\repository\org\slf4j\slf4j-api\1.5.6\slf4j-api-1.5.6.jar;C:\Users\th\.m2\repository\ch\qos\logback\logback-classic\1.2.11\logback-classic-1.2.11.jar;build\libs\universal-logger-java-1.0.0.jar

javac -encoding UTF-8 -source 11 -target 11 -cp "%CP%" -d build\temp_classes src\main\java\com\universallogger\sql\SqlCaptureProxy.java

if %ERRORLEVEL% == 0 (
    cd build\temp_classes
    jar uf ..\libs\universal-logger-java-1.0.0.jar com\universallogger\sql\*.class
    cd ..\..
    echo JAR updated successfully!
) else (
    echo Compilation failed!
)
