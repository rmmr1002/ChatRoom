JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $*.java

CLASSES = Server.java

default: classes

classes: $(CLASSES:.java=.class) 

clean:
	rm *.class
