#!/bin/bash

MONTH=$(date +'%m')

if [ $MONTH -le 2 ]; then
    TEMP=$((-1 * ($RANDOM % 20) + $RANDOM % 25))
elif [ $MONTH -lt 6 ]; then
    TEMP=$(($RANDOM % 30))
elif [ $MONTH -le 9 ]; then
    TEMP=$((15 + $RANDOM % 20))
elif [ $MONTH -le 11 ]; then
    TEMP=$(($RANDOM % 20))
else
    TEMP=$((-1 * ($RANDOM % 20) + $RANDOM % 25))
fi

echo $TEMP
