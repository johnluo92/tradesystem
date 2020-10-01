@echo off

javac -d ".\commandline-builds" -cp "src;" .\src\MacroEconomic\Crude_Oil.java

cls

java -cp ".\commandline-builds" MacroEconomic.Crude_Oil