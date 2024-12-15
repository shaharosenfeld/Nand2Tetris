JAVAC=javac
JAVACFLAGS=-encoding ISO-8859-1
SRCS=*.java
EXEC=VMtranslator
TAR=tar
TARFLAGS=cvf
TARNAME=projXXX.tar
TARSRCS=$(SRCS) $(EXEC) Makefile
all: compile
compile:
	$(JAVAC) $(JAVACFLAGS) $(SRCS)
	chmod +x $(EXEC)
export PATH := $(PWD):$(PATH)
tar:
	$(TAR) $(TARFLAGS) $(TARNAME) $(TARSRCS)
clean:
	rm -f *.class *~

