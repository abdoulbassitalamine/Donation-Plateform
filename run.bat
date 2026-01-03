@echo off
REM DonoPlatform - Script de compilation et execution (Windows)
REM Usage: run.bat [compile|test|run|clean|package]

setlocal enabledelayedexpansion

echo ================================================
echo   DonoPlatform - Spring Boot Application
echo ================================================
echo.

set ACTION=%1
if "%ACTION%"=="" set ACTION=run

if "%ACTION%"=="compile" goto COMPILE
if "%ACTION%"=="test" goto TEST
if "%ACTION%"=="package" goto PACKAGE
if "%ACTION%"=="run" goto RUN
if "%ACTION%"=="clean" goto CLEAN
if "%ACTION%"=="all" goto ALL
goto USAGE

:COMPILE
echo [1/2] Compilation du projet...
call mvnw.cmd clean compile -DskipTests
if errorlevel 1 goto ERROR
echo [OK] Compilation reussie
echo.
goto END

:TEST
echo [2/2] Execution des tests...
call mvnw.cmd test
if errorlevel 1 goto ERROR
echo [OK] Tests termines
echo.
goto END

:PACKAGE
echo [1/1] Creation du JAR executable...
call mvnw.cmd clean package -DskipTests
if errorlevel 1 goto ERROR
echo [OK] JAR cree : target\donoplatform-0.0.1-SNAPSHOT.jar
echo.
goto SHOW_INFO

:RUN
echo Demarrage de l'application...
echo.
if exist "target\donoplatform-0.0.1-SNAPSHOT.jar" (
    java -jar target\donoplatform-0.0.1-SNAPSHOT.jar
) else (
    echo JAR introuvable, compilation en cours...
    call mvnw.cmd clean package -DskipTests
    java -jar target\donoplatform-0.0.1-SNAPSHOT.jar
)
goto END

:CLEAN
echo Nettoyage du projet...
call mvnw.cmd clean
del /q data\*.db 2>nul
echo [OK] Projet nettoye
echo.
goto END

:ALL
call mvnw.cmd clean compile test package
if errorlevel 1 goto ERROR
echo [OK] Build complet termine
goto END

:SHOW_INFO
echo.
echo Application demarree avec succes!
echo.
echo URLs disponibles :
echo    - Application web : http://localhost:8080
echo    - API JSON : http://localhost:8080 (avec Accept: application/json^)
echo    - H2 Console : http://localhost:8080/h2-console
echo.
echo Base H2 :
echo    - URL JDBC : jdbc:h2:file:./data/donoplatform_db
echo    - Username : sa
echo    - Password : (vide^)
echo.
echo Appuyez sur Ctrl+C pour arreter l'application
goto END

:USAGE
echo Usage: %0 {compile^|test^|package^|run^|clean^|all}
echo.
echo   compile  - Compile le code source
echo   test     - Compile et execute les tests
echo   package  - Cree le JAR executable
echo   run      - Lance l'application (defaut^)
echo   clean    - Nettoie le projet
echo   all      - Compile, teste et package
goto END

:ERROR
echo.
echo [ERREUR] La commande a echoue!
exit /b 1

:END
endlocal