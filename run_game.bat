@echo off
cd /d "d:\Code\Github\OOP Project\ProjectOop\ProjectOop"
echo STARTING JAVA... > debug.log
"C:\Program Files\RedHat\java-21-openjdk-21.0.11.0.10-1\bin\java.exe" -cp out ProjectOop.ui.GameUI >> debug.log 2>&1
echo EXIT CODE: %ERRORLEVEL% >> debug.log
