@echo off
setlocal

REM Comprobamos si JAVA_HOME está definido
if "%JAVA_HOME%"=="" (
  echo [ERROR] JAVA_HOME no está definido
  exit /B 1
)

REM Definimos el directorio base del proyecto
set "BASEDIR=%~dp0"

REM Ejecutamos el wrapper de Maven
"%JAVA_HOME%\bin\java" ^
  -Dmaven.multiModuleProjectDirectory="%BASEDIR%" ^
  -classpath "%BASEDIR%\.mvn\wrapper\maven-wrapper.jar" ^
  org.apache.maven.wrapper.MavenWrapperMain %*

exit /B %ERRORLEVEL%
