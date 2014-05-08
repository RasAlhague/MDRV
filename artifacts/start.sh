#!/bin/bash

xterm -e "sudo java -jar MDRV.jar" &
gnome-terminal -x "sudo java -jar MDRV.jar" &
konsole -e "sudo java -jar MDRV.jar" &