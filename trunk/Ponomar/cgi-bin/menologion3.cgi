#!/usr/bin/perl -wT

use warnings;
use strict;

##################################################################################
### menologion3.cgi :: IMPLEMENTS THE RESULTS OF THE GREAT CONVERSION FOR PERL
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
my %language_data = ();

## THIS IS THE MAIN FILE PATH
my $basepath = "/home/ponomar0/svn/Ponomar/languages/";

### THIS STORES ALL OF THE DATA FROM THE XML FILES
tie my %SAINTS, "Tie::IxHash";
tie my %READINGS, "Tie::IxHash";
my @DEBUG = ();
my $whichService = "";

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
	die "Unable to find any instances of $file in the path for $language" unless (@paths);
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
	};
}

sub endElement {
	my ($parseinst, $element, %attrs) = @_;
	
	if ($element eq "VESPERS" || $element eq "MATINS" || $element eq "LITURGY" || $element eq "SERVICE") {
		$whichService = "";
	}
	return;
}

sub formatScriptureReading {
	my ($reading, $pericope) = @_;
	
	my ($book, $verses) = split(/_/, $reading);
	my $MG = exists $matinsGospels{$reading} && $dow == 0 ? " " . $language_data{133 + $matinsGospels{$reading}} : "";
	return defined $pericope ? qq(<A Href="JavaScript:doReadings('$book', '$verses');">$book $verses (§ $pericope)</A>$MG) : qq(<A Href="JavaScript:doReadings('$book', '$verses');">$book $verses</A>$MG);
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
		$PARSER->parsefile( $file );
	}
}

################################ CREATE THE USER'S COOKIE ###################################
my $cookievalue = join("|", ($City, $Lat, $Lon, $TZ, $language, $GS));
my $cookie  = new CGI::Cookie(-name => 'menologion', 
			      -value=> "$cookievalue",
			      -expires=>'+1y',
			      -domain=>'ponomar.net');
			     
print "Set-Cookie: $cookie\n";
############################### NAVIGATION DATA #############################################
my @yesterdayvals = ($yesterday->getMonth(), $yesterday->getDay(), $yesterday->getYear());
my @tomorrowvals  = ($tomorrow->getMonth(), $tomorrow->getDay(), $tomorrow->getYear());

################################ CONSTRUCT THE OUTPUT #######################################
print "Content-type: text/html; charset=utf-8\n\n";
%language_data = General->loadLanguage($language);
General->write_top($month, $day, $year);

print "<CENTER>\n";
print "$language_data{20} <B>" . $today->toStringFull($language) . "</B><BR>";
print "$language_data{21}: " . $today->toStringGregorian($language) . "<BR>";
print "<BR><BR>";

print qq(<A Href="JavaScript:navigate3($yesterdayvals[0], $yesterdayvals[1], $yesterdayvals[2]);"><< Previous day</A>);
print "<IMG Src=\"../images/icon.jpg\" Alt=\"Icons not yet available\">\n";;
print qq(<A Href="JavaScript:navigate3($tomorrowvals[0], $tomorrowvals[1], $tomorrowvals[2]);">Next day >></A></CENTER><BR><BR>);
print "$language_data{24}: $sunrise; $language_data{25}: $sunset $language_data{26} $City (<A Href=\"JavaScript:openWindow('configure.cgi');\">$language_data{27}</A>)<BR>\n";


## print Pentecostarion data
foreach (keys %SAINTS) {
	next unless $SAINTS{$_}{Reason} eq "pentecostarion";

	# Get the dRank of the observance
	print $typicon[$SAINTS{$_}{Type}] . " " if ($SAINTS{$_}{Type});
	print qq(<B>) . $SAINTS{$_}{NAME}{Nominative} . "</B>";
	print " Tone " . $tones[$SAINTS{$_}{Tone}] if ($SAINTS{$_}{Tone});
	print "; ";
}

print "<BR><BR>";
## print the Menaion data
foreach (keys %SAINTS) {
	next unless $SAINTS{$_}{Reason} eq "menaion";
	
	print $typicon[$SAINTS{$_}{Type}] . " " if ($SAINTS{$_}{Type});
	print qq(<A Href="JavaScript:doLives($_);">) . $SAINTS{$_}{NAME}{Nominative} . "</A>";
	print "; ";
}

################### BEGIN SCRIPTURE MEGA-SORTING ALGORITHM #####################
my @order_of_types = ("vespers", "matins", "liturgy");
my @order_of_srcs  = $dow == 6 ? ("menaion", "pentecostarion") : ("pentecostarion", "menaion");
my %sort_order     = map  { $order_of_srcs[$_] => $_ } (0..$#order_of_srcs);
my @order_of_srcs  = sort { $sort_order{$SAINTS{$a}{Reason}} <=> $sort_order{$SAINTS{$b}{Reason}} || $SAINTS{$b}{Type} <=> $SAINTS{$a}{Type}} keys %SAINTS;

print "<BR><BR><DIV Class=\"header\" Align=\"center\">$language_data{29}</DIV><BR>\n";

## TODO: IMPLEMENT SUPRESSION AND MOVEMENT OF READINGS
foreach my $type (@order_of_types) {
	next unless grep { exists $READINGS{$_}{$type} } @order_of_srcs;
	print "<B>" . $type . "</B>: ";
	foreach my $source (@order_of_srcs) {
		next unless $READINGS{$source}{$type};
		print join ("; ", map { formatScriptureReading( $READINGS{$source}{$type}{$_}{Reading}, $READINGS{$source}{$type}{$_}{Pericope} ) } sort { $a cmp $b } keys %{ $READINGS{$source}{$type} });
		print " (for " . $SAINTS{$source}{NAME}{Short} . "); ";
	}
	print "<BR>\n";
}

General->footer_en();
