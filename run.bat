@echo off
REM DonoPlatform - Script de compilation et execution (Windows)
REM Usage: run.bat [compile|test|run|clean|package]

setlocal enabledelayedexpansion

echo ================================================
echo   DonoPlatform - Spring Boot Application
echo ================================================
echo.

REM Detecter Maven Wrapper ou Maven
if exist "mvnw.cmd" (
    set MVN=mvnw.cmd
    echo [INFO] Utilisation de Maven Wrapper
) else (
    where mvn >nul 2>nul
    if errorlevel 1 (
        echo [ERREUR] Ni mvnw.cmd ni Maven ne sont disponibles!
        echo.
        echo Solutions :
        echo   1. Installez Maven : https://maven.apache.org/download.cgi
        echo   2. Ou ajoutez Maven Wrapper : mvn wrapper:wrapper
        echo.
        pause
        exit /b 1
    )
    set MVN=mvn
    echo [INFO] Utilisation de Maven systeme
)
echo.

set ACTION=%1
if "%ACTION%"=="" set ACTION=run

if "%ACTION%"=="compile" goto COMPILE
if "%ACTION%"=="package" goto PACKAGE
if "%ACTION%"=="run" goto RUN
if "%ACTION%"=="clean" goto CLEAN
if "%ACTION%"=="all" goto ALL
goto USAGE

:COMPILE
echo [1/1] Compilation du projet...
call %MVN% clean compile -DskipTests
if errorlevel 1 goto ERROR
echo [OK] Compilation reussie
echo.
goto END

:PACKAGE
echo [1/1] Creation du JAR executable...
call %MVN% clean package -DskipTests
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
    call %MVN% clean package -DskipTests
    if errorlevel 1 goto ERROR
    java -jar target\donoplatform-0.0.1-SNAPSHOT.jar
)
goto END

:CLEAN
echo Nettoyage du projet...
call %MVN% clean
if not errorlevel 1 (
    if exist "data" (
        del /q data\*.db 2>nul
    )
)
echo [OK] Projet nettoye
echo.
goto END

:ALL
call %MVN% clean compile package -DskipTests
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
echo Usage: %0 {compile^|package^|run^|clean^|all}
echo.
echo   compile  - Compile le code source
echo   package  - Cree le JAR executable
echo   run      - Lance l'application (defaut^)
echo   clean    - Nettoie le projet
echo   all      - Compile et package
goto END

:ERROR
echo.
echo [ERREUR] La commande a echoue!
pause
exit /b 1

:END
endlocal