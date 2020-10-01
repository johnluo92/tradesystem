@echo off

javac -d ".\commandline-builds" -cp "src;" .\src\MacroEconomic\Exit.java

cls

java -cp ".\commandline-builds" MacroEconomic.Exit