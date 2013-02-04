#!/usr/bin/perl

### THIS IS THE MAIN SCRIPT FOR THE PONOMAR.NET WEBSITE
#   IT SHOWS HOW THE PONOMAR PERL API CAN BE EFFECTIVELY USED IN A WEB CONTEXT
###
### COPYRIGHT ALEKSANDR ANDREEV WITH THE USUAL DISCLAIMERS THAT NOTHING EVER HAS ANY WARRANTY
###
###

use warnings;
use strict;
use bytes; # UGH! use bytes IS UGLY, BUT WE HAVE NO CHOICE. W/OUT IT XML::Parser GOES BONKERS AND KILLS UTF-8
use CGI;
use CGI::Carp qw( fatalsToBrowser );
use CGI::Cookie;

use lib "./";
use lib "./API";
use Ponomar;
use Ponomar::Util;
use Ponomar::I18n;
use Ponomar::JDate;
use General;

my ($City, $Lat, $Lon, $TZ, $language, $GS);

BEGIN {
	$ENV{PATH} = "/bin:/usr/bin";
	delete @ENV{ qw( IFS CDPATH ENV BASH_ENV ) };
}

my %language_data  = ();
my @typicon  = ("", "✺", "<FONT Face=\"Hirmos Ponomar\">🕃</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕃</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕂</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕁</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕀</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕀</FONT>", "<FONT Face=\"Hirmos Ponomar\" Color=\"red\">🕀</FONT>");
my $bible;

sub formatScriptureReading {
	my ($reading, $pericope) = @_;
	
	my ($book, $verses) = split(/_/, $reading);
	my $MG = defined getMatinsGospel($reading) ? " " . Ponomar::I18n::getLocaleKey('MG' . getMatinsGospel($reading), $language) : "";

	my $bookName = $bible->getBookNameShort($book);
	return defined $pericope ? qq(<A Href="JavaScript:doReadings('$book', '$verses');">$bookName $verses</A> (§ $pericope)) . $MG : qq(<A Href="JavaScript:doReadings('$book', '$verses');">$bookName $verses</A>) . $MG;
}

my $q = new CGI;

my %cookies = fetch CGI::Cookie;
if ($cookies{"menologion"}) {
	my @temp = split(/\|/, $cookies{"menologion"}->value());
	if (@temp == 6) {
		($City, $Lat, $Lon, $TZ, $language, $GS) = @temp;
	}
}

my $tmshft = $q->param("tmshft") % 25 || 0;
$tmshft += $TZ;
my $today = getToday($tmshft);
my $year = $q->param("year") || $today->getYear();
my $month = $q->param("month") || $today->getMonth();
my $day   = $q->param("day") || $today->getDay();

my $validchars = "0-9";
if ($language =~ /^([a-zA-Z\/]+)$/) {
	$language = $1;
} else {
	$language = "en";
}

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

$today = new Ponomar::JDate($month, $day, $year);
%language_data = General->loadLanguage($language);
my $ponomar = new Ponomar($today, $language);
$bible   = $ponomar->loadBible();

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
	
################################ CREATE THE USER'S COOKIE ###################################
my $cookievalue = join("|", ($City, $Lat, $Lon, $TZ, $language, $GS));
my $cookie  = new CGI::Cookie(-name   => 'menologion', 
			      -value  => "$cookievalue",
			      -expires=> '+1y',
			      -domain => 'ponomar.net');
			     
print "Set-Cookie: $cookie\n";

################################ CONSTRUCT THE OUTPUT #######################################
print "Content-type: text/html; charset=utf-8\n\n";

General->write_top($month, $day, $year);

print "<CENTER>\n";
print "$language_data{20} <B>" . Ponomar::I18n::dateToStringFull($today, $language) . "</B><BR>";
print "$language_data{21}: " . Ponomar::I18n::dateToStringGregorian($today, $language) . "<BR>";
print "<BR><BR>";

################# SET UP NAVIGATION ###############
my $yesterday = $today->subtractDays(1);
my $tomorrow  = $today->addDays(1);
my @yesterdayvals = ($yesterday->getMonth(), $yesterday->getDay(), $yesterday->getYear());
my @tomorrowvals  = ($tomorrow->getMonth(), $tomorrow->getDay(), $tomorrow->getYear());
my ($sunrise, $sunset) = $today->getSunriseSunset($Lon, $Lat, $TZ);

print qq(<A Href="JavaScript:navigate3($yesterdayvals[0], $yesterdayvals[1], $yesterdayvals[2]);">&lt;&lt; $language_data{22}</A>);
print "<IMG Src=\"$icon\" Alt=\"Icon fetcher\">\n";;
print qq(<A Href="JavaScript:navigate3($tomorrowvals[0], $tomorrowvals[1], $tomorrowvals[2]);">$language_data{23} &gt;&gt;</A><BR>);

#### GET COMPLEX NAVIGATION STUFF
my @this_date_last_year = ($today->getMonth(), $today->getDay(), $today->getYear() - 1);
my @this_date_next_year = ($today->getMonth(), $today->getDay(), $today->getYear() + 1);
my $tmp = Ponomar::Util::getThisDayPreviousYear($today);
my @this_day_last_year  = ($tmp->getMonth(), $tmp->getDay(), $tmp->getYear);
$tmp    = Ponomar::Util::getThisDayNextYear($today);
my @this_day_next_year  = ($tmp->getMonth(), $tmp->getDay(), $tmp->getYear);
$tmp    = Ponomar::Util::getNextYearWithSamePascha($today->getYear());
my @next_same_combo     = ($today->getMonth(), $today->getDay(), $tmp);
$tmp    = Ponomar::Util::getPreviousYearWithSamePascha($today->getYear());
my @last_same_combo     = ($today->getMonth(), $today->getDay(), $tmp);

print qq(<A Href="JavaScript:navigate3($this_date_last_year[0], $this_date_last_year[1], $this_date_last_year[2]);">&lt; Same date last year</A>
<A Href="JavaScript:navigate3($this_day_last_year[0], $this_day_last_year[1], $this_day_last_year[2]);">&lt; Same day last year</A>
<A Href="JavaScript:navigate3($last_same_combo[0], $last_same_combo[1], $last_same_combo[2]);">&lt; Last same combination</A> |
<A Href="JavaScript:navigate3($next_same_combo[0], $next_same_combo[1], $next_same_combo[2]);">Next same combination &gt;</A>
<A Href="JavaScript:navigate3($this_day_next_year[0], $this_day_next_year[1], $this_day_next_year[2]);">Same day next year &gt;</A>
<A Href="JavaScript:navigate3($this_date_next_year[0], $this_date_next_year[1], $this_date_next_year[2]);">Same date next year &gt;</A></CENTER><BR><BR>);

print "$language_data{24}: $sunrise; $language_data{25}: $sunset $language_data{26} $City (<A Href=\"JavaScript:openWindow('configure.cgi');\">$language_data{27}</A>)<BR>\n";
print $ponomar->getFastingInstructions() . "<BR>\n";

my @saints = $ponomar->getSaints('pentecostarion');
for (my $i = 0; $i < @saints; $i++) {
	next unless defined $saints[$i]->getKey('Name');
	my $name = $saints[$i]->getKey('Name')->{Nominative};
	my $type = $saints[$i]->getKey('Type');
		
	print $typicon[$type] . " " if ($type);
	print qq(<B>) . $name . "</B>";
	print "; ";
}
print $ponomar->getTone();

print "<BR><BR>";

@saints   = $ponomar->getSaints('menaion');
for (my $i = 0; $i < @saints; $i++) {
	next unless defined $saints[$i]->getKey('Name');
	my $name = $saints[$i]->getKey('Name')->{Nominative};
	my $type = $saints[$i]->getKey('Type');
	my $id   = $saints[$i]->getKey('CId');
	
	print $typicon[$type] . " " if ($type);
	if ($type >= 5) {
		print qq(<A Href="JavaScript:doLives($id);"><B><FONT Color="red">) .
			$name . "</FONT></B></A>";
	} elsif ($type >= 4) {
		print qq(<A Href="JavaScript:doLives($id);"><B>) .
			$name . "</B></A>";
	} else {	
		print qq(<A Href="JavaScript:doLives($id);">) . $name . "</A>";
	}
	print "; ";
}

################### BEGIN SCRIPTURE MEGA-SORTING ALGORITHM #####################
print "<BR><BR><DIV Class=\"header\" Align=\"center\">$language_data{29}</DIV><BR>\n";

foreach ($ponomar->getSaints('pentecostarion')) {
	next unless ($_->getKey('CId') >= 9000 && $_->getKey('CId') <= 9315);
	my @services = $_->getServices();
	foreach my $service (@services) {
		## EXECCOMMANDS TAKES DRANK ARGUMENT
		## USER MUST SET DRANK BASED ON WHAT SERVICE HE'S DOING
		## FOR NOW, WE'RE USING THE MAX AVAILABLE DRANK
		$service->execCommands( max (  map { $_->getKey("Type") } $ponomar->getSaints() ) );
	}
}

my @order_of_types = ("prime", "terce", "sexte", "none", "vespers", "matins", "liturgy");
my @order_of_reads = $today->getDayOfWeek() == 6 ? ("menaion", "pentecostarion") : ("pentecostarion", "pentecostarion2", "pentecostarion3", "menaion");

foreach my $service_type (@order_of_types) {
	next unless grep { $_->hasReadings() } map { $_->getServices($service_type) } $ponomar->getSaints();
	print "<B>" . Ponomar::I18n::getLocaleKey($service_type, $language) . '</B>: ';

	foreach my $comm_type (@order_of_reads) {

		my @saints = $ponomar->getSaints($comm_type);
		foreach my $saint (@saints) {
			next unless $saint->hasServices($service_type);
			my @services = $saint->getServices($service_type);
			next unless grep { $_->hasReadings() } @services;
		
			foreach my $service (@services) {
				next unless $service->hasReadings();
				my @readings = $service->getReadings();
				
				foreach my $reading (@readings) {
					print formatScriptureReading($reading->getReading(), $reading->getPericope());
				
					if (defined $reading->getEffWeek() || grep { $_->getSaint() ne $saint->getKey('CId') } @readings) {
						my $descript;
						if ($reading->getSaint() ne $saint->getKey('CId')) {
							my $saint = Ponomar::Saint->new( CId => $reading->getSaint(), Src => 'nameonly', Date => $today, Lang => 'en' );
							$descript = $saint->getKey('Name')->{Nominative};
						} else {
							$descript = $saint->getKey('Name')->{Nominative};
						}
						my $effWeek  = $reading->getEffWeek();
						$descript =~ s/\d+/$effWeek/ if (defined $effWeek);
						print " ($descript); ";
					} else {
						print "; ";
					}
				}
			} # service
			unless (grep { eval { defined $_->getEffWeek() || $_->getSaint() ne $saint->getKey('CId') } } map { $_->getReadings() } $saint->getServices()) {
				my $name = "";
				$name = ($saint->getKey('Name')->{Genetive} or $saint->getKey('Name')->{Short}) if (defined $saint->getKey('Name'));
				print " (" . $name . "); ";
			} else {
				print "; ";
			}
		
		} #saint
	} # commemoration type
	print "<BR>";
} # service type

############# DEBUGGING INFORMATION ####################
#print qq(<!-- dow: $dow; doy: $doy; nday: $nday; ndayP: $ndayP; ndayF: $ndayF; dRank: $dRank -->);
General->footer_en();

