package Ponomar::Util;

=head1 Ponomar::Util

Ponomar::Util - Exports utility functions for Ponomar API.

=head3 DESCRIPTION

This is not an Object Oriented class, but rather is a set of utility functions for the Ponomar API. All useful methods are exported from this class via the Exporter interface.

=cut

use strict;
use utf8;
require 5.004;
use POSIX qw(floor);
use Carp;
# use File::ShareDir 'dist_dir';
require Exporter;
require Ponomar::JDate;
use vars qw (@ISA @EXPORT_OK %EXPORT_TAGS @EXPORT $VERSION $basepath);

## $basepath IS THE PATH TO THE ROOT OF THE XML DATA. YOU WILL NEED TO SET THIS VARIABLE
BEGIN {
	$VERSION = 0.01;
	@ISA 	 = qw( Exporter );
	@EXPORT  = qw( getPascha getGregorianOffset findBottomUp findTopDown getToday max argmax isNumeric getMatinsGospel julianFromGregorian getNextYearWithBoundary getKeyOfBoundaries);
	@EXPORT_OK = qw(getIndiction getSolarCycle getConcurrent getLunarCycle getFoundation getEpacta getNextFullMoon getVernalEquinox getJulianDayFromMilankovich getMilankovichPascha);
	$basepath = "/home/sasha/Documents/ponomar/Ponomar/languages/"; # dist_dir('Ponomar');
}

my %matinsGospels = (
	"Mt_28:16-20" => 1,
	"Mk_16:1-8" => 2,
	"Mk_16:9-20" => 3,
	"Lk_24:1-12" => 4,
	"Lk_24:12-35" => 5,
	"Lk_24:36-53" => 6,
	"Jn_20:1-10" => 7,
	"Jn_20:11-18" => 8,
	"Jn_20:19-31" => 9,
	"Jn_21:1-14" => 10,
	"Jn_21:15-25" => 11
);

## some auxilliary functions for doing math
sub pi () { 4 * CORE::atan2(1, 1) }

sub deg2rad {
	my $n = shift;
	return $n * pi () / 180;
}

sub rad2deg {
	my $n = shift;
	return $n * 180 / pi ();
}

=head3 METHODS

=over 4

=item findBottomUp ($language, $file)

THIS ALGORITHM IMPLEMENTS BOTTOM-UP READING OF FILES IN THE XML PATH. THE FULL IMPLEMENTATION IS DESCRIBED BY YURI IN "A Description", p. 27.

Basically, we begin with C<< basepath/<language>/<script>/<locale>/file >> and go until C<basepath/file>, stopping at the first occurance of file, which is then read.

PARAMETERS PASSED TO HERE: C<$language> is the locale, e.g. cu/ru or zh/Hans. C<$file> is e.g., C<xml/01/01.xml> or C<xml/pentecostarion/01.xml>.

Returns: the full path to the file if found. Croaks if no file was found in the Bottom-up path

=cut

sub findBottomUp {
	my ($language, $file) = @_;
	
	# we have a path like language: cu/ru or zh/Hans
	# file: xml/01/01.xml
	my @parts = split (/\//, $language);
	for (my $j = $#parts; $j >= 0; $j--) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/$file";
		return $path if (-e $path);
	}
#warn "Foudn $basepath/$file" if (-e $basepath . $file);
	return $basepath . $file if (-e $basepath . $file);
	croak (__PACKAGE__ . "::findBottomUp($language, $file) : unable to find file");
}

=item findTopDown ($language, $file)

THIS ALGORITHM IMPLEMENTS THE TOP-DOWN APPROACH FOR READING FILES DESCRIBED BY YURI IN op. cit., p. 28

WE CREATE AN ARRAY OF ALL EXTANT FILES NAMED C<$file> IN ALL PATHS BEGINNING WITH BASEPATH AND UP TO 

C<< $basepath/<language>/<script>/<locale>/file >>

PARAMETERS: SAME AS ABOVE. Returns: an array of all files in the top-down path. Carps and returns an empty array if no files were found.

=cut

sub findTopDown {
	my ($language, $file) = @_;
	
	my @paths = ();
	push @paths, $basepath . $file if (-e $basepath . $file);
	my @parts = split(/\//, $language);
	for (my $j = 0; $j < @parts; $j++) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/" . $file;
		push @paths, $path if (-e $path);
	}

	carp (__PACKAGE__ . "::findTopDown($language, $file) : unable to find any instances") unless (@paths);
	return @paths;
}

=item exists_saint($cid)

Checks if a saint with CId C<$cid> exists in the file structure.
Returns C<1> if saint exists and C<0> otherwise.

=cut

sub exists_saint {
	my $cid = shift;

	unless (defined $cid && $cid =~ /\d+/) {
		carp (__PACKAGE__ . "::exists_saint($cid) : invalid CId specified" );
	}

	# XXX: create a separate sub that checks valid languages first, and get rid
	# of hard-coded dependency.
	my @paths = map { "$basepath/$_/lives/$cid.xml" } ('', 'en', 'cu', 'cu/ru', 'el', 'fr', 'zh');
	return scalar map { -e $_ } @paths;
}

=item getPascha($year)

Returns a new Ponomar::JDate object with Pascha for the specified C<$year>.

=cut

sub getPascha ($) {
	my $inyear = shift;

	#Use the Gaussian formulae to calculate the Alexandria Paschallion
	my $a = $inyear % 4;
	my $b = $inyear % 7;
	my $c = $inyear % 19;
	my $d = (19 * $c + 15) % 30;
	my $e = (2 * $a + 4 * $b - $d + 34) % 7;
	my $f = int(($d + $e + 114) / 31); #Month of pascha e.g. march=3
	my $g = (($d + $e + 114) % 31) + 1; #Day of pascha in the month
	return new Ponomar::JDate($f, $g, $inyear);
}

=item getGregorianOffset($year)

Returns an Integer indicating by how many days the Gregorian calendar is ahead of the (potentially proleptic) Julian calendar in C<$year>.

=cut

sub getGregorianOffset ($) {
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

=item getToday( [$timeshift] )

Returns a Ponomar::JDate object with the date of Today according to the System clock.

Optional parameter C<$timeshift> indicates a Time zone shift from UTC in hours.

B<WARNING>: Ponomar::Util relies on C<time> to set Today. It assumes that the system's epoch begins
on 00:00:00 UTC, January 1, 1970 (GREGORIAN!). It has recently come to my attention that this is not true for all systems. I know of no way to get around this problem, so this should be considered a bug.

=cut

sub getToday {
	## WE SHALL ASSUME THAT THE EPOCH BEGINS ON JANUARY 1, 1970
	## THIS IS JULIAN DAY 2440588
	my $timeshift = shift;
	
	return new Ponomar::JDate(int((time + $timeshift * 60 * 60) / 86400) + 2440588);
}

=item julianFromGregorian( $month, $day, $year )

Given a C<$month>, C<$day>, and C<$year> on the B<Gregorian> calendar, 
returns a JDate object with the Julian day for this date.
If date is before 1582, the function croaks.

The formulae are from Meuss, p. 61.

=cut

sub julianFromGregorian {
	my ($month, $day, $year) = @_;
	
	croak (__PACKAGE__ . "::julianFromGregorian($month, $day, $year) - Year is before 1582.") if ($year < 1582);
	
	if ($month < 3) {
		$month += 12;
		$year -= 1;
	}
	my $a = int($year / 100);
	my $b = 2 - $a + int($a / 4);
	return new Ponomar::JDate( int(365.25 * ($year + 4716)) + int(30.6001 * ($month + 1)) + $day + $b - 1524 );
}

=item getNextYearWithSamePascha ( $year )

Given C<$year>, a year AD, returns the next year when Pascha occurs on the same date as this year.

=cut

sub getNextYearWithSamePascha {
	my $year = shift;
	my $oldyear = $year;
	my $oldkey = getKeyOfBoundaries($year);
	
	while ($year - $oldyear < 533) {
		# get the next year that has the same concurrent.
		if ($year % 4 == 2) {
			$year += 11;
		} elsif ($year % 4 == 3) {
			$year += 5;
		} else {
			$year += 6;
		}
	
		# compute the key of boundaries in this year
		my $newkey = getKeyOfBoundaries($year);
		last if ($newkey eq $oldkey);
	}
	return $year;
}

=item getPreviousYearWithSamePascha ($year)

Given C<$year>, a year AD, returns the previous year when Pascha occurs on the same day

=cut

sub getPreviousYearWithSamePascha {
	my $year = shift;
	my $oldyear = $year;
	my $oldkey = getKeyOfBoundaries($year);
	
	while ($oldyear - $year < 533) {
		# get the next year that has the same concurrent.
		if ($year % 4 == 2) {
			$year -= 5;
		} elsif ($year % 4 == 3) {
			$year -= 11;
		} else {
			$year -= 6;
		}
	
		# compute the key of boundaries in this year
		my $newkey = getKeyOfBoundaries($year);
		last if ($newkey eq $oldkey);
	}
	return $year;
}


=item getNextYearWithBoundary ($i, $Year)

Solves a reverse computus problem by returning the next year after C<$Year> when pascha occurs C<$i> days after March 22.

=cut

## TODO: THIS CODE NEEDS TO BE FIXED.
sub getNextYearWithBoundary {
	my $i = shift;
	my $Year = shift;

	Carp::croak ("Invalid arguments") if ($i < 0 || $i > 35);
	my $newyear = $Year + 1;
	while ($newyear - $Year < 533) {
		my $pascha = getPascha($newyear);
		last if ($pascha->getDaysSince(new Ponomar::JDate(3, 22, $newyear)) == $i);
		$newyear++;
	}
	return $newyear;
}

=item getThisDayNextYear ($jdate)

Given C<$jdate>, a date on the Julian calendar, returns a date next year that is same number of days away
from next year's Pascha.

=cut

sub getThisDayNextYear {
	my $date = shift;
	
	my $pascha = getPascha($date->getYear());
	my $nday   = $date->getDaysSince($pascha);
	return getPascha($date->getYear() + 1)->addDays($nday);
}

=item getThisDayPreviousYear ($jdate)

Given C<$jdate>, a date on the Julian calendar, returns a date in the previous year that is the same number 
of days away from the previous year's Pascha.

=cut

sub getThisDayPreviousYear {
	my $date = shift;
	
	my $pascha = getPascha($date->getYear());
	my $nday   = $date->getDaysSince($pascha);
	return getPascha($date->getYear() - 1)->addDays($nday);
}
	
=item max(@array)

Given an C<@array> of real numbers, returns the maximum.

=cut

sub max {
	my $max = shift;
	for ( @_ ) { $max = $_ if $max < $_; }
	return $max;
}

=item argmax($code, @array)

Given an C<@array> of objects, returns the object that maximizes the function given by C<$code> (the argument of the maximum).

Note that C<$code> need not specify a function that is injective. However, if the argmax is not unique, only the first maximizer will be returned (this is a bug?).

If the argmax is the empty set, C<undef> is returned. However, by construction this would never occur, unless C<$code> returns something that is not comparable or C<@array> is C<undef>, since the argmax on an empty domain is necessarily empty (or is it?). I should have paid attention in Analysis ...

E.g.:

 argmax { $_->getKey('Type') } $ponomar->getSaints();

Returns the highest-ranked Saint of the day.

=cut

sub argmax (&@) {
	return () unless @_ > 1;

	my $index = undef;
	my $max   = undef;
	my $block = shift;

	for (@_) {
		my $val = $block->($_);
		if ( not defined $max or $val > $max) {
			$max = $val;
			$index = $_;
		}
	}
	
	return $index;
}

=item isNumeric($variable)

Given a C<$variable> returns true if C<$variable> is a real number.

=cut

sub isNumeric {
	my $var = shift;
	return $var =~ /^-?\d+\.?\d*$/;
}


=item getMatinsGospel($reading)

Given a C<$reading>, which a String of the type returned by a Reading object, returns the Matins Gospel number. If the reading is not a matins gospel, returns C<undef>.

B<FIXME>: THIS SHOULD ACTUALLY TAKE THE READING OBJECT AND CHECK IF IT'S SUNDAY AND MATINS!!

=cut

sub getMatinsGospel {
	my $reading = shift;

	return $matinsGospels{$reading};
}

=item getIndiction( $year )

Given C<$year>, a year AD, returns the Indiction of the year.
Carps if C<$year> is before 313 AD.

=cut

sub getIndiction {
	my $year = shift;
	
	carp (__PACKAGE__ . "::getIndiction($year) - Year is before 312 AD!") if ($year < 313);
	my $indikt = ($year - 312) % 15;
	$indikt += 15 if ($indikt == 0);
	return $indikt;
}

=item getSolarCycle( $year )

Given C<$year>, a year AD, returns the solar cycle of the year.

=cut

sub getSolarCycle {
	my $year = shift;
	
	my $solarcycle = ($year + 20) % 28;
	$solarcycle += 28 if ($solarcycle == 0);
	return $solarcycle;
}

=item getConcurrent( $year )

Given C<$year>, a year AD, returns the concurrent (a number from C<1> to C<7>).
The concurrent numbers are associated with the Slavonic вруцелѣто letters so that C<1> = А, C<2> = В, etc.

=cut

sub getConcurrent {
	my $year = shift;
	
	my $krug_solntsu = ($year + 20) % 28;
	my $vrutseleto = $krug_solntsu + int($krug_solntsu / 4);
	while ($vrutseleto > 7) {
		$vrutseleto -= 7;
	}
	$vrutseleto += 7 if ($vrutseleto == 0);
	return $vrutseleto;
}

=item getLunarCycle( $year )

Given C<$year>, a year AD, returns the lunar (Metonic) cycle number

=cut

sub getLunarCycle {
	my $year = shift;
	
	my $krug_lune = ($year - 2) % 19;
	$krug_lune += 19 if ($krug_lune == 0);
	return $krug_lune;
}

=item getFoundation( $year )

Given C<$year>, a year AD, returns the foundation (the ``age of the moon" on March 1 of that year)

=cut

sub getFoundation {
	my $year = shift;
	
	my $osnovanie = ((($year + 1) % 19) * 11);
	while ($osnovanie > 30) {
		$osnovanie -= 30;
	}

	return $osnovanie == 0 ? 29 : $osnovanie;
}

=item getEpacta( $year )

Given C<$year>, a year AD, returns the Epacta. Note that this is not the Roman Epacta (the age of the moon on January 1). 
Rather, this is the number that needs to be added to make the Foundation C<21> (or C<51>).

=cut

sub getEpacta {
	my $year = shift;
	
	my $foundation = getFoundation($year);
	return (51 - $foundation) % 30;
}

=item getKeyOfBoundaries( $year )

Given C<$year>, a year AD, returns the Key of Boundaries, a letter indicating the structure of the year.

=cut

## FIXME: here we should use Unicode codepoints so that the script does not depend on UTF support on the client side
## and so that it is absolutely clear which code points are being used.

sub getKeyOfBoundaries {
	my $year = shift;
	my @letters = ("А", "Б", "В", "Г", "Д", "Е", "Ж", "Ѕ", "З", "И", "І", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "Ꙋ", "Ф", "Х", "Ѿ", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Ѣ", "Ю", "Ѫ", "Ѧ");
	
	my $pascha = getPascha($year);
	my $kluch;
	if ($pascha->getMonth() == 3) {
		# PASCHA IS IN MARCH
		$kluch = $pascha->getDay() - 21;
	} elsif ($pascha->getMonth() == 4) {
		# PASCHA IS IN APRIL
		$kluch = $pascha->getDay() + 10;
	}
	return $letters[$kluch - 1];
}

=item getGregorianEaster( $year )

Given C<$year>, a year AD, returns the date of the Gregorian Easter (according to the Julian calendar).
If C<$year> is less than 1583, this routine croaks.
This routine uses the formulae as given by Meuss, Astronomical Algorithms, Chapter 8.

=cut

sub getGregorianEaster {
	my $year = shift;
	
	my $a = $year % 19;
	my $b = int($year / 100);
	my $c = $year % 100;
	my $d = int($b / 4);
	my $e = $b % 4;
	my $f = int(($b + 8) / 25);
	my $g = int(($b - $f + 1) / 3);
	my $h = (19 * $a + $b - $d - $g + 15) % 30;
	my $i = int($c / 4);
	my $k = $c % 4;
	my $l = (32 + 2 * $e + 2 * $i - $h - $k) % 7;
	my $m = int(($a + 11 * $h + 22 * $l) / 451);
	my $n = int(($h + $l - 7 * $m + 114) / 31);
	my $p = ($h + $l - 7 * $m + 114) % 31;
	return julianFromGregorian($n, $p + 1, $year);
}

=item getPassover( $year )

Given C<$year>, a year AD, returns the date of the Jewish Passover (15 Nisan) according to the Julian calendar.
The formulae are due to Meeus, Astronomical Algorithms, Chapter 9.

Note that this is the actual, modern Jewish Pesach, not the ecclesiastical Old Testament Passover.
The ecclesiastical old testament Passover can be obtained from the Epacta.

=cut

sub getPassover {
	my $year = shift;
	
	my $a = (12 * $year + 12) % 19;
	my $b = $year % 4;
	my $Q = -1.904412361576 + 1.554241796621 * $a + 0.25 * $b - 0.003177794022 * $year;
	my $j = (int($Q) + 3 * $year + 5 * $b + 2) % 7;
	my $r = $Q - int($Q);
	
	my $d = int($Q) + 22;
	if ($j == 2 || $j == 4 || $j == 6) {
		$d++;
	} elsif ($j == 1 && $a > 6 && $r >= 0.632870370) {
		$d += 2;
	} elsif ($j == 0 && $a > 11 && $r >= 0.897723765) {
		$d++;
	}
	
	return $d > 31 ? new Ponomar::JDate(4, $d - 31, $year) :
			new Ponomar::JDate(3, $d, $year);
}

sub getCivilHolidays {
	my $year = shift;
	
	my $offset = getGregorianOffset $year;
	
	my %holidays = ();
	
	$holidays{'Easter'} = getGregorianEaster $year;
	$holidays{'Civil New Year'} = julianFromGregorian(1, 1, $year);
	$holidays{'Australia Day'}  = julianFromGregorian(1, 26, $year);
	$holidays{'Anzac Day'}      = julianFromGregorian(4, 25, $year);
	$holidays{'Diamond Jubilee'} = julianFromGregorian(6, 5, $year);
	$holidays{'Independence Day (US)'} = julianFromGregorian(7, 4, $year);
	$holidays{'Western Christmas'} = julianFromGregorian(12, 25, $year);
	$holidays{'Boxing Day (Canada)'} = julianFromGregorian(12, 26, $year);
	
	$holidays{'Canada Day'} = julianFromGregorian(7, 1, $year);
	if ($holidays{'Canada Day'}->getDayOfWeek() == 0) {
		$holidays{'Canada Day'}++;
	}
	
	$holidays{'Veterans Day (US)'} = julianFromGregorian(11, 11, $year);
	if ($holidays{'Veterans Day (US)'}->getDayOfWeek() == 0) {
		$holidays{'Veterans Day (US)'}++;
	} elsif ($holidays{'Veterans Day (US)'}->getDayOfWeek() == 6) {
		$holidays{'Veterans Day (US)'}--;
	}
	
	# NOW MORE COMPLEX COMPUTATIONS. 
	my $firstMondayOfYear = $holidays{'Civil New Year'}->addDays((8 - $holidays{'Civil New Year'}->getDayOfWeek()) % 7);
	$holidays{'MLK Day (US)'} = $firstMondayOfYear->addDays(14);
	
	my $firstMondayOfFeb  = julianFromGregorian(2, 1, $year)->addDays((8 - julianFromGregorian(2, 1, $year)->getDayOfWeek()) % 7);
	$holidays{'Presidents Day (US)'} = $firstMondayOfFeb->addDays(14);
	
	$holidays{'Bank Holiday (UK)'} = julianFromGregorian(5, 1, $year)->addDays((8 - julianFromGregorian(5, 1, $year)->getDayOfWeek()) % 7);
	$holidays{'Memorial Day (US)'} = $holidays{'Bank Holiday (UK)'}->addDays(21);
	my $dummy = $holidays{'Memorial Day (US)'}->addDays(7);
	$holidays{'Memorial Day (US)'} = $dummy->getMonthGregorian() == 5 ? $dummy : $holidays{'Memorial Day (US)'};
	
	$holidays{'Victoria Day (Canada)'} = julianFromGregorian(5, 25, $year);
	$holidays{'Victoria Day (Canada)'} = $holidays{'Victoria Day (Canada)'}->getDayOfWeek() == 1 ?
						$holidays{'Victoria Day (Canada)'}->subtractDays(7)   :
						$holidays{'Victoria Day (Canada)'}->subtractDays(($holidays{'Victoria Day (Canada)'}->getDayOfWeek() + 6) % 7);
	
	my $firstMondayOfAug = julianFromGregorian(8, 1, $year)->addDays((8 - julianFromGregorian(8, 1, $year)->getDayOfWeek()) % 7);
	$holidays{'Bank Holiday 2 (UK)'} = $firstMondayOfAug->addDays(21);
	$dummy = $holidays{'Bank Holiday 2 (UK)'}->addDays(7);
	$holidays{'Bank Holiday 2 (UK)'} = $dummy->getMonthGregorian() == 8 ? $dummy : $holidays{'Bank Holiday 2 (UK)'};
	
	$holidays{'Labor Day (US)'} = julianFromGregorian(9, 1, $year)->addDays((8 - julianFromGregorian(9, 1, $year)->getDayOfWeek()) % 7);
	
	my $firstMondayOfOct = julianFromGregorian(10, 1, $year)->addDays((8 - julianFromGregorian(10, 1, $year)->getDayOfWeek()) % 7);
	$holidays{'Columbus Day (US)'} = $firstMondayOfOct->addDays(7);
	$holidays{'Thanksgiving Day (Canada)'} = $firstMondayOfOct->addDays(7);
	
	my $firstThursdayOfNov = julianFromGregorian(11, 1, $year)->addDays((11 - julianFromGregorian(11, 1, $year)->getDayOfWeek()) % 7);
	$holidays{'Thanksgiving Day (US)'} = $firstThursdayOfNov->addDays(21);

	return %holidays;
}

=item getNextFullMoon( $date )

Given C<$date>, a JDate object, returns the Date and Time (Universal Time)
of the next astronomical full moon.
Used in Milankovic calculations.

Formulae due to Meuss, p. 350ff.

=cut

sub getNextFullMoon {
	my $date = shift;
	
	# step 1: get "k"
	my $year = $date->getYear();
	my $rem  = $date->getDaysSince(new Ponomar::JDate(1, 1, $year));
	my $k    = floor( ($year + $rem / 365.25 - 2000) * 12.3685 ) + 0.5;
	# step 2: get "T", the time in Julian centuries since 2000
	my $T    = $k / 1236.85;
	# step 3: get JDE -- the time of the mean phase of the Moon
	my $JDE = 2451550.09765 + 29.530588853 * $k + 0.0001337 * $T ** 2 - 
			0.000000150 * $T ** 3 + 0.00000000073 * $T ** 4;
	# Calculate Eccentricity of Earth's orbit around the Sun
	my $E = 1 - 0.002516 * $T - 0.0000074 * $T ** 2;
	# Calculate Mean anomaly of Sun
	my $M1 = deg2rad( 2.5534 + 29.10535669 * $k - 0.0000218 * $T ** 2 - 0.00000011 * $T ** 3 );
	# Calculate Mean anomaly of Moon
	my $M2 = deg2rad( 201.5643 + 385.81693528 * $k + 0.0107438 * $T ** 2 + 0.00001239 * $T ** 3 - 0.000000058 * $T ** 4);
	# Calculate Moon's argument of latitude
	my $F  = deg2rad( 160.7108 + 390.67050274 * $k - 0.0016341 * $T ** 2 - 0.00000227 * $T ** 3 + 0.000000011 * $T ** 4 );
	# Calculate longitude of ascending node of the lunar orbit
	my $O  = deg2rad( 124.7746 - 1.56375580 * $k + 0.0020691 * $T ** 2 + 0.00000215 * $T ** 3 );
	
	## ADD TO JDE THE CORRECTION TERMS BELOW
	$JDE += -0.40614 * sin ($M2) 
	       + 0.17302 * $E * sin ($M1)
	       + 0.01614 * sin (2 * $M2)
	       + 0.01043 * sin ( 2 * $F)
	       + 0.00734 * $E * sin ( $M2 - $M1)
	       - 0.00515 * $E * sin ( $M2 + $M1)
	       + 0.00209 * ($E ** 2) * sin (2 * $M1)
	       - 0.00111 * sin ( $M2 - 2 * $F)
	       - 0.00057 * sin ( $M2 + 2 * $F)
	       + 0.00056 * $E * sin (2 * $M2 + $M1)
	       - 0.00042 * sin (3 * $M2)
	       + 0.00042 * $E * sin ($M1 + 2 * $F)
	       + 0.00038 * $E * sin ($M1 - 2 * $F)
	       - 0.00024 * $E * sin (2 * $M2 - $M1)
	       - 0.00017 * sin ($O)
	       - 0.00007 * sin ($M2 + 2 * $M1)
	       + 0.00004 * sin (2 * $M2 - 2 * $F)
	       + 0.00004 * sin (3 * $M1)
	       + 0.00003 * sin ($M2 + $M1 - 2 * $F)
	       + 0.00003 * sin (2 * $M2 + 2 * $F)
	       - 0.00003 * sin ($M2 + $M1 + 2 * $F)
	       + 0.00003 * sin ($M2 - $M1 + 2 * $F)
	       - 0.00002 * sin ($M2 - $M1 - 2 * $F)
	       - 0.00002 * sin (3 * $M2 + $M1)
	       + 0.00002 * sin (4 * $M2);
	
	## compute the planetary terms
	my $A1 = deg2rad (299.77 + 0.107408 * $k - 0.009173 * $T ** 2);
	my $A2 = deg2rad (251.88 + 0.016321 * $k);
	my $A3 = deg2rad (251.83 + 26.651886 * $k);
	my $A4 = deg2rad (349.42 + 36.412478 * $k);
	my $A5 = deg2rad (84.66  + 18.206239 * $k);
	my $A6 = deg2rad (141.74 + 53.303771 * $k);
	my $A7 = deg2rad (207.14 + 2.453732 * $k);
	my $A8 = deg2rad (154.84 + 7.306860 * $k);
	my $A9 = deg2rad (34.52 + 27.261239 * $k);
	my $A10 = deg2rad (207.19 + 0.121824 * $k);
	my $A11 = deg2rad (291.34 + 1.844379 * $k);
	my $A12 = deg2rad (161.72 + 24.198154 * $k);
	my $A13 = deg2rad (239.56 + 25.513099 * $k);
	my $A14 = deg2rad (331.55 + 3.592518 * $k);
	
	## Add to JDE the planetary correction terms
	$JDE += 0.000325 * sin ($A1)
	       + 0.000165 * sin ($A2)
	       + 0.000164 * sin ($A3)
	       + 0.000126 * sin ($A4)
	       + 0.000110 * sin ($A5)
	       + 0.000062 * sin ($A6)
	       + 0.000060 * sin ($A7)
	       + 0.000056 * sin ($A8)
	       + 0.000047 * sin ($A9)
	       + 0.000042 * sin ($A10)
	       + 0.000040 * sin ($A11)
	       + 0.000037 * sin ($A12)
	       + 0.000035 * sin ($A13)
	       + 0.000023 * sin ($A14);

	### JDE now contains the Julian day of the moon
	# XXX: Why do we have to add one? There is some kind of problem with our code
	return new Ponomar::JDate($JDE);
}

=item getVernalEquinox ( $year ) 

Given C<$year>, a year between AD 1000 and AD 3000, returns the date of the March equinox

Formulae due to Meuss, Astronomical Algorithms, pp. 177-182.

=cut

sub getVernalEquinox {
	my $year = shift;
	carp (__PACKAGE__ . "::getVernalEquinox($year) : year outside allowed bounds") unless ($year > 999 && $year < 3001);
	carp (__PACKAGE__ . "::getVernalEquinox($year) : year must be an integer") unless ($year == int($year));

	my $y = ($year - 2000) / 1000;
	my $JDE0 = 2451623.80984 + 365242.37404 * $y + 0.05169 * $y ** 2 - 0.00411 * $y ** 3 - 0.00057 * $y ** 4;
#	my $JDE0 = 2451716.56767 + 365241.62603 * $y + 0.00325 * $y ** 2 + 0.00888 * $y ** 3 - 0.00030 * $y ** 4;
	my $T = ($JDE0 - 2451545.0) / 36525;
	my $W = deg2rad(35999.373 * $T - 2.47);
	my $lambda = 1 + 0.0334 * cos($W) + 0.0007 * cos(2 * $W);
	my $S = 	485 * cos (deg2rad( 324.96 + 1934.136 * $T)) +
			203 * cos (deg2rad( 337.23 + 32964.467 * $T)) +
			199 * cos (deg2rad( 342.08 + 20.186 * $T)) +
			182 * cos (deg2rad( 27.85 + 445267.112 * $T)) +
			156 * cos (deg2rad( 73.14 + 45036.886 * $T)) +
			136 * cos (deg2rad(171.52 + 22518.443 * $T)) +
			 77 * cos (deg2rad(222.54 + 65928.934 * $T)) +
			 74 * cos (deg2rad(296.72 + 3034.906 * $T)) +
			 70 * cos (deg2rad(243.58 + 9037.513 * $T)) +
			 58 * cos (deg2rad(119.81 + 33718.147 * $T)) +
			 52 * cos (deg2rad(297.17 + 150.678 * $T)) +
			 50 * cos (deg2rad(21.02 + 2281.226 * $T)) +
			 45 * cos (deg2rad(247.54 + 29929.562 * $T)) +
			 44 * cos (deg2rad(325.15 + 31555.956 * $T)) +
			 29 * cos (deg2rad(60.93 + 4443.417 * $T)) +
			 18 * cos (deg2rad(155.12 + 67555.328 * $T)) +
			 17 * cos (deg2rad(288.79 + 4562.452 * $T)) +
			 16 * cos (deg2rad(198.04 + 62894.029 * $T)) +
			 14 * cos (deg2rad(199.76 + 31436.921 * $T)) +
			 12 * cos (deg2rad(95.39 + 14577.848 * $T)) +
			 12 * cos (deg2rad(287.11 + 31931.756 * $T)) +
			 12 * cos (deg2rad(320.81 + 34777.259 * $T)) +
			  9 * cos (deg2rad(227.73 + 1222.114 * $T)) +
			  8 * cos (deg2rad(15.45 + 16859.074 * $T));

	# time of vernal equinox expressed as a Julian Ephemeris Day
	# (hence in Dynamical Time).
	return new Ponomar::JDate( $JDE0 + (0.00001 * $S) / $lambda );
}

=item isMilankovichLeap ($year) 

Given C<$year>, a year, returns 0 unless it is a leap year on the Milankovich calendar.

=cut

sub isMilankovichLeap {
	my $year = shift;
	carp (__PACKAGE__ . "::isMilankovichLeap($year) : invalid year specified") unless ($year == int($year));
	if ($year % 4 == 0) {
		if ($year % 100 == 0) {
			return ( (($year / 100) % 9 == 2) || (($year / 100) % 9) == 6);
		} else {
			return 1;
		}
	}
	return 0;
}

=item getJulianDayFromMilankovich ($month, $day, $year) 

Given C<$month>, C<$day>, C<$year>, a date according to the (proleptic) Milankovich calendar, returns
the Julian Day, that is the number of days since January 1, 4713 BC on the proleptic Julian calendar.

=cut

sub getJulianDayFromMilankovich {
	my ($month, $day, $year) = @_;

	# validation
	carp (__PACKAGE__ . "::getJulianDayFromMilankovich($month, $day, $year) : invalid date specified") unless ($year == int($year) && $month == int($month) && $day == int($day));
	carp (__PACKAGE__ . "::getJulianDayFromMilankovich($month, $day, $year) : invalid date specified") unless ($month > 0 && $month < 13);
	my @NUM_DAYS = isMilankovichLeap($year) ? (0, 31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31) : (0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
	carp (__PACKAGE__ . "::getJulianDayFromMilankovich($month, $day, $year) : invalid date specified") unless ($day > 0 && $day <= $NUM_DAYS[$month]);

	my $f = 1721425.5 + 365 * ($year - 1) + floor(($year - 1) / 4) + floor((367 * $month - 362) / 12) + $day;
	if ($month > 2) {
		if (isMilankovichLeap($year)) {
			$f--;
		} else {
			$f -= 2;
		}
	}

	# correction for prior century leap years
	$f += -1 * floor(($year - 1) / 100) + floor( (2 * floor(($year - 1) / 100) + 6) / 9);
	return $f;
}

=item getDeltaT ( $date )

Give C<$date>, a JDate object with year between 1999 BC and 3000 AD on the (proleptic) Julian calendar,
returns Delta T, that is, the offset (in seconds) between Dynamic Time and Universal Time.
Note that UT = TD - DeltaT.
These formulae are by Fred Espenak and Jean Meeus, see 
L<http://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html>
for more information.

=cut

sub getDeltaT {
	my $date = shift;
	my $year = $date->getYearGregorian();
	my $month = $date->getMonthGregorian();

	# validation
	carp (__PACKAGE__ . "::getDeltaT($year) : year outside of range") unless ($year > -2000 && $year < 3001);

	
	my $y = $year + ($month - 0.5) / 12;
	if ($year < -500) {
		return -20 + 32 * (($y - 1820) / 100) ** 2;
	} elsif ($year < 501) {
		return 10583.6 - 1014.41 * ($y / 100) + 33.78311 * ($y / 100) ** 2 - 5.952053 * ($y / 100) ** 3 - 0.1798452 * ($y / 100) ** 4 + 0.022174192 * ($y / 100) ** 5 + 0.0090316521 * ($y / 100) ** 6;
	} elsif ($year < 1601) {
		return 1574.2 - 556.01 * (($y-1000)/100) + 71.23472 * (($y-1000)/100) ** 2 + 0.319781 * (($y-1000)/100) ** 3 - 0.8503463 * (($y-1000)/100) ** 4 - 0.005050998 * (($y-1000)/100) ** 5 + 0.0083572073 * (($y-1000)/100) ** 6;
	} elsif ($year < 1701) {
		return 120 - 0.9808 * ($y - 1600) - 0.01532 * ($y - 1600) ** 2 + ($y - 1600) ** 3 / 7129;
	} elsif ($year < 1801) {
		return 8.83 + 0.1603 * ($y - 1700) - 0.0059285 * ($y - 1700) ** 2 + 0.00013336 * ($y - 1700) ** 3 - ($y - 1700) ** 4 / 1174000;
	} elsif ($year < 1861) {
		return 13.72 - 0.332447 * ($y - 1800) + 0.0068612 * ($y - 1800) ** 2 + 0.0041116 * ($y - 1800) ** 3 - 0.00037436 * ($y - 1800) ** 4 + 0.0000121272 * ($y - 1800) ** 5 - 0.0000001699 * ($y - 1800) ** 6 + 0.000000000875 * ($y - 1800) ** 7;
	} elsif ($year < 1901) {
		return 7.62 + 0.5737 * ($y - 1860) - 0.251754 * ($y - 1860) ** 2 + 0.01680668 * ($y - 1860) ** 3 - 0.0004473624 * ($y - 1860) ** 4 + ($y - 1860) ** 5 / 233174;
	} elsif ($year < 1921) {
		return -2.79 + 1.494119 * ($y - 1900) - 0.0598939 * ($y - 1900) ** 2 + 0.0061966 * ($y - 1900) ** 3 - 0.000197 * ($y - 1900) ** 4;
	} elsif ($year < 1942) {
		return 21.20 + 0.84493*($y - 1920) - 0.076100 * ($y - 1920) ** 2 + 0.0020936 * ($y - 1920) ** 3;
	} elsif ($year < 1962) {
		return 29.07 + 0.407*($y - 1950) - ($y - 1950) ** 2/233 + ($y - 1950) ** 3 / 2547;
	} elsif ($year < 1987) {
		return 45.45 + 1.067*($y - 1975) - ($y - 1975) ** 2/260 - ($y - 1975) ** 3 / 718;
	} elsif ($year < 2006) {
		return 63.86 + 0.3345 * ($y - 2000) - 0.060374 * ($y - 2000) ** 2 + 0.0017275 * ($y - 2000) ** 3 + 0.000651814 * ($y - 2000) ** 4 + 0.00002373599 * ($y - 2000) ** 5;
	} elsif ($year < 2051) {
		return 62.92 + 0.32217 * ($y - 2000) + 0.005589 * ($y - 2000) ** 2;
	} elsif ($year < 2151) {
		return -20 + 32 * (($y-1820)/100)**2 - 0.5628 * (2150 - $y);
	} else {
		return -20 + 32 * ($y-1820)/100  ** 2;
	}
}


=item getMilankovichPascha ( $year )

Wrapper sub for Milankovich calculations. Given a C<$year> AD, returns the Pascha
according to the Milankovich specification.

The Milankovich Pascha is defined as 
the Sunday after the midnight-to-midnight day at the meridian of the Church of the Holy Sepulchre in Jerusalem during which the first full moon after the vernal equinox occurs.

=head3 Method for computing Milankovich Pascha

=over 8

=item Compute the date and time of the Vernal Equinox for C<$year> (returns C<$EQ>, a Julian Day)

=item Compute the date and time of the full moon that occurs after C<$EQ> (returns C<$MOON>, a Julian Day in Dynamic Time)

=item Get the Date part of C<$MOON> (returns C<$DAY>, C<$MONTH>, a date on the Julian calendar)

=item Convert the time part of C<$MOON> to Universal Time using the polynomials here:
	L<http://eclipse.gsfc.nasa.gov/SEcat5/deltatpoly.html>.

=item Convert the Universal Time to local time at the Church of the Holy Sepulchre. For simplicity, assume the Holy Sepulchre is always two hours ahead of UT. (This is C<$TIME> in Hours, including a fractional component).

=item If C<$TIME> > 24, add C<1> to C<$DAY> (note this may also push C<$MONTH> up).

=item Compute the next Sunday after C<$DAY> (C<$PDAY>). This is the date of Pascha.

=back

The method returns a JDate object containing the Julian Day of the Milankovich Pascha.

B<NOTE>: the resulting JDate object is a Julian Day. Since you're working with the 
Milankovich calendar, you will probably want the date according to the Milankovich calendar.
Use the C<getMilankovich...> functions of the JDate class, e.g.:

	$pascha = getMilankovichPascha(2100);
	print $pascha->getMilankovichDay();

=back

=cut

sub getMilankovichPascha {
	my $year = shift;

	# Milankovich calculations are really only valid up to the year 3000 AD 
	carp (__PACKAGE__ . "::getMilankovichPascha($year) : year outside of range") unless ($year > 0 && $year < 3001);
	my $ve = getVernalEquinox($year);

	my $moon = getNextFullMoon($ve);
	my $start = $ve; # iterator :(
	while ($moon->getJulianDay() < $ve->getJulianDay()) {
		$start = $start->addDays(1);
		$moon = getNextFullMoon($start);
	}
	my $apparentMoon = new Ponomar::JDate($moon->getJulianDay() - getDeltaT($moon) / (60 * 60 * 24) + 2.0 / 24); # this is the time when the full moon occured at Jerusalem
	return $apparentMoon->addDays(7 - $apparentMoon->getDayOfWeek());
}

1;

__END__

