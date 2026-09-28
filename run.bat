@echo off
REM ==============================================================================
REM FinVantage - Personal & Business Finance Management System
REM Launcher Script (Windows)
REM ==============================================================================

TITLE FinVantage - Finance Management System
echo ==================================================
echo  Starting FinVantage (Java Swing + Oracle Database)
echo ==================================================

IF EXIST "target\finvantage-swing-oracle-1.0.0-jar-with-dependencies.jar" (
    java -jar "target\finvantage-swing-oracle-1.0.0-jar-with-dependencies.jar" %*
) ELSE (
    echo [!] Fat JAR not found. Compiling and launching via Maven...
    mvn clean compile exec:java -Dexec.mainClass="com.finvantage.Main"
)

PAUSE
