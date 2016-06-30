call rmdir /S /Q bin 
call mkdir bin 
call javac -cp lib\* -d bin src\analyser\*.java src\data\*java src\other\*.java src\output\*.java src\test\*.java
