#!/bin/sh
cd /CNC/macros
/usr/java/j9/bin/j9 -cp "/CNC/macros/BaseClasses.jar:/CNC/macros/BaseMacros.jar:/CNC/macros/AdditionalMacros.jar:/usr/java/libs/JavaKernel.jar:/usr/java/libs/swt.jar:/usr/java/libs/jdom.jar:/usr/java/libs/crimson.jar" -Djava.library.path=/usr/java/libs ru.autogenmash.macros.cometmacros.MacrosLauncher