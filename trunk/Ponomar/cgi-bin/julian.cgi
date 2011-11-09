#!/usr/bin/perl -wT

use warnings;
use strict;
use utf8;
use Encode qw( encode_utf8 );
##################################################################################
### julian.cgi :: THE MENOLOGION ON YOUR SITE SERVICE, VERSION 3.0 BETA
### 
##################################################################################

use CGI;
use CGI::Carp qw( fatalsToBrowser );
use CGI::Cookie;
use XML::Parser;
use Tie::IxHash;
use lib "./";
use JDate;
use General;

BEGIN {
	$ENV{PATH} = "/bin:/usr/bin";
	delete @ENV{ qw( IFS CDPATH ENV BASH_ENV ) };
}

use constant false => 0;
use constant CREATION_OF_THE_WORLD => 5508;
my @typicon  = ("", "A service with no mark in the Typicon is served for this saint", 
			"Sextuple service", "Doxology service",
			"Polyeleos service", "Vigil-rank service", "Great feast");
my @tones    = ("VIII", "I", "II", "III", "IV", "V", "VI", "VII");
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
tie my %SAINTS, "Tie::IxHash";
tie my %READINGS, "Tie::IxHash";
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

		$Tone = int(eval($tone));
	}
	SWITCH: {
		if ($element eq "SAINT") {
			# INITIAL COMMEMORATION INFORMATION
			# CREATE NEW ENTRY IN SAINTS HASH WITH THE KEY OF CID
			# PUSH ALL OTHER RELEVANT KEYS TO THIS HASH
			my $CId = $attrs{CId};
			delete $attrs{CId};
			%{ $SAINTS{$CId}} = (%attrs, "Reason" => $src);
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
		if ($element eq "SEXTE") {
			$whichService = "6th hour";
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
	my ($reading, $pericope) = @_;
	
	my ($book, $verses) = split(/_/, $reading);
	my $MG = exists $matinsGospels{$reading} && $dow == 0 ? " " . $language_data{133 + $matinsGospels{$reading}} : "";
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

my $mode = $q->param("mode");
my $LS = $q->param("LS") || 0;
my $tmshft = 0;
if ($q->param("timeshift")) {
	$tmshft = $q->param("timeshift");
	if (abs($tmshft) > 15) {
		$tmshft = $tmshft % 12;
	}
}
# untaint
if ($LS =~ /([\d]+)/) {
	$LS = $1;
} else {
	$LS = 0;
}

my %lang_map = (
	0 => "en",
	1 => "fr",
	3 => "cu/ru",
	5 => "zh/Hans",
	6 => "zh/Hant"
	);

$language = $lang_map{$LS};

#### LOAD THE LANGUAGE DATA
%language_data = General->loadLanguage($language);
my @weekdays = map $language_data{$_}, (31..37);

#### FIGURE OUT WHICH DAY WE WANT
my $year = (gmtime(time))[5] + 1900;
$tmshft *= 3600;
my $offset = getGregorianOffset($year);
$year     = (gmtime(time + $tmshft - ($offset * 24 * 60 * 60)))[5] + 1900;
my $month = (gmtime(time + $tmshft - ($offset * 24 * 60 * 60)))[4] + 1;
my $day   = (gmtime(time + $tmshft - ($offset * 24 * 60 * 60)))[3];

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
$PARSER->parsefile( findBottomUp($language, $filepath) );

#### NEXT, PARSE THE MENAION FILE
$filepath = "xml/";
$filepath .= $today->getMonth() < 10 ? "0" . $today->getMonth() : $today->getMonth();
$filepath .= $today->getDay() < 10 ? "/0" . $today->getDay() : "/" . $today->getDay();
$filepath .= ".xml";

$src = "menaion";
$PARSER->parsefile( findBottomUp($language, $filepath) );

#### WE NOW HAVE A SET OF CIDs, which we are ready to Parse
$src = "";
foreach my $CId (keys %SAINTS) {
	$src = $CId;
	foreach my $file (findTopDown($language, "xml/lives/$CId.xml")) {
		$PARSER->parsefile( $file);
	}
}

##### PROCESS THE FASTING INSTRUCTIONS
##### SET dRank
$dRank = max ( map { $SAINTS{$_}{Type} } keys %SAINTS );

foreach my $file (findTopDown($language, "xml/Commands/Fasting.xml")) {
	$PARSER->parsefile( $file );
}

##### CREATE THE COMPILED DATA STRINGS
## Pentecostarion data
my $PENTECOSTARION_DATA = "";
foreach (keys %SAINTS) {
	next unless $SAINTS{$_}{Reason} eq "pentecostarion";

	# Get the dRank of the observance
	my $l = $SAINTS{$_}{Type};
	$PENTECOSTARION_DATA .= qq(<A Href="JavaScript:openWindow('http://www.ponomar.net/typicon.html');"><IMG Src="http://www.ponomar.net/images/T$l.gif" Alt="$typicon[$l]" Border="0"></A>) . " " if (defined $l);
	$PENTECOSTARION_DATA .= qq(<B>) . $SAINTS{$_}{NAME}{Nominative} . "</B>";
	$PENTECOSTARION_DATA .= " Tone " . $tones[$SAINTS{$_}{Tone}] if ($SAINTS{$_}{Tone});
	$PENTECOSTARION_DATA .= "; ";
}

## Menaion data
my $MENAION_DATA = "";
foreach (keys %SAINTS) {
	next unless $SAINTS{$_}{Reason} eq "menaion";
	
	my $l = $SAINTS{$_}{Type};
	$MENAION_DATA .= qq(<A Href="JavaScript:openWindow('http://www.ponomar.net/typicon.html');"><IMG Src="http://www.ponomar.net/images/T$l.gif" Alt="$typicon[$l]" Border="0"></A>) . " " if (defined $l);
	$MENAION_DATA .= qq(<A Href="JavaScript:doLives($_);">) . $SAINTS{$_}{NAME}{Nominative} . "</A>";
	$MENAION_DATA .= "; ";
}

################### BEGIN SCRIPTURE MEGA-SORTING ALGORITHM #####################
my @order_of_types = ("6th hour", "vespers", "matins", "liturgy");
my @order_of_srcs  = $dow == 6 ? ("menaion", "pentecostarion") : ("pentecostarion", "menaion"); ## FIXME
my %sort_order     = map  { $order_of_srcs[$_] => $_ } (0..$#order_of_srcs);
my @order_of_srcs  = sort { $sort_order{$SAINTS{$a}{Reason}} <=> $sort_order{$SAINTS{$b}{Reason}} || $SAINTS{$b}{Type} <=> $SAINTS{$a}{Type}} keys %SAINTS;

## TODO: IMPLEMENT SUPRESSION AND MOVEMENT OF READINGS
my $READINGS_DATA = "";
foreach my $type (@order_of_types) {
	next unless grep { exists $READINGS{$_}{$type} } @order_of_srcs;
	# $READINGS_DATA .= "<B>" . $scriptTypes{$type} . "</B>: "; # SUPRESSING THIS FOR NOW XXX
	foreach my $source (@order_of_srcs) {
		next unless $READINGS{$source}{$type};
		$READINGS_DATA .= join ("; ", map { formatScriptureReading( $READINGS{$source}{$type}{$_}{Reading}, $READINGS{$source}{$type}{$_}{Pericope} ) } sort { $a cmp $b } keys %{ $READINGS{$source}{$type} });
		# $READINGS_DATA .= " (" . ($SAINTS{$source}{NAME}{Genetive} or $SAINTS{$source}{NAME}{Short}) . "); ";
		$READINGS_DATA .= " ";
	}
	# print "<BR>\n";
}

################################ CONSTRUCT THE OUTPUT #######################################
print "Content-type: text/javascript; charset=utf-8\n\n";

## OUTPUT THE INFORMATION:
print "var old_calendar='" . $today->toString($language) . "';\n";
print "var new_calendar='" . $today->toStringGregorian($language) . "';\n";
print "var x_year='"       . $today->getYear() . "';\n";
print "var ad_creatio='"   . ($today->getYear() + CREATION_OF_THE_WORLD) . "';\n";
print "var n_day='"        . $today->getDay() . "';\n";
print "var n_month='"      . $today->getMonth() . "';\n";
print "var w_day='"        . $weekdays[$today->getDayOfWeek()] . "';\n";
print "var m_saints='"     . encode_utf8(quotemeta($MENAION_DATA)) . "';\n";
print "var paschalcycle='" . encode_utf8(quotemeta($PENTECOSTARION_DATA)) . "';\n";
print "var readings='"     . encode_utf8(quotemeta($READINGS_DATA)) . "';\n";
print "var fastinfo='"     . convert($fast)  . "';\n\n";

print <<END_OF_SCRIPT;
var print_day = function() { lsPrintFlag = 76.27 * Math.random() - Math.PI };

function doSaintsLink()
{
	url = 'http://www.ponomar.net/cgi-bin/index.cgi';
	window.open(url, 'nWindow', 'height=500,width=800,scrollbars=yes,resizable=yes');
}

function doReadings(book, chapter) {
	// LAUNCH LECTIONARY 1.0
	url = 'http://www.ponomar.net/cgi-bin/bible.cgi?mode=specify&lang=$language&book=' + book + '&reading=' + chapter;
	window.open(url, 'bible', 'height=500,width=900,scrollbars=yes,resizable=yes');
}

function printDateDefault()
{
	document.write(w_day + ', ' + old_calendar + ' ' + x_year + ' (' + ad_creatio + ')');
}

function printDateNS()
{
	document.write('&nbsp;[' + new_calendar + ' on the civil calendar]');
}

function printPaschalCycle()
{
	document.write('<B>' + paschalcycle + '</B> ');
}

function printSaints()
{
	document.write(m_saints);
}

function printReadings()
{
	print_day();
	document.write(readings);
}

function printIcon()
{
	document.write('<img src="http://www.ponomar.net/cgi-bin/cache.cgi">');
}

function doPrologue(month, day) {
	// deprecated
}

function doLink()
{
	window.open('http://www.ponomar.net/cgi-bin/index.cgi', 'nHelp', 'height=500,width=800,scrollbars=yes,resizable=yes');
}

function doSaints(id)
{
	var url = 'http://www.ponomar.net/cgi-bin/lives.cgi?id=' + id;
	window.open(url, 'life' + id, 'height=600,width=800,scrollbars=yes,resizable=yes');
}

function openWindow(url)
{
	window.open(url, "mailer", "scrollbars=1, height=600, left=150, top=150, width=500");
}
END_OF_SCRIPT

if ($mode eq "simple") {
print qq(
document.write('<div class="saints"><b>Today is:</b> '); printDateDefault(); printDateNS(); document.write('<br>Today the Church celebrates:<BR><BR>');printPaschalCycle(); printSaints(); document.write('<br><b>Fasting:</b> ' + fastinfo + ' <b>Scripture Reading: </b>');printReadings(); document.write('<br><small>Powered by Ponomar. <A Href="JavaScript:doLink();">Complete online menologion</A>.</small></div>'););
}

