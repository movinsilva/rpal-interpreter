.PHONY: dirs all clean cl

JC = javac
JFLAGS = -g
CLASSDIR = $(shell pwd)
#CLASSDIR = class #need only this when CLASSDIR = class (and NOT pwd)
SRCDIR = source

.SUFFIXES: .java .class

$(CLASSDIR)/%.class : $(SRCDIR)/%.java
	@echo -n ">>> Compiling $<..."
	@$(JC) $(JFLAGS) -sourcepath $(SRCDIR) -cp $(CLASSDIR) -d $(CLASSDIR) $<
	@echo " done."

SOURCEFILES := \
               ast/AST.java \
               ast/ASTNode.java \
               ast/ASTNodeType.java \
               ast/StandardizeException.java \
               csem/Beta.java \
               csem/CSEMachine.java \
               csem/Delta.java \
               csem/Environment.java \
               csem/Eta.java \
               csem/EvaluationError.java \
               csem/NodeCopier.java \
               csem/Tuple.java \
               parser/ParseException.java \
               parser/Parser.java \
               scanner/LexicalRegexPatterns.java \
               scanner/Scanner.java \
               scanner/Token.java \
               scanner/TokenType.java \
               driver/P1.java \
               driver/P2.java \
               rpal20.java \

all: dirs classestocompile

classestocompile: $(addprefix $(CLASSDIR)/, $(SOURCEFILES:.java=.class))

# Example usage: `make run cmd='-ast ~/rpal/tests/add'`
# The cmd variable is passed to the p1 script, which in turn passes
# it to P1.java
run: all
  ifeq ($(strip $(cmd)),) #NOTE: conditional directive must NOT start with a tab!
		@echo "Please provide parser switches using the cmd variable e.g."
		@echo "make run cmd='-ast <filename>'"
  else
		@./p1 $(cmd)
  endif
#@java -cp $(CLASSDIR) P1 #need only this when CLASSDIR = class (and NOT pwd)


# example usage: `make jar`
jar: all
	@echo -n ">>> Generating P1.jar... "
	@jar -cf P1.jar -m MANIFEST.MF -C . com/ P1.class
	@echo " done."

# example usage: `make test`
test: all
	./difftest.pl -1 "./rpal -st FILE" -2 "java -cp $(CLASSDIR) P2 -st FILE" -t ~/rpal/tests/
#./difftest.pl -1 "./rpal -ast -noout FILE" -2 "java P1 -ast -noout FILE" -t ~/rpal/tests/

dirs:
	@mkdir -p $(CLASSDIR)

cl: clean

clean:
	@rm -rf ast
	@rm -rf csem
	@rm -rf driver
	@rm -rf scanner
	@rm -rf parser
	@rm -f P1.class
	@rm -f P2.class
	@rm -f *.jar
	@rm -fr diffresult
	@rm -rf *.class
	@rm -rf output.*
#@rm -fr $(CLASSDIR) #need only this when CLASSDIR = class (and NOT pwd)
