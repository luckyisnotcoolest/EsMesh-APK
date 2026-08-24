@rem Gradle wrapper script
@if "%DEBUG%" == "" @echo off
set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set WRAPPER_JAR=%DIRNAME%gradle\wrapper\gradle-wrapper.jar

if exist "%WRAPPER_JAR%" (
    if defined JAVA_HOME (
        set JAVA_EXE="%JAVA_HOME%\bin\java.exe"
    ) else (
        set JAVA_EXE=java.exe
    )
    %JAVA_EXE% -Dorg.gradle.appname=gradlew -classpath "%WRAPPER_JAR%" org.gradle.wrapper.GradleWrapperMain %*
) else (
    gradle %*
)
