#!/usr/bin/perl -wT

use warnings;
use strict;
use utf8;
use CGI;
use CGI::Carp qw( fatalsToBrowser );

### THIS IS THE MENOLOGION ON YOUR SITE SCRIPT
### FOR REASONS OF BACKWARDS COMPATIBILITY, IT MUST BE NAMED julian.cgi
### IT MUST ALSO USE THE OLD LS THINGS INSTEAD OF LANGUAGE IS0 CODES IN ORDER TO BE COMPATIBLE
### TODO: INTRODUCE A NEW QUERY STRING ITEM (LANG?) TO USE ISO CODES 
### ANNOUNCE THAT LS WILL BE DEPRECATED? 
### THIS WILL PROBABLY ONLY AFFECT MITROPHAN AND A COUPLE OF FRENCH SITES

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

my $bible;

sub formatReading {
	my ($reading, $pericope) = @_;
	
	my ($book, $verses) = split(/_/, $reading);
	my $bookName = $bible->getBookNameShort($book);
	return defined $pericope ? qq(<A Href="JavaScript:doReadings('$book', '$verses');">$bookName $verses</A> (§ $pericope)) : qq(<A Href="JavaScript:doReadings('$book', '$verses');">$bookName $verses</A>);
}

my @weekdays = qw/sunday monday tuesday wednesday thursday friday saturday/;
my @typicon  = ("", "A service with no mark in the Typicon is served for this saint", 
			"Hexastecheiraric service", "Doxology service",
			"Polyeleos service", "Vigil-rank service", "Great feast",
			"Great fest", "Great feast", "Great feast");
			
my $timeshift = 0;

my $q = new CGI;
my $mode   = $q->param("mode");
my $LS = $q->param("LS") || 0;
if ($q->param("timeshift")) {
	$timeshift = $q->param("timeshift");
	if (abs($timeshift) > 15) {
		$timeshift = $timeshift % 12; ## timeshift is in hours
	}
}

## make sure that language is valid!
# untaint
my $language = "en";
if ($LS =~ /([\d]+)/) {
	$LS = $1;
} 

$language = "cu/ru" if $LS == 3;
$language = "fr" if $LS == 1;
$language = "el" if $LS == 2;
$language = "zh/Hans" if $LS == 5;
$language = "zh/Hant" if $LS == 6;

my $today = getToday();
my $year  = $today->getYear();
my $offset = getGregorianOffset($year);
$today     = new Ponomar::JDate(int((time + $timeshift * 3600) / 86400) + 2440588);

my $ponomar = new Ponomar($today, $language);
$bible   = $ponomar->loadBible();

###### FORMAT NECESSAR INFORMATION
# 1. PENTECOSTARION
my $PENTECOSTARION_DATA = "";

my @saints = $ponomar->getSaints('pentecostarion');
for (my $i = 0; $i < @saints; $i++) {
	next unless defined $saints[$i]->getKey('Name');
	my $name = $saints[$i]->getKey('Name')->{Nominative};
	my $type = $saints[$i]->getKey('Type');
		
	$PENTECOSTARION_DATA .= qq(<A Href="JavaScript:openWindow('http://www.ponomar.net/typicon.html');"><IMG Src="http://www.ponomar.net/images/T$type.gif" Alt="$typicon[$type]" Border="0"></A>) if ($type > 0);
	$PENTECOSTARION_DATA .= "<B>" if ($i == 0);	
	$PENTECOSTARION_DATA .= $name;
	$PENTECOSTARION_DATA .= "</B>" if ($i == 0);
	$PENTECOSTARION_DATA .= "; ";
}

$PENTECOSTARION_DATA .= $ponomar->getTone();

# 2. Menaion data
my $MENAION_DATA = "";
my @saints = $ponomar->getSaints('menaion');
for (my $i = 0; $i < @saints; $i++) {
	next unless defined $saints[$i]->getKey('Name');
	my $name = $saints[$i]->getKey('Name')->{Nominative};
	my $type = $saints[$i]->getKey('Type');
	my $id   = $saints[$i]->getKey('CId');
	
	$MENAION_DATA .= qq(<A Href="JavaScript:openWindow('http://www.ponomar.net/typicon.html');"><IMG Src="http://www.ponomar.net/images/T$type.gif" Alt="$typicon[$type]" Border="0"></A>) if ($type > 0);
	$MENAION_DATA .= "<B>" if ($type >= 4);
	$MENAION_DATA .= qq(<A Href="JavaScript:doSaints('$id');">$name</A>);
	$MENAION_DATA .= "</B>" if ($type >= 4);
	$MENAION_DATA .= "; " unless ($i == $#saints);
}

# 3. Readings
my @readings = ();
my @order_of_types = ("prime", "terce", "sexte", "none", "vespers", "matins", "liturgy");
foreach my $service_type (@order_of_types) {
	push @readings, $ponomar->getReadings($service_type);
}

my $READINGS = join('; ', map { formatReading($_->getReading(), $_->getPericope()) } @readings);

# 4. Icon of the day
#### set up icon data
# 1. Build the icon language substitution algorithm
my @ils = ("cu", "el", "zh", "en", "fr");
for (my $i = 0; $i <= $#ils; $i++) {
	last if (split (/\//, $language))[0] eq $_;
	push @ils, shift @ils;
}

my $icon = "http://www.ponomar.net/images/icon.jpg";
OUTERLOOP: foreach my $saint ( sort { $a->getKey('Type') <=> $b->getKey('Type') } $ponomar->getSaints() ) {
	foreach my $l (@ils) {
		if ($saint->hasIcons($l)) {
			my $id = $saint->getKey('CId');
			$icon = "http://www.ponomar.net/cgi-bin/fetch.cgi?lang=$l&saint=$id&icon=thumb";
			last OUTERLOOP;
		}
	}
}

print "Content-type: text/javascript; charset=utf-8\n\n";

## OUTPUT THE INFORMATION:
print "var old_calendar='" . Ponomar::I18n::dateToString($today, $language) . "';\n";
print "var new_calendar='" . Ponomar::I18n::dateToStringGregorian($today, $language) . "';\n";
print "var x_year='"       . $today->getYear() . "';\n";
print "var ad_creatio='"   . $today->getYearAM() . "';\n";
print "var n_day='" . $today->getDay() . "';\n";
print "var n_month='" . $today->getMonth() . "';\n";
print "var w_day='" . Ponomar::I18n::getLocaleKey($weekdays[$today->getDayOfWeek()], $language) . "';\n";
print "var m_saints='" . quotemeta($MENAION_DATA) . "';\n";
print "var paschalcycle='" . quotemeta($PENTECOSTARION_DATA) . "';\n";
print "var readings='" . quotemeta($READINGS) . "';\n";
print "var fastinfo='" . $ponomar->getFastingInstructions()  . "';\n";
print "var iconpath='" . $icon . "';\n\n";

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
	document.write('<img src="' + iconpath + '">');
}

function doLink()
{
	window.open('http://www.ponomar.net/cgi-bin/index.cgi', 'nHelp',  'height=500,width=800,scrollbars=yes,resizable=yes');
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

