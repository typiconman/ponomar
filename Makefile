default: install

install:
	javac -cp src src/net/ponomar/Main.java

install-separate-directory:
	mkdir binary
	javac -cp src src/net/ponomar/Main.java -d binary
	cp -avr src/images/ binary/images/

run:
	java -cp src net.ponomar.Main

run-separate-directory:
	java -cp binary net.ponomar.Main

test:
	perl src/scripts/Perl/paschalion.pl
	perl src/scripts/Perl/test_lj.pl
