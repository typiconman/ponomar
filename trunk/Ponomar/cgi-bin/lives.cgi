#!/usr/bin/perl -wT

use warnings;
use strict;
use bytes;

use CGI;
use CGI::Carp qw( fatalsToBrowser );
use CGI::Cookie;
use XML::Parser;
use lib "./";
use General;

BEGIN {
	$ENV{PATH} = "/bin:/usr/bin";
	delete @ENV{ qw( IFS CDPATH ENV BASH_ENV ) };
}

########################################### GLOBAL DEFINITION VARIABLES ##################
use constant false => 0;
my %language_data = ();
my %versions = (
	"en" => "English",
	"fr" => "fran&ccedil;ais",
	"cu/ru" => "русский", 
	"el" => "Ελληνικά"
	);

## THIS IS THE MAIN FILE PATH
my $basepath = "/home/ponomar0/svn/Ponomar/languages/";

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

####################################### GLOBAL SUBS ##############################
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

####### PARSER INFORMATION
my %DATA = ();
my $readLife = false;

sub default {
	return;
}

sub startElement {
	my ($parseinst, $element, %attrs) = @_;
	
	if ($attrs{Cmd}) {
		# remember that in perl, variable names must start with a $
		# edit the Cmd
		my $cmd = $attrs{Cmd};
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		return unless eval($cmd);
	}
	SWITCH: {
		if ($element eq "NAME") {
			delete $attrs{Cmd};
			@{ $DATA{NAME} }{keys %attrs} = values %attrs;
			last SWITCH;
		}
		if ($element eq	"LIFE") {
			delete $attrs{Cmd};
			# my $lifeID = $attrs{Id} || 0;
			### FIXME: ALLOW FOR MULTIPLE LIVES BASED ON SOURCES
			## IGNORED FOR NOW
			$readLife = not false;
			@{ $DATA{LIFE} }{keys %attrs} = values %attrs;
			$DATA{LIFE}{TEXT} = "";
			last SWITCH;
		}
	};
}

sub endElement {
	my ($parseinst, $element, %attrs) = @_;
	
	if ($element eq "LIFE") {
		$readLife = false;
	}
}

sub text {
	my ($parseinst, $data) = @_;
	
	if ( $readLife ) {
		# substitution
		### FIXME: ALLOW FOR MULTIPLE LIVES BASED ON SOURCES
		$DATA{LIFE}{TEXT} .= ($data);
	}
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

#### FIGURE OUT WHAT WE'RE LOOKING FOR
my $CId = $q->param("id");
die "You did not specify an ID" unless ($CId);
my $lang = $q->param("language") || $language;

#### UNTAINT VARIABLES PASSED VIA THE QUERY STRING
my $validchars = "0-9";
if ( $CId =~ /^([$validchars]+)$/ ) {
	$CId = $1;
} else {
	die "This is not a valid ID. Execution stopped ";
}
$validchars = "a-zA-Z0-9/";
if ( $lang =~ /^([$validchars]+)$/ ) {
	$lang = $1;
} else {
	die "This is not a valid language.";
}

#### SET UP THE PARSER
my $PARSER = new XML::Parser(ErrorContext => 2);
$PARSER->setHandlers(	Start   => \&startElement,
			End     => \&endElement,
			Char    => \&text,
			);

#### PARSE THE LIFE FILES
foreach my $file (findTopDown($lang, "xml/lives/$CId.xml")) {
	$PARSER->parsefile( $file );
}

#### GET THE ICON OF THE SAINT
# 1. Build the icon language substitution algorithm
my @ils = ("cu", "el", "zh", "en", "fr");
for (my $i = 0; $i <= $#ils; $i++) {
	last if (split (/\//, $lang))[0] eq $_;
	push @ils, shift @ils;
}

# 2. Find out if we have an icon available
my $icon = "http://www.ponomar.net/images/icon.jpg";
foreach my $l (@ils) {
	eval {
		findBottomUp( $l, "icons/$CId/" );
	};
	unless ($@) {
		$icon = "http://www.ponomar.net/cgi-bin/fetch.cgi?lang=$l&saint=$CId&icon=0";
		last;
	}
}

################################ CREATE THE USER'S COOKIE ###################################
my $cookievalue = join("|", ($City, $Lat, $Lon, $TZ, $language, $GS)); ## XXX: language, not lang here
my $cookie  = new CGI::Cookie(-name   => 'menologion', 
			      -value  => "$cookievalue",
			      -expires=> '+1y',
			      -domain => 'ponomar.net');
			     
print "Set-Cookie: $cookie\n";

################################ CONSTRUCT THE OUTPUT #######################################
print "Content-type: text/html; charset=utf-8\n\n";
%language_data = General->loadLanguage($language);

print <<END_OF_HTML;
<HTML>
<HEAD>
<TITLE>Lives of the saints</TITLE>
<LINK Rel="stylesheet" Type="text/css" Href="http://www.ponomar.net/new.css"/>
<SCRIPT Langauge="JavaScript" Src="http://www.ponomar.net/new.js"/></SCRIPT>
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-12187460-2");
pageTracker._setDomainName("none");
pageTracker._setAllowLinker(true);
pageTracker._trackPageview();
} catch(err) {}</script>
</HEAD>
<BODY MarginWidth=0 MarginHeight=0 LeftMargin=0 TopMargin=0 BgColor="#FFFFFF">
<TABLE CellPadding=0 CellSpacing=0 Width="100%" BgColor="#F6F6F6">
	<TR>
		<TD Class="calframe" VAlign="top" Width=22%>
		<!-- Navigation -->
		<FORM Name="lives" Action="http://www.ponomar.net/cgi-bin/lives.cgi" Method="Get" Id="navigate">
		<INPUT Type="hidden" Name="mode" Value="specify">
		<INPUT Type="submit" Name="submitbtn" Value="Submit" Style="display: none;">
		<INPUT Type="hidden" Name="id" Value="$CId">
		<strong>Language selection</strong>:<br>
END_OF_HTML

## PRINT THE DIFFERENT VERSIONS FIXME: CHECK IF LIFE EXISTS IN EACH LANGUAGE
print "<SELECT Name=\"language\" OnChange=\"JavaScript:changeMe();\">\n";

foreach (sort keys %versions) {
	my $selected = ($language eq $_) ? "Selected" : "";
	print "<OPTION Value=\"$_\" $selected > $versions{$_} </OPTION>\n";
}

print "</SELECT></FORM><BR><BR>\n";

print "&nbsp;<BR><BR><CENTER>";
print qq(<IMG Src="$icon" Alt="fetcher" Border="1">);
print "</CENTER><p><small><A Href=\"iconviewer.cgi?id=$CId\">More Icons >></A></small></p></TD><TD VAlign=\"top\" Class=\"mainframe\">\n";

####### TODO: ## put toolbar here ##
print "<DIV Class=\"header\" Align=\"center\">$DATA{NAME}{Nominative}</DIV><BR>";
print "<SMALL>Source: $DATA{LIFE}{Copyright}</SMALL><BR>\n" if ($DATA{LIFE}{Copyright});
print defined $DATA{LIFE}{TEXT} ? $DATA{LIFE}{TEXT} : "Alas, we have not yet uploaded life information for this Saint. Please be patient.";
General->footer_en();

