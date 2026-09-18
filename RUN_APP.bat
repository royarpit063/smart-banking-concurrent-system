@echo off
title Smart Banking System
color 0B
cls
echo =======================================================================
echo          STARTING SMART BANKING AND CONCURRENT TRANSACTION SYSTEM        
echo =======================================================================
echo.
echo [1/2] Checking and compiling latest source code...

if not exist "out" mkdir out
javac -cp ".;lib/*" -d out src\com\vityarthi\banking\model\*.java src\com\vityarthi\banking\exception\*.java src\com\vityarthi\banking\config\*.java src\com\vityarthi\banking\dao\*.java src\com\vityarthi\banking\service\*.java src\com\vityarthi\banking\util\*.java src\com\vityarthi\banking\Main.java src\test\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed! Please check the code for syntax errors.
    pause
    exit /b %ERRORLEVEL%
)

echo [2/2] Launching application...
echo =======================================================================
echo.
java -cp "out;lib/*" com.vityarthi.banking.Main
echo.
echo Application closed.
pause
