#!/bin/bash
javac -d $HOME/bin -cp $HOME/bin $HOME/src/org/cicirello/matrixops/*.java $HOME/src/org/cicirello/math/*.java $HOME/src/org/cicirello/algengine/*.java 
if [ ! -f $HOME/scripts/Manifest.txt ]; then
    printf 'Main-Class: org.cicirello.algengine.ParallelAlgorithmEngine\nClass-Path: %s/lib/algengine.jar\n' $HOME > $HOME/scripts/Manifest.txt
fi
jar cvfm $HOME/lib/algengine.jar $HOME/scripts/Manifest.txt -C $HOME/bin org/cicirello/matrixops -C $HOME/bin org/cicirello/math -C $HOME/bin org/cicirello/algengine
javac -d $HOME/bin -cp $HOME/bin $HOME/src/org/cicirello/tests/parperformance/*.java 
