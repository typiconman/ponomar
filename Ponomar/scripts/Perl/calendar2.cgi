#!/usr/bin/perl -w

use warnings;
use strict;

## THIS IS A REWRITE OF calendar.cgi USING the Ponomar::API
use CGI;
use CGI::Carp qw( fatalsToBrowser );
use CGI::Cookie;

use lib "./";
use lib "./API";
use Ponomar;
use Ponomar::Util;
use Ponomar::I18n;
use Ponomar::JDate;

BEGIN {
	$ENV{PATH} = "/bin:/usr/bin";
	delete @ENV{ qw( IFS CDPATH ENV BASH_ENV ) };
}

my @monthNames = ("january", "february", "march", "april", "may", "june", "july", "august", "september", "october", "november", "december");
my @wd = ("sun", "mon", "tue", "wed", "thu", "fri", "sat");

# get user's cookie
my %cookies     = fetch CGI::Cookie;
my ($City, $Lat, $Lon, $TZ, $language, $GS);

if ($cookies{"menologion"}) {
	my @temp = split(/\|/, $cookies{"menologion"}->value());
	if (@temp == 6) {
		($City, $Lat, $Lon, $TZ, $language, $GS) = @temp;
	}
}

# PREVENT HACKING
if ($language =~ /^([a-zA-Z\/]+)$/) {
	$language = $1;
} else {
	$language = "en";
}

## GET PARAMETERS
my $q = new CGI;
my $today = getToday();
my $year = $q->param("year") || $today->getYear();
my $month = $q->param("month") || $today->getMonth();
my $style  = $q->param("style") || 0; # 0 -- Julian, 1 -- Gregorian

###### CREATE CALENDAR CONTROL
print "Content-type: text/html; charset=utf-8\n\n";
print "<HTML><HEAD><TITLE>CALENDAR FOR " . Ponomar::I18n::getLocaleKey($monthNames[$month - 1], $language) . ' ' . $year . "</TITLE>";
print "<LINK Rel=stylesheet Type=text/css Href=\"http://www.ponomar.net/new.css\">\n";
print "<SCRIPT Language=\"JavaScript\" Src=\"http://www.ponomar.net/new.js\"></SCRIPT>\n";
print qq(<META Http-equiv="content-type" Content="text/html; charset=utf-8">\n);
print "</HEAD><BODY MarginWidth=0 MarginHeight=0 LeftMargin=0 TopMargin=0 BgColor=#003366>\n";
print "<DIV Class=\"select\"><FORM Name=\"Navigator\" Method=\"get\" Action=\"calendar2.cgi\">";

## BUILD THE NAVIGATION
print "<SELECT Id=\"month\" Name=\"month\">";

for (my $j = 1; $j <= 12; $j++) {
	print "<OPTION Value=$j";
	if ($month == $j) { print " Selected"; }
	print ">" . Ponomar::I18n::getLocaleKey($monthNames[$j - 1], $language) . "</OPTION>\n";
}
print "</SELECT>&nbsp;<INPUT Type=\"text\" Name=\"year\" Size=5 Width=5 Value=\"$year\">";
## FIXME: Add Go to locale!
print "&nbsp;<INPUT Type=\"submit\" Value=\"Go\"></DIV>\n";

## BUILD THE TABLE
print "<TABLE Width=100% CellPadding=0 CellSpacing=0 Border=0><TR>";
foreach my $day (@wd) {
	print "<TD Class=\"wday\" Width=14.2%>" . Ponomar::I18n::getLocaleKey($day, $language) . "</TD>";
}
print "</TR><TR>";

### START BUILDING CALENDAR
my $start = new Ponomar::JDate($month, 1, $year);
my $end = $start->addMonths(1);
$start = $start->subtractDays(getGregorianOffset($year)) if ($style == 1);
$end   = $end->subtractDays(getGregorianOffset($year)) if ($style == 1);
my $wstart = $start->getDayOfWeek();
for (my $i = 0; $i < $wstart; $i++) {
	print "<TD Class=\"blank\">&nbsp;</TD>";
}



for (my $date = $start; $date < $end; $date++) {
	my @link = ($date->getMonth(), $date->getDay(), $date->getYear());
	my $i   = $style ? $date->getDayGregorian() : $date->getDay();
	
	my $ponomar = new Ponomar($date, $language);
	## get the highest ranked saint of the day

	my $index   = argmax { $_->getKey('Type') } $ponomar->getSaints();
	## get the name and dRank
	my $dRank   = $index->getKey('Type');
	my $name    = $index->getKey('Name')->{Genetive} || $index->getKey('Name')->{Nominative};
	
	# get the fasting instructions
	my @bits    = split(//, $ponomar->getFastingCode());
	my $class = "";
	if ($bits[0] == 1) {
		$class = 'nofast';
	} elsif ($bits[1] == 1) {
		$class = 'maslenitsa';
	} else {
		$class = 'fast';
	}
	
	$class = 'feast' if ($dRank >= 6);
	my $dow = $date->getDayOfWeek();
	if ($dow == 0) {
		# sunday
		$class .= 'sun' unless ($class eq 'feast');
		print qq(<TR><TD Class="$class"><A Title="$name" Href="JavaScript:goDay($link[0], $link[1], $link[2]);">$i</A></TD>);
	} elsif ($dow == 6) {
		# saturday
		print qq(<TD Class="$class"><A Title="$name" Href="JavaScript:goDay($link[0], $link[1], $link[2]);">$i</A></TD></TR>);
	} else {
		print qq(<TD Class="$class"><A Title="$name" Href="JavaScript:goDay($link[0], $link[1], $link[2]);">$i</A></TD>);
	}
}

for (my $i = $end->getDayOfWeek(); $i <= 6 && $i > 0; $i++) {
	print "<TD Class=\"blank\">&nbsp;</TD>";
}
print "</TR>" unless ($end->getDayOfWeek() == 0);

print "</TABLE><DIV Class=\"select\">";
if ($style == 0) {
	print "<INPUT Type=\"radio\" Name=\"style\" Value=0 Checked>Julian";
	print "<INPUT Type=\"radio\" Name=\"style\" Value=1>Gregorian";
} else {
	print "<INPUT Type=\"radio\" Name=\"style\" Value=0>Julian";
	print "<INPUT Type=\"radio\" Name=\"style\" Value=1 Checked>Gregorian";
}

print "</DIV></FORM></BODY></HTML>";
