.PHONY: clean all check

all: test Environment.class EnvironmentMain.class Environment_LibC.o
	env LD_LIBRARY_PATH=. java -cp .:jna.jar EnvironmentMain

Environment_LibC.o: Environment_LibC.cc
	g++ -shared $< -o libEnvironment_LibC.so -I$(JAVA_HOME)/include/linux -I$(JAVA_HOME)/include

test: test.o
	$(CC) -o $@ $^

%.class : %.java
	javac  -cp .:jna.jar $<

clean:
	rm -f *.class *.o *.so test

TestEnvironment.class: TestEnvironment.java
	javac  -cp .:jna.jar:junit4.jar $<

check: TestEnvironment.class
	java -cp ".:jna.jar:junit4.jar" org.junit.runner.JUnitCore TestEnvironment
