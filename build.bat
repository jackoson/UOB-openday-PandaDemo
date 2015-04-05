cd "C:\Users\Julian\Google Drive\University\Year 1\Programming and Algorithms II\panda-cwk6"
javac -d "bin\classes" src\client\algorithms\*.java src\client\application\*.java src\client\model\*.java src\client\scotlandyard\*.java src\client\view\*.java
cd bin\classes
jar cfm ..\jar\scotlandyardapplication.jar ..\..\Manifest.txt client\algorithms\*.class client\application\*.class client\model\*.class client\scotlandyard\*.class client\view\*.class resources\counters\*.png resources\players\*.png resources\tickets\*.png resources\*.png resources\*.jpg resources\*.txt
cd ..\jar
java -jar scotlandyardapplication.jar
cd ..\..