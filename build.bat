cd "/Users/ben_milnes/Documents/Projects/CompSci/PANDA2/Git/panda-cwk6"
cd src
javac -d ../bin/classes client/algorithms/*.java client/application/*.java client/model/*.java client/scotlandyard/*.java client/view/*.java
cd ../bin/classes
jar cfm ../jar/scotlandyardapplication.jar ../../Manifest.txt client/algorithms/*.class client/application/*.class client/model/*.class client/scotlandyard/*.class client/view/*.class resources/counters/*.png resources/players/*.png resources/tickets/*.png resources/*.png resources/*.jpg resources/*.txt
cd ../jar
java -jar scotlandyardapplication.jar
cd ../..