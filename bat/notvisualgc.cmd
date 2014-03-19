@ECHO OFF

::
:: Batch script to launch the visualgc command.
::

setlocal

if not "%JVMSTAT_HOME%"=="" GOTO CHECKJAVAHOME

:: get the full path name for this script
SET SCRIPT_DIR=%~f0%

:: substitute \bat\<cmd> with null to get the jvmstat home directory
SET JVMSTAT_HOME=%SCRIPT_DIR:\bat\notvisualgc.cmd=%

if "%NVGC_VMARGS%"=="" GOTO NVGCARGS
:NVGCARGS
    set NVGC_VMARGS="-XX:+CMSClassUnloadingEnabled -XX:MaxPermSize=6M -XX:PermSize=5M -Xmx3M"
goto EONVGCARGS
:EONVGCARGS

:CHECKJAVAHOME

if not "%JVMSTAT_JAVA_HOME%"=="" GOTO CHECKJVMSTATJAVAHOME

:: find the location of the java command so we can locate the
:: the tools.jar archive. Note that this code assumes that java.exe
:: is found in a jkd installation directory, not in the windows
:: directory (eg. c:\WINNT\system32\java.exe).

call :which java.exe JAVAEXE

if not "%JAVAEXE%"=="" GOTO CHECK1
echo java.exe not found in path - check PATH
goto :EOF

:CHECK1

call :dirname "%JAVAEXE%" JAVABIN
call :dirname "%JAVABIN%" JVMSTAT_JAVA_HOME

:: check that we are not dealing with a JRE or installed JDK/JRE
if exist "%JVMSTAT_JAVA_HOME%\lib\tools.jar" GOTO CHECK2
goto :ENVERROR
goto :EOF

:CHECK2

:: make sure it's JDK 1.5.0 or later
if exist "%JVMSTAT_JAVA_HOME%\bin\jps.exe" GOTO LAUNCH
goto :ENVERROR
goto :EOF

:: Verify that JVMSTAT_JAVA_HOME is appropriately set

:CHECKJVMSTATJAVAHOME

:: check that we are not dealing with a JRE
if exist "%JVMSTAT_JAVA_HOME%\lib\tools.jar" GOTO CHECK3
goto :JREERROR
goto :EOF

:CHECK3

:: make sure it's JDK 1.5.0 or later
if exist "%JVMSTAT_JAVA_HOME%\bin\jps.exe" GOTO LAUNCH
goto :VERSIONERROR
goto :EOF


:LAUNCH

SET VISUALGC_JAR=%JVMSTAT_HOME%\jars\visualgc.jar
SET TOOLS_JAR=%JVMSTAT_JAVA_HOME%\lib\tools.jar

:: %0 no longer needed, so shift it out to allow one more
:: parameter. %* is available in winnt and later, but doesn't
:: work in earlier windows environemnts.
::
shift

:: Launch the command
::
java "%NVGC_VMARGS%" -Xbootclasspath/p:"%TOOLS_JAR%" -cp "%VISUALGC_JAR%" "com.sun.jvmstat.tools.visualgc.NotVisualGC" %0 %1 %2 %3 %4 %5 %6 %7 %8 %9

GOTO :EOF

:ENVERROR
echo Could not automatically set the JVMSTAT_JAVA_HOME environment
echo variable. Please set JVMSTAT_JAVA_HOME to the path to the
echo J2SE 5.0 JDK installation directory and try again.
GOTO :EOF

:JREERROR
echo The java.exe found at:
echo %JVMSTAT_JAVA_HOME%
echo is not in a JDK directory. Please set your JVMSTAT_JAVA_HOME
echo environment variable to refer to a directory containing the
echo Sun J2SE 1.5.0 JDK (not a JRE) echo and try again.
GOTO :EOF

:VERSIONERROR
echo The java.exe found at:
echo %JVMSTAT_JAVA_HOME%
echo is not the Sun J2SE 1.5.0 JDK version. The J2SE version found is:
java -version
echo Please set your JVMSTAT_JAVA_HOME environment variable echo to
echo refer to a directory containing the Sun J2SE 1.5.0 JDK (not JRE)
echo and try again.
GOTO :EOF

:which %cmd% location
:: performs something similar to the unix which command
setlocal
:: find the cmd given in %1 in the PATH
set res=%~$PATH:1%
endlocal & set "%2=%res%"
GOTO :EOF

:dirname %file% dirpart
:: performs something similar to the unix dirname command
setlocal
:: get just the drive and path components of the given file
set drivepath=%~dp1%
:: eliminate the trailing backslash
set res=%drivepath:~0,-1%
endlocal & set "%2=%res%"
GOTO :EOF
