cd C:\eclipse\ws\MacrosLauncher\
"%JAVA_HOME%\bin\jar.exe" cvf BaseClasses.jar -C ..\JavaKernel\build\ .\ru
"%JAVA_HOME%\bin\jar.exe" uvf BaseClasses.jar -C .\build\ .\ru
"%JAVA_HOME%\bin\jar.exe" uvf BaseClasses.jar -C ..\Macros\build\ .\ru\autogenmash\macros\cometmacros
cp BaseClasses.jar ../CNC/deploy

"%JAVA_HOME%\bin\jar.exe" cvfm BaseMacros.jar ..\Macros\manifest\common\MANIFEST.MF -C ..\Macros\build\ .\ru\autogenmash\macros\cometmacros\common
cp BaseMacros.jar ../CNC/deploy

"%JAVA_HOME%\bin\jar.exe" cvfm AdditionalMacros.jar ..\Macros\manifest\additional\MANIFEST.MF -C ..\Macros\build\ .\ru\autogenmash\macros\cometmacros\additional
cp AdditionalMacros.jar ../CNC/deploy


