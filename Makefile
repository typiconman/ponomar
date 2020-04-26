JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Ponomar/utility/OrderedHashtable.java \
	Ponomar/parsing/DocHandler.java \
	Ponomar/parsing/QDParser.java \
	Ponomar/parsing/Reporter.java \
	Ponomar/parsing/parseConvert.java \
	Ponomar/utility/StringOp.java \
	Ponomar/internationalization/LanguagePack.java \
	Ponomar/calendar/JDate.java \
	Ponomar/astronomy/Paschalion.java \
	Ponomar/DoSaint1.java \
	Ponomar/astronomy/Sunrise.java \
	Ponomar/panels/PrintableTextPane.java \
	Ponomar/ConfigurationFiles.java \
	Ponomar/panels/GospelSelector.java \
	Ponomar/utility/Helpers.java \
	Ponomar/calendar/JDaySelector.java \
	Ponomar/calendar/JCalendar.java \
	Ponomar/internationalization/LanguageSelector.java \
	Ponomar/internationalization/Languagizer.java \
	Ponomar/MenuFiles.java \
	Ponomar/About.java \
	Ponomar/parsing/Commemoration.java \
	Ponomar/parsing/ReadText.java \
	Ponomar/Bible.java \
	Ponomar/parsing/Days.java \
	Ponomar/parsing/Fasting.java \
	Ponomar/readings/DivineLiturgy1.java \
	Ponomar/services/Primes.java \
	Ponomar/panels/PrimeSelector.java \
	Ponomar/services/RoyalHours.java \
	Ponomar/parsing/Service.java \
	Ponomar/parsing/ServiceInfo.java \
	Ponomar/parsing/UsualBeginning.java \
	Ponomar/services/ThirdHour.java \
	Ponomar/services/SixthHour.java \
	Ponomar/panels/IconDisplay.java \
	Ponomar/services/NinthHour.java \
	Ponomar/Main.java 

default: classes

classes: $(CLASSES:.java=.class)

test:
	perl Ponomar/scripts/Perl/paschalion.pl
	perl Ponomar/scripts/Perl/test_lj.pl
	
clean:
	$(RM) Ponomar/*.class
	$(RM) Ponomar/*/*.class


