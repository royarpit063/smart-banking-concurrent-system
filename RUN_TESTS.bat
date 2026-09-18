@echo off
title Banking System Test Suite
color 0A
cls
echo =======================================================================
echo              RUNNING AUTOMATED TEST SUITE AND CONCURRENCY TEST           
echo =======================================================================
echo.
if not exist "out" mkdir out
javac -cp ".;lib/*" -d out src\com\vityarthi\banking\model\*.java src\com\vityarthi\banking\exception\*.java src\com\vityarthi\banking\config\*.java src\com\vityarthi\banking\dao\*.java src\com\vityarthi\banking\service\*.java src\com\vityarthi\banking\util\*.java src\com\vityarthi\banking\Main.java src\test\*.java

java -cp "out;lib/*" test.BankSystemTest
echo.
pause
