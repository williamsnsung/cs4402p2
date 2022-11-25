#!/bin/bash
javac *.java
touch Manifest.txt
echo "Main-Class: Solver" > Manifest.txt
jar cfm P2.jar Manifest.txt ./*.class
rm *.class
rm Manifest.txt