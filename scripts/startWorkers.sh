#!/bin/bash
ssh rpi1.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'
ssh rpi2.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'
ssh rpi3.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'
ssh rpi4.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'
ssh rpi5.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'
ssh rpi6.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'
ssh rpi7.local 'nohup ./scripts/startAlgEngine.sh > /dev/null'


