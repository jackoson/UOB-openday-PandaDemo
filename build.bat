cd "/Users/ben_milnes/Documents/Projects/CompSci/PANDA2/Git/panda-cwk6"
javac -d "bin/classes" -classpath "lib/scotlandyard.jar" src/client/algorithms/*.java src/client/application/*.java src/client/model/*.java src/client/view/*.java
cd bin/classes
jar cfm ../jar/scotlandyardapplication.jar ../../Manifest.txt client/algorithms/*.class client/application/*.class client/model/*.class client/view/*.class resources/counters/*.png resources/players/*.png resources/tickets/*.png resources/*.png resources/*.jpg resources/*.txt
cd ../jar
java -jar scotlandyardapplication.jar
cd ../..