CLASSPATH=classes:`find aws-java-sdk* -name '*.jar' | xargs | tr ' ' ':'`

compile:
	javac -d classes -classpath $(CLASSPATH) -Xlint:deprecation -Xlint:unchecked src/*.java
