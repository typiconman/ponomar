default: install

install:
	mkdir binary
	javac -cp src src/net/ponomar/Main.java -d binary
	cp -avr src/images/ binary/images/
	
run:
	java -cp binary net.ponomar.Main
	
clean:
	rm -rf binary

test:
	perl src/scripts/Perl/paschalion.pl
	perl src/scripts/Perl/test_lj.pl
