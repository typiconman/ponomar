JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	Ponomar/OrderedHashtable.java \
	Ponomar/DocHandler.java \
	Ponomar/QDParser.java \
	Ponomar/Reporter.java \
	Ponomar/parseConvert.java \
	Ponomar/StringOp.java \
	Ponomar/LanguagePack.java \
	Ponomar/JDate.java \
	Ponomar/Paschalion.java \
	Ponomar/DoSaint1.java \
	Ponomar/Sunrise.java \
	Ponomar/PrintableTextPane.java \
	Ponomar/ConfigurationFiles.java \
	Ponomar/GospelSelector.java \
	Ponomar/Helpers.java \
	Ponomar/JDaySelector.java \
	Ponomar/JCalendar.java \
	Ponomar/LanguageSelector.java \
	Ponomar/Languagizer.java \
	Ponomar/MenuFiles.java \
	Ponomar/About.java \
	Ponomar/Commemoration.java \
	Ponomar/ReadText.java \
	Ponomar/Bible.java \
	Ponomar/Database.java \
	Ponomar/Days.java \
	Ponomar/Fasting.java \
	Ponomar/DivineLiturgy1.java \
	Ponomar/Primes.java \
	Ponomar/PrimeSelector.java \
	Ponomar/RoyalHours.java \
	Ponomar/Service.java \
	Ponomar/ServiceInfo.java \
	Ponomar/UsualBeginning.java \
	Ponomar/ThirdHour.java \
	Ponomar/SixthHour.java \
	Ponomar/IconDisplay.java \
	Ponomar/NinthHour.java \
	Ponomar/Main.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) Ponomar/*.class


