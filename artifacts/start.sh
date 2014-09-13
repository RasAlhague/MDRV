#!/bin/bash

Command="sudo java -jar MDRV.jar"

xfce4-terminal -e "$Command" ||
gnome-terminal -x "$Command" ||
xterm -e "$Command" ||
konsole -e "$Command"
