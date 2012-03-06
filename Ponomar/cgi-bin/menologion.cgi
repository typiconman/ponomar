#!/usr/bin/perl -wT

use warnings;
use strict;
## ACCORDING TO PERL DOC, YOU'RE NOT SUPPOSED TO use bytes
## HOWEVER, XML::Parser APPEARS TO BE BROKEN, THEREFORE THE ONLY WAY TO GET 
## UNICODE DATA THROUGH IT IS TO COMPLETELY SUPRESS CHARACTER SEMANTICS
## (XXX) NOTE THAT THIS BREAKS SOME THINGS LIKE chr, ord, etc, BUT WE DON'T CARE
## IF THIS BECOMES AN ISSUE, LOCALLY SET no bytes
use bytes;

########################################################################################
### menologion.cgi :: VERSION 3: IMPLEMENTS THE RESULTS OF THE GREAT CONVERSION       ##
### 										      ##
### THIS SCRIPT PROCESSES XML DATA FOR THE PONOMAR PROJECT AND OUTPUTS A MENOLOGION   ##
### FOR ANY DAY OF ANY YEAR							      ##
###										      ##
### (C) 2011 ALEKSANDR ANDREEV. THIS CODE IS PART OF THE PONOMAR PROJECT.	      ##
########################################################################################

use CGI;
use CGI::Carp qw( fatalsToBrowser );
use CGI::Cookie;
use XML::Parser;
use Tie::IxHash;
use lib "./";
use JDate;
use General;
use Astro::Sunrise;

BEGIN {
	$ENV{PATH} = "/bin:/usr/bin";
	delete @ENV{ qw( IFS CDPATH ENV BASH_ENV ) };
	CGI::Carp::set_message( \&carp_error );
}

########################################### GLOBAL DEFINITION VARIABLES ##################
use constant false => 0;
use constant CREATION_OF_THE_WORLD => 5508;
my @typicon  = ("", "✺", "<FONT Face=\"Hirmos Ponomar\">🕃</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕃</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕂</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕁</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕀</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕀</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕀</FONT>");
my @toneNumbers = ();
my @weekDays = ();
my %matinsGospels = (
	"Mt_28:16-20" => 1,
	"Mk_16:1-8" => 2,
	"Mk_16:9-20" => 3,
	"Lk_24:1-12" => 4,
	"Lk_24:13-35" => 5,
	"Lk_24:36-53" => 6,
	"Jn_20:1-10" => 7,
	"Jn_20:11-18" => 8,
	"Jn_20:19-31" => 9,
	"Jn_21:1-14" => 10,
	"Jn_21:15-25" => 11
);
my %scriptTypes = ();
my %language_data  = ();
my %bibleBookNames = ();

## THIS IS THE MAIN FILE PATH
my $basepath = "/home/ponomar0/svn/Ponomar/languages/";

### THIS STORES ALL OF THE DATA FROM THE XML FILES
### XXX: IT APPEARS THAT TIE::IXHASH IS STUPID AND CANNOT HANDLE MULTI-LEVEL HASHES
### THAT IS, HASHES OF ARRAYS OF HASHES OF HASHES, PROPERLY
my %SAINTS = ();
my @SAINT_KEYS = ();
tie my %READINGS, "Tie::IxHash";
my %COMMANDS = ();
my @DEBUG = ();
my $whichService = "";
my $readPeriod = false;
my $readBible  = false;
my $fast = "";

### DATA GLOBALS
my $GS = 1; # gospel selector: exJordanville = 0; Lucan Jump = 1
my $language = "en"; # English is the default language
my $Lat = 60.0; # THE DEFAULT LATITUDE
my $Lon = 30.3; # THE DEFAULT LONGITUDE
my $TZ  = 3;	# THE DEFAULT TIMEZONE (FROM GMT)
my $City = "Saint Petersburg, Russia"; # THE NAME OF THE DEFAULT LOCATION
my $dow;	# today's day of week
my $doy;	# Day-of-year
my $nday;	# Number of days before or after this year's Pascha
my $ndayP;	# Number of days after last year's Pascha
my $ndayF;	# Number of days before next year's Pascha
my $Tone;
my $Year;
my $dRank;
my @GLOBALS = qw /dow doy nday Year GS Tone dRank/;

my $src = ""; # THIS IS THE SOURCE OF THE DATA
###################################### GLOBAL SUBROUTINES ################################
sub carp_error {
	my $error_message = shift;
	print "Content-type: text/html\n\n";
	print $error_message;
	print join ",", @DEBUG;
}

sub getPascha {
	my $inyear = shift;
	#Use the Gaussian formulae to calculate the Alexandria Paschallion
	my $a = $inyear % 4;
	my $b = $inyear % 7;
	my $c = $inyear % 19;
	my $d = (19 * $c + 15) % 30;
	my $e = (2 * $a + 4 * $b - $d + 34) % 7;
	my $f = int(($d + $e + 114) / 31); #Month of pascha e.g. march=3
	my $g = (($d + $e + 114) % 31) + 1; #Day of pascha in the month
	return JDate->new($f, $g, $inyear);
}

sub getGregorianOffset {
	my $inyear = shift;
	
	# First, calculate which century we are in
	my $century = int($inyear / 100);
	
	# If this is one of the special centuries, skip to the previous one
	if (($century % 4) == 0) {
		$century--;
	}
	
	# If we're before the start of the gregorian calendar, just return 10
	my $answer;
	
	if ($century <= 15) {
		$answer = 10;
	} else {
		# Figure out how many special centuries there have been between now and 16
		my $offset = int(($century - 16) / 4) + 1;
		
		# Now calculate the offset
		# Take 10, add the number of centuries, and subract the offset
		$answer = 10 + ($century - 15) - $offset;
	}
	
	return $answer;
}

sub findBottomUp {
	my ($language, $file) = @_;
	
	###########################
	### THIS ALGORITHM IMPLEMENTS BOTTOM-UP READING OF FILES IN THE XML PATH
	### THE FULL IMPLEMENTATION IS DESCRIBED BY YURI IN "A Description", p. 27
	###
	### Basically, we begin with basepath/<language>/<script>/<locale>/file
	### and go until basepath/file
	### Stopping at the first occurance of file, which is then read
	###
	### PARAMETERS PASSED TO HERE: $language is e.g. cu/ru or zh/Hans
	### $file is e.g., xml/01/01.xml or xml/pentecostarion/01.xml
	###########################
	
	# we have a path like language: cu/ru or zh/Hans
	# file: xml/01/01.xml
	my @parts = split (/\//, $language);
	for (my $j = $#parts; $j >= 0; $j--) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/$file";
		return $path if (-e $path);
	}

	return $basepath . $file if (-e $basepath . $file);
	die "Unable to find file $file in the bottom-up path for language $language";
}

sub findTopDown {
	my ($language, $file) = @_;
	
	###########################
	### THIS ALGORITHM IMPLEMENTS THE TOP-DOWN APPROACH FOR READING FILES
	### DESCRIBED BY YURI IN op. cit., p. 28
	###
	### WE CREATE AN ARRAY OF ALL EXTANT FILES NAMED $file IN ALL PATHS
	### BEGINNING WITH BASEPATH
	### AND UP TO $basepath/<language>/<script>/<locale>/file
	### PARAMETERS: SAME AS ABOVE
	############################
	
	my @paths = ();
	push @paths, $basepath . $file if (-e $basepath . $file);
	my @parts = split(/\//, $language);
	for (my $j = 0; $j < @parts; $j++) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/" . $file;
		push @paths, $path if (-e $path);
	}
	warn "Unable to find any instances of $file in the path for $language" unless (@paths);
	return @paths;
}

sub isNumeric {
	shift;
	return /^(\d+\.?\d*|\.\d+)$/;
}

sub max {
	my $max = shift;
	for ( @_ ) { $max = $_ if $max < $_; }
	return $max;
}

sub default {
	return;
}

sub text {
	return;
}

sub startElement {
	my( $parseinst, $element, %attrs ) = @_;
	
	if ($attrs{Cmd}) {
		# remember that in perl, variable names must start with a $
		# edit the Cmd
		my $cmd = $attrs{Cmd};
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		return unless eval($cmd);
	}
	if ($attrs{Tone}) {
		my $tone = $attrs{Tone};
		foreach (@GLOBALS) {
			$tone =~ s/([^\$])$_/$1\$$_/g;
		}

		$attrs{Tone} = $Tone = int(eval($tone));
	}
	SWITCH: {
		if ($element eq "SAINT") {
			# INITIAL COMMEMORATION INFORMATION
			# CREATE NEW ENTRY IN SAINTS HASH WITH THE KEY OF CID
			# PUSH ALL OTHER RELEVANT KEYS TO THIS HASH
			my $CId = $attrs{CId};
			delete $attrs{CId};
			%{ $SAINTS{$CId} } = (%attrs, "Reason" => $src);
			push @SAINT_KEYS, $CId;
			last SWITCH;
		} 
		if ($element eq "NAME") {
			# src is a CID or SID
			# PUSH ALL INFORMATION INTO THIS HASH
			die "Invalid identifier $src " unless exists $SAINTS{$src};
			delete $attrs{Cmd};
			## THIS WILL DO THE FOLLOWING:
			## SUPPOSE WE HAVE THE TAG NAME WITH ATTRS Nominative, Short
			## WE NOW SET $SAINTS{$src}{NAME}{Nominative}, etc.
			## WHEN (if) WE REREAD THIS FILE IN A DIFFERENT LANG, THESE ATTRS
			## WILL BE AUTOMATICALLY OVERWRITTEN AS NEEDED
			@{ $SAINTS{$src}{$element}}{keys %attrs} = values %attrs;
			last SWITCH;
		}
		if ($element eq "SCRIPTURE") {
			# again src is a CId or SId
			# PUSH ALL INFORMATION INTO READINGS HASH
			die "Invalid identifier $src " unless exists $SAINTS{$src};
			# CREATE (OR REPLACE) AN ENTRY IN READINGS SUBHASH
			# WHERE CId (SId) is the Key
			# E.G., $READINGS{123}{apostol}{Reading}
			# and $READINGS{123}{apostol}{Pericope}
			my $type = $attrs{Type};
			delete @attrs{ qw(Cmd Type) }; # don't need this if we've gotten here
			unless ($whichService) {
				## orphan?
				last SWITCH;
			}
			@{ $READINGS{$src}{$whichService}{$type} }{keys %attrs} = values %attrs;
			# (XXX) THE ABOVE SHOULD ALLOW FOR LOCALE-LEVEL INHERITANCE,
			# EVEN OF THINGS LIKE PERICOPE NUMBERS, IF NECESSARY, BUT NOT TESTED
			last SWITCH;
		}
		if ($element eq "SERVICE") {
			die "Invalid identifier $src " unless exists $SAINTS{$src};
			$SAINTS{$src}{Type} = $attrs{Type}; 
			last SWITCH;
		}
		if ($element eq "PRIMES") {
			$whichService = "1st hour";
			last SWITCH;
		}
		if ($element eq "TERCE") {
			$whichService = "3rd hour";
			last SWITCH;
		}
		if ($element eq "SEXTE") {
			$whichService = "6th hour";
			last SWITCH;
		}
		if ($element eq "NONE") {
			$whichService = "9th hour";
			last SWITCH;
		}
		if ($element eq "VESPERS") {
			$whichService = "vespers";
			last SWITCH;
		}
		if ($element eq "MATINS") {
			$whichService = "matins";
			last SWITCH;
		}
		if ($element eq "LITURGY") {
			$whichService = "liturgy";
			last SWITCH;
		}
		if ($element eq "PERIOD") {
			$readPeriod = !false;
			last SWITCH;
		}
		if ($element eq "RULE" && $readPeriod) {
			$fast = $attrs{Case};
			last SWITCH;
		}
		if ($element eq "BIBLE") {
			# IDs are of the form lang/bible/version where lang is ISO lang code
			my @id_parts = ();
			foreach (split(/\//, $attrs{Id})) {
				last if $_ eq "bible";
				push @id_parts, $_;
			}
			
			my @lang_parts = split(/\//, $language);
			$readBible = $id_parts[0] eq $lang_parts[0]; ## FIXME
			last SWITCH;
		}
		if ($element eq "BOOK" && $readBible) {
			my $tmpid = $attrs{Id};
			$tmpid =~ s/_/ /;
			$bibleBookNames{$tmpid} = $attrs{Short};
			last SWITCH;
		}
		if ($element eq "COMMAND") {
			my $i = max keys %COMMANDS;
			@{ $COMMANDS{++$i} }{keys %attrs} = values %attrs;
			last SWITCH;
		}
	};
}

sub endElement {
	my ($parseinst, $element, %attrs) = @_;
	
	if ($element eq "VESPERS" || $element eq "MATINS" || $element eq "LITURGY" || $element eq "SERVICE") {
		$whichService = "";
	} elsif ($element eq "PERIOD") {
		$readPeriod   = false;
	} elsif ($element eq "BIBLE") {
		$readBible    = false;
	}
	return;
}

sub formatScriptureReading {
	my ($reading, $pericope, $effWeek) = @_;
	
	my ($book, $verses) = split(/_/, $reading);
	my $MG = exists $matinsGospels{$reading} && $dow == 0 ? " " . $language_data{133 + $matinsGospels{$reading}} : "";
	# $MG .= " (" . $weekDays[$dow] . " $effWeek)" if (defined $effWeek);
	return defined $pericope ? qq(<A Href="JavaScript:doReadings('$book', '$verses');">$bibleBookNames{$book} $verses (§ $pericope)</A>$MG) : qq(<A Href="JavaScript:doReadings('$book', '$verses');">$bibleBookNames{$book} $verses</A>$MG);
}

sub convert {
	my $fast = shift;

	my %fastReqs = (
		"0000000" => $language_data{120},
		"0000001" => $language_data{121},
		"0000011" => $language_data{122},
		"0000111" => $language_data{123},
		"0001111" => $language_data{124},
		"0011111" => $language_data{125},
		"0111111" => $language_data{126},
		"1111111" => $language_data{127},
		"0000010" => $language_data{128}
		);
	return exists $fastReqs{$fast} ? $language_data{30} . ": " . $fastReqs{$fast} : "Error computing fast requirements";
}

##################################### BEGIN CODE #######################################
my $q = new CGI;

#### GET THE USER'S COOKIE
my %cookies = fetch CGI::Cookie;
if ($cookies{"menologion"}) {
	my @temp = split(/\|/, $cookies{"menologion"}->value());
	if (@temp == 6) {
		($City, $Lat, $Lon, $TZ, $language, $GS) = @temp;
	}
}

#### FIGURE OUT WHICH DAY WE WANT
my $year = $q->param("year") || (gmtime(time))[5] + 1900;
my $tmshft = $q->param("tmshft") % 25 || 0;
$tmshft += $TZ;
$tmshft *= 3600;
my $offset = getGregorianOffset($year);
$year = $q->param("year") || (gmtime(time + $tmshft - ($offset * 24 * 60 * 60)))[5] + 1900;
my $month = $q->param("month") || (gmtime(time + $tmshft - ($offset * 24 * 60 * 60)))[4] + 1;
my $day   = $q->param("day") || (gmtime(time + $tmshft - ($offset * 24 * 60 * 60)))[3];

#### UNTAINT VARIABLES PASSED VIA THE QUERY STRING
my $validchars = "0-9";
if ( $year =~ /^([$validchars]+)$/ ) {
	$year = $1;
} else {
	die "This is not a valid Year. Execution stopped ";
}

if ( $month =~ /^([$validchars]+)$/ ) {
	$month = $1;
} else {
	die "This is not a valid Month. Execution stopped ";
}

if ( $day =~ /^([$validchars]+)$/ ) {
	$day = $1;
} else {
	die "This is not a valid Day. Execution stopped ";
}

### LOAD LANGUAGE DATA
%language_data = General->loadLanguage($language);
%scriptTypes = (
	"1st hour" => $language_data{84},
	"3rd hour" => $language_data{85},
	"6th hour" => $language_data{86},
	"9th hour" => $language_data{87},
	"vespers"  => $language_data{88},
	"compline" => $language_data{89},
	"nocturns" => $language_data{90},
	"matins"   => $language_data{91},
	"liturgy"  => $language_data{92},
	);
@toneNumbers = map $language_data{$_}, (76..83);
@weekDays    = map $language_data{$_}, (106..112);
unshift @toneNumbers, ""; 

#### CREATE THE GLOBAL CLASSES
my $today      = JDate->new($month, $day, $year);
my $thispascha = getPascha($year);
my $lastpascha = getPascha($year - 1);
my $nextpascha = getPascha($year + 1);

#### GET TODAY'S GLOBAL VARIABLES
$dow   = $today->getDayOfWeek();
$doy   = $today->getDoy();
$nday  = JDate->difference($today, $thispascha);
$ndayP = JDate->difference($today, $lastpascha);
$ndayF = JDate->difference($today, $nextpascha);
$Year  = $today->getYear();

#### ADDITIONAL VARIABLES REQUIRED FOR PROPER NAVIGATION
my $tomorrow  = $today->addDays(1);
my $yesterday = $today->subtractDays(1);
my $this_day_last_yr = $lastpascha->addDays($nday);
my $this_day_next_yr = $nextpascha->addDays($nday);

## CALCULATE THE SUNRISE AND SUNSET FOR THE USER'S LOCATION
my ($sunrise, $sunset) = sunrise($today->getYearGregorian(), $today->getMonthGregorian(), $today->getDayGregorian(), $Lon, $Lat, $TZ);

#### FIGURE OUT WHERE WE ARE IN TERMS OF THE TRIODION / PENTECOSTARION CYCLE
my $directory;
my $filename;
if ($nday >= -70 && $nday < 0) {
	$directory = "triodion";
	$filename  = abs($nday);
} elsif ($nday < -70) {
	$directory = "pentecostarion";
	$filename  = $ndayP + 1;
} else {
	$directory = "pentecostarion";
	$filename  = $nday + 1;
}

my $filepath = "xml/" . $directory . "/" . ($filename >= 10 ? $filename . ".xml" : "0" . $filename . ".xml");

#### SET UP THE PARSER
my $PARSER = new XML::Parser(ErrorContext => 2);
$PARSER->setHandlers(	Start   => \&startElement,
			End     => \&endElement,
			Char    => \&text,
			Default => \&default);

#### LOAD BIBLE BOOKS FOR THE GIVEN LANGUAGE
$PARSER->parsefile( findBottomUp($language, "xml/bible.xml") );

#### BEGIN BY PARSING THE PENTECOSTARION / TRIODION FILE
$src = "pentecostarion";
push @DEBUG, findBottomUp($language, $filepath);
$PARSER->parsefile( findBottomUp($language, $filepath) );

#### NEXT, PARSE THE MENAION FILE
$filepath = "xml/";
$filepath .= $today->getMonth() < 10 ? "0" . $today->getMonth() : $today->getMonth();
$filepath .= $today->getDay() < 10 ? "/0" . $today->getDay() : "/" . $today->getDay();
$filepath .= ".xml";

$src = "menaion";
push @DEBUG, findBottomUp($language, $filepath);
$PARSER->parsefile( findBottomUp($language, $filepath) );

#### WE NOW HAVE A SET OF CIDs, which we are ready to Parse
$src = "";
foreach my $CId (keys %SAINTS) {
	$src = $CId;
	foreach my $file (findTopDown($language, "xml/lives/$CId.xml")) {
		push @DEBUG, $file;
		$PARSER->parsefile( $file);
	}
}

##### GET THE ICON OF THE DAY
# 1. Build the icon language substitution algorithm
my @ils = ("cu", "el", "zh", "en", "fr");
for (my $i = 0; $i <= $#ils; $i++) {
	last if (split (/\//, $language))[0] eq $_;
	push @ils, shift @ils;
}

# 2. Find out if we have an icon available
my $icon = "http://www.ponomar.net/images/icon.jpg";
OUTERLOOP: foreach my $id (sort { $SAINTS{$a}{Type} <=> $SAINTS{$b}{Type} } @SAINT_KEYS) {
	next unless $id;
	foreach my $l (@ils) {
		eval {
			findBottomUp( $l, "icons/$id/" );
		};
		unless ($@) {
			$icon = "http://www.ponomar.net/cgi-bin/fetch.cgi?lang=$l&saint=$id&icon=0";
			last OUTERLOOP;
		}
	}
}

##### PROCESS THE FASTING INSTRUCTIONS
##### SET dRank
$dRank = max ( map { $SAINTS{$_}{Type} } keys %SAINTS );

foreach my $file (findTopDown($language, "xml/Commands/Fasting.xml")) {
	push @DEBUG, $file;
	$PARSER->parsefile( $file );
}

################################ CREATE THE USER'S COOKIE ###################################
my $cookievalue = join("|", ($City, $Lat, $Lon, $TZ, $language, $GS));
my $cookie  = new CGI::Cookie(-name   => 'menologion', 
			      -value  => "$cookievalue",
			      -expires=> '+1y',
			      -domain => 'ponomar.net');
			     
print "Set-Cookie: $cookie\n";
############################### NAVIGATION DATA #############################################
my @yesterdayvals = ($yesterday->getMonth(), $yesterday->getDay(), $yesterday->getYear());
my @tomorrowvals  = ($tomorrow->getMonth(), $tomorrow->getDay(), $tomorrow->getYear());

################################ CONSTRUCT THE OUTPUT #######################################
print "Content-type: text/html; charset=utf-8\n\n";

General->write_top($month, $day, $year);

print "<CENTER>\n";
print "$language_data{20} <B>" . $today->toStringFull($language) . "</B><BR>";
print "$language_data{21}: " . $today->toStringGregorian($language) . "<BR>";
print "<BR><BR>";

print qq(<A Href="JavaScript:navigate3($yesterdayvals[0], $yesterdayvals[1], $yesterdayvals[2]);">&lt;&lt; $language_data{22}</A>);
print "<IMG Src=\"$icon\" Alt=\"Icon fetcher\">\n";;
print qq(<A Href="JavaScript:navigate3($tomorrowvals[0], $tomorrowvals[1], $tomorrowvals[2]);">$language_data{23} &gt;&gt;</A></CENTER><BR><BR>);
print "$language_data{24}: $sunrise; $language_data{25}: $sunset $language_data{26} $City (<A Href=\"JavaScript:openWindow('configure.cgi');\">$language_data{27}</A>)<BR>\n";
print convert ($fast) . "<BR>\n";

## print Pentecostarion data
foreach (@SAINT_KEYS) {
	next unless $SAINTS{$_}{Reason} eq "pentecostarion";

	# Get the dRank of the observance
	print $typicon[$SAINTS{$_}{Type}] . " " if ($SAINTS{$_}{Type});
	print qq(<B>) . $SAINTS{$_}{NAME}{Nominative} . "</B>";
	print " " . $toneNumbers[$SAINTS{$_}{Tone}] if ($SAINTS{$_}{Tone});
	print "; ";
}

print "<BR><BR>";
## print the Menaion data
foreach (@SAINT_KEYS) {
	next unless $SAINTS{$_}{Reason} eq "menaion";
	
	print $typicon[$SAINTS{$_}{Type}] . " " if ($SAINTS{$_}{Type});
	if ($SAINTS{$_}{Type} >= 5) {
		print qq(<A Href="JavaScript:doLives($_);"><B><FONT Color="red">) .
			$SAINTS{$_}{NAME}{Nominative} . "</FONT></B></A>";
	} elsif ($SAINTS{$_}{Type} >= 4) {
		print qq(<A Href="JavaScript:doLives($_);"><B>) .
			$SAINTS{$_}{NAME}{Nominative} . "</B></A>";
	} else {	
		print qq(<A Href="JavaScript:doLives($_);">) . $SAINTS{$_}{NAME}{Nominative} . "</A>";
	}
	print "; ";
}

################### BEGIN SCRIPTURE MEGA-SORTING ALGORITHM #####################
print "<BR><BR><DIV Class=\"header\" Align=\"center\">$language_data{29}</DIV><BR>\n";

## IMPLEMENT SUPRESSION AND MOVEMENT OF READINGS
# 1. LOAD SUPPRESSION INSTRUCTIONS INTO A HASH
foreach ( findTopDown($language, "xml/Commands/DivineLiturgy.xml") ) {
	$PARSER->parsefile( $_ );
}

# 2. DECIDE IF TODAY'S READINGS ARE SUPPRESSED
foreach ( keys %COMMANDS ) {
	## RECALL THAT IN PERL, VARS MUST START WITH $
	my $cmd = $COMMANDS{$_}{Value};
	foreach (@GLOBALS) {
		$cmd =~ s/$_/\$$_/g;
	}
	next unless eval $cmd;
	## IF WE GOT HERE, THEN THE READING IS TO BE SUPPRESSED
	## REMEMBER, THE SUPPRESSION REFERS ONLY TO PENTECOSTARION READINGS
	## AND MAY BE CAUSED BY THE PRESENCE OF A MENAION-BASED COMMEMORATION
	## XXX: FOR EXAMPLE, THE dRank OF ASCENSION IS 7, BUT THE READING IS NOT SUPPRESSED
	## WE HANDLE THIS BY ENSURING THAT THE SOURCE OF THE READING IS AN UNRANKED COMMEMORATION
	## ALSO, NOTE THAT WE MAY HAVE MULTIPLE-SOURCE RANKED COMMEMORATIONS ON A GIVEN DAY
	## E.G., ASCENSION + STS CYRIL AND METHODIUS, OR MID-PENTECOST + ST JOHN
	## BUT IN THESE CASES, WE DO **NOT** SUPPRESS THE ``DAILY'' READINGS
	foreach my $source (keys %READINGS) {
		next unless $READINGS{$source}{liturgy};
		next unless $SAINTS{$source}{Reason} eq "pentecostarion";
		delete $READINGS{$source}{liturgy} unless ($SAINTS{$source}{Type} >= 1);
		#if ($COMMANDS{$_}{Name} eq "Suppress") {
		#	delete $READINGS{$src}{matins}  unless ($SAINTS{$src}{Type} >= 1);
		#}
	}
}

# 3. TODO: CHECK TOMORROW AND YESTERDAY FOR TRANSFERS
### 3.1: TOMORROW
### 3.1.1: Set the Globals to Tomorrow's values
$dow = $tomorrow->getDayOfWeek();
$doy = $tomorrow->getDoy();
$nday  = JDate->difference($tomorrow, $thispascha);
$ndayP = JDate->difference($tomorrow, $lastpascha);
$ndayF = JDate->difference($tomorrow, $nextpascha);
$Year  = $tomorrow->getYear();

### 3.1.2: Figure out where we are in the Pentecostarion / Triodion cycle
if ($nday >= -70 && $nday < 0) {
	$directory = "triodion";
	$filename  = abs($nday);
} elsif ($nday < -70) {
	$directory = "pentecostarion";
	$filename  = $ndayP + 1;
} else {
	$directory = "pentecostarion";
	$filename  = $nday + 1;
}

$filepath = "xml/" . $directory . "/" . ($filename >= 10 ? $filename . ".xml" : "0" . $filename . ".xml");

### 3.1.3: Set the SOURCE. Pentecostarion2 means we are only interested in readings for tomorrow
$src = "pentecostarion2";

### 3.1.4: Parse tomorrow's Pentecostarion DATA
## THIS ADDS DATA TO SAINTS WITH THE "Reason" LABEL SET TO pentecostarion2
push @DEBUG, findBottomUp($language, $filepath);
$PARSER->parsefile( findBottomUp($language, $filepath) );

### 3.1.5: NOW PARSE EACH COMMEMORATION FILE FILE OF pentecostarion2 TO OBTAIN DAILY READINGS
$src = "";

foreach my $source (keys %SAINTS) {
	next unless $SAINTS{$source}{Reason} eq "pentecostarion2";
	## XXX: CIDs 9800 - 9900 are reserved for Triodion
	## CIDs 9000 - 9315 are reserved for Pentecostarion
	## NO OTHER CIDs should be read
	next unless (($source > 9000 && $source <= 9315) || ($source > 9800 && $source < 9900));

	$src = $source;
	foreach my $file (findTopDown($language, "xml/lives/$src.xml")) {
		push @DEBUG, $file;
		$PARSER->parsefile( $file );
	}
}

### 3.1.6: Parse tomorrow's Menaion DATA
$filepath = "xml/";
$filepath .= $tomorrow->getMonth() < 10 ? "0" . $tomorrow->getMonth() : $tomorrow->getMonth();
$filepath .= $tomorrow->getDay() < 10 ? "/0" . $tomorrow->getDay() : "/" . $tomorrow->getDay();
$filepath .= ".xml";

$src = "menaion2";
push @DEBUG, findBottomUp($language, $filepath);
$PARSER->parsefile( findBottomUp($language, $filepath) );

### 3.1.7: Again, parse each commemoration file. Here, we actually only need the dRank
### XXX: IN THE FUTURE, WE SHOULD ALLOW FOR DIFFERENT SERVICE ALTERNATIVES
$src = "";
foreach my $source (keys %SAINTS) {
	next unless $SAINTS{$source}{Reason} eq "menaion2";

	$src = $source;
	foreach my $file (findTopDown($language, "xml/lives/$src.xml")) {
		push @DEBUG, $file;
		$PARSER->parsefile( $file);
	}
}

### 3.1.8: Compute tomorrow's dRank
$dRank = max ( map { $SAINTS{$_}{Type} } grep { $SAINTS{$_}{Reason} eq "menaion2" } keys %SAINTS );

### 3.1.9: CHECK IF WE HAVE A TransferRulesB COMMAND
my $KEEP_FLAG = false;
foreach (keys %COMMANDS) {
	if ($COMMANDS{$_}{Name} eq "TransferRulesB") {
		my $cmd = $COMMANDS{$_}{Value};
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		if (eval $cmd) {
			$KEEP_FLAG = !false;
		}
	}
}

### 3.1.10: DELETE TOMORROW'S READINGS, KEEPING IF THE TRANSFERRULESB COMMAND WAS TRUE
foreach my $src (keys %READINGS) {
	delete $READINGS{$src} if ($SAINTS{$src}{Reason} eq "menaion2");
	delete $READINGS{$src} if ($SAINTS{$src}{Reason} eq "pentecostarion2" && !$KEEP_FLAG);
}	

### 3.2: NOW CHECK FOR TRANSFER READINGS FOR YESTERDAY
### 3.2.1: Set the Globals to Yesterday's values
$dow = $yesterday->getDayOfWeek();
$doy = $yesterday->getDoy();
$nday  = JDate->difference($yesterday, $thispascha);
$ndayP = JDate->difference($yesterday, $lastpascha);
$ndayF = JDate->difference($yesterday, $nextpascha);
$Year  = $yesterday->getYear();

### 3.2.2: Figure out where we are in the Pentecostarion / Triodion cycle
if ($nday >= -70 && $nday < 0) {
	$directory = "triodion";
	$filename  = abs($nday);
} elsif ($nday < -70) {
	$directory = "pentecostarion";
	$filename  = $ndayP + 1;
} else {
	$directory = "pentecostarion";
	$filename  = $nday + 1;
}

$filepath = "xml/" . $directory . "/" . ($filename >= 10 ? $filename . ".xml" : "0" . $filename . ".xml");

### 3.2.3: Set the SOURCE. Pentecostarion3 means we are only interested in readings for yesterday
$src = "pentecostarion3";

### 3.2.4: Parse yesterday's Pentecostarion DATA
## THIS ADDS DATA TO SAINTS WITH THE "Reason" LABEL SET TO pentecostarion3
push @DEBUG, findBottomUp($language, $filepath);
$PARSER->parsefile( findBottomUp($language, $filepath) );

### 3.2.5: NOW PARSE EACH COMMEMORATION FILE OF pentecostarion3 TO OBTAIN DAILY READINGS
$src = "";

foreach my $source (keys %SAINTS) {
	next unless $SAINTS{$source}{Reason} eq "pentecostarion3";
	## XXX: CIDs 9800 - 9900 are reserved for Triodion
	## CIDs 9000 - 9315 are reserved for Pentecostarion
	## NO OTHER CIDs should be read
	next unless (($source > 9000 && $source <= 9315) || ($source > 9800 && $source < 9900));
	
	$src = $source;
	foreach my $file (findTopDown($language, "xml/lives/$src.xml")) {
		push @DEBUG, $file;
		$PARSER->parsefile( $file );
	}
}

### 3.2.6: Parse yesterday's Menaion DATA
$filepath = "xml/";
$filepath .= $yesterday->getMonth() < 10 ? "0" . $yesterday->getMonth() : $yesterday->getMonth();
$filepath .= $yesterday->getDay() < 10 ? "/0" . $yesterday->getDay() : "/" . $yesterday->getDay();
$filepath .= ".xml";

$src = "menaion3";
push @DEBUG, findBottomUp($language, $filepath);
$PARSER->parsefile( findBottomUp($language, $filepath) );

### 3.2.7: Again, parse each commemoration file. Here, we actually only need the dRank
### XXX: IN THE FUTURE, WE SHOULD ALLOW FOR DIFFERENT SERVICE ALTERNATIVES
$src = "";
foreach my $source (keys %SAINTS) {
	next unless $SAINTS{$source}{Reason} eq "menaion3";

	$src = $source;
	foreach my $file (findTopDown($language, "xml/lives/$src.xml")) {
		push @DEBUG, $file;
		$PARSER->parsefile( $file );
	}
}

### 3.2.8: Compute yesterday's dRank
$dRank = max ( map { $SAINTS{$_}{Type} } grep { $SAINTS{$_}{Reason} eq "menaion3" } keys %SAINTS );

### 3.2.9: CHECK IF WE HAVE A TransferRulesF COMMAND
$KEEP_FLAG = false;
foreach (keys %COMMANDS) {
	if ($COMMANDS{$_}{Name} eq "TransferRulesF") {
		my $cmd = $COMMANDS{$_}{Value};
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		if (eval $cmd) {
			$KEEP_FLAG = !false;
		}
	}
}

### 3.2.10: DELETE YESTERDAY'S READINGS, KEEPING IF THE TRANSFERRULESF COMMAND WAS TRUE
foreach my $src (keys %READINGS) {
	delete $READINGS{$src} if ($SAINTS{$src}{Reason} eq "menaion3");
	delete $READINGS{$src} if ($SAINTS{$src}{Reason} eq "pentecostarion3" && !$KEEP_FLAG);
}	

# 3.3: SET EVERYTHING BACK
$dow = $today->getDayOfWeek();
$doy = $today->getDoy();
$nday  = JDate->difference($today, $thispascha);
$ndayP = JDate->difference($today, $lastpascha);
$ndayF = JDate->difference($today, $nextpascha);
$Year  = $today->getYear();
$dRank = max ( map { $SAINTS{$_}{Type} } grep { $SAINTS{$_}{Reason} eq "menaion" || $SAINTS{$_}{Reason} eq "pentecostarion" } keys %SAINTS );

## SET US UP THE BOMB
my @order_of_types = ("1st hour", "3rd hour", "6th hour", "9th hour", "vespers", "matins", "liturgy");
my @order_of_reads = $dow == 6 ? ("menaion", "pentecostarion") : ("pentecostarion", "pentecostarion2", "pentecostarion3", "menaion");
my %sort_order     = map  { $order_of_reads[$_] => $_ } (0..$#order_of_reads);
my @order_of_srcs  = sort { $sort_order{$SAINTS{$a}{Reason}} <=> $sort_order{$SAINTS{$b}{Reason}} } keys %SAINTS;

# 4. OUTPUT THE REMAINING READINGS	
foreach my $type (@order_of_types) {
	next unless grep { exists $READINGS{$_}{$type} } @order_of_srcs;
	print "<B>" . $scriptTypes{$type} . "</B>: ";
	foreach my $source (@order_of_srcs) {
		next unless $READINGS{$source}{$type};
		
		foreach my $reading (sort {$a cmp $b} keys %{ $READINGS{$source}{$type} } ) {
			if (defined $READINGS{$source}{$type}{$reading}{EffWeek}) {
				print formatScriptureReading( $READINGS{$source}{$type}{$reading}{Reading}, $READINGS{$source}{$type}{$reading}{Pericope} );
				my $descript = $SAINTS{$source}{NAME}{Nominative};
				my $effWeek  = $READINGS{$source}{$type}{$reading}{EffWeek};
				$descript =~ s/\d+/$effWeek/;
				print " ($descript); ";
			} else {
				print formatScriptureReading( $READINGS{$source}{$type}{$reading}{Reading}, $READINGS{$source}{$type}{$reading}{Pericope} );
				print "; ";
			}
		}
			
#		print join ("; ", map { formatScriptureReading( $READINGS{$source}{$type}{$_}{Reading}, $READINGS{$source}{$type}{$_}{Pericope}, $READINGS{$source}{$type}{$_}{EffWeek} ) } sort { $a cmp $b } keys %{ $READINGS{$source}{$type} });
		print " (" . ($SAINTS{$source}{NAME}{Genetive} or $SAINTS{$source}{NAME}{Short}) . "); " unless (grep { $READINGS{$source}{$type}{$_}{EffWeek} } keys %{ $READINGS{$source}{$type} });
	}
	print "<BR>\n";
}

############# DEBUGGING INFORMATION ####################
print qq(<!-- dow: $dow; doy: $doy; nday: $nday; ndayP: $ndayP; ndayF: $ndayF; dRank: $dRank -->);
General->footer_en();
