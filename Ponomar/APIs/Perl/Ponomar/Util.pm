package Ponomar::Util;

=head1 Ponomar::Util

Ponomar::Util - Exports utility functions for Ponomar API

=head3 DESCRIPTION

This is not an Object Oriented class, but rather is a set of utility functions for the Ponomar API. All useful methods are exported from this class via the Exporter interface.

=cut

use strict;
require 5.004;
use Carp;
require Exporter;
require Ponomar::JDate;
use vars qw (@ISA @EXPORT_OK %EXPORT_TAGS @EXPORT $VERSION $basepath);

BEGIN {
	$VERSION = 0.01;
	@ISA 	 = qw( Exporter );
	@EXPORT  = qw( getPascha getGregorianOffset findBottomUp findTopDown getToday max argmax getMatinsGospel);
	@EXPORT_OK = ();
#	$basepath = "/home/sasha/svn/ponomar/Ponomar/languages/";
	$basepath = "/home/ponomar0/svn/Ponomar/languages/";
}

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

	return $basepath . $file if (-e $basepath . $file);
	croak (__PACKAGE__ . "::findBottomUp($language, $file) : unable to find file");
}

=item findTopDown ($language, $file)

THIS ALGORITHM IMPLEMENTS THE TOP-DOWN APPROACH FOR READING FILES DESCRIBED BY YURI IN op. cit., p. 28

WE CREATE AN ARRAY OF ALL EXTANT FILES NAMED C<$file> IN ALL PATHS BEGINNING WITH BASEPATH AND UP TO C<< $basepath/<language>/<script>/<locale>/file >>

PARAMETERS: SAME AS ABOVE. Returns: an array of all files in the top-down path. Carps and returns an empty array if not files were found.

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

=item getToday()

Returns a Ponomar::JDate object with the date of Today according to the System clock.

B<WARNING>: Ponomar::Util relies on time to set Today. It assumes that the system's epoch begins
on 00:00:00 UTC, January 1, 1970 (GREGORIAN!). It has recently come to my attention that this is not true for all systems. I know of no way to get around this problem, so this should be considered a bug.

=cut

sub getToday {
	## WE SHALL ASSUME THAT THE EPOCH BEGINS ON JANUARY 1, 1970
	## THIS IS JULIAN DAY 2440588
	return new Ponomar::JDate(int(time / 86400) + 2440588);
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
	if ($indikt == 0) {
		$indikt += 15;
	}
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

Given C<$year>, a year AD, returns the concurrent (a number from 1 to 7)
The concurrent numbers are associated with the Slavonic вруцелѣто letters so that 1 = А, 2 = В, etc.

=cut

sub getConcurrent {
	my $year = shift;
	
	my $krug_solntsu = ($year + 20) % 28;
	my $vrutseleto = $krug_solntsu + int($krug_solntsu / 4);
	while ($vrutseleto > 7) {
		$vrutseleto -= 7;
	}
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

Given C<$year>, a year AD, returns the foundation (the "age of the moon" on March 1 of that year)

=cut

sub getFoundation {
	my $year = shift;
	
	my $osnovanie = (($year + 1) % 19) * 11;
	return $osnovanie % 30;
}

=item getEpacta( $year )

Given C<$year>, a year AD, returns the Epacta. Note that this is not the Roman Epacta (the age of the moon on January 1). Rather, this is the number that needs to be added to make the Foundation 21 (51).

=cut

sub getEpacta {
	my $year = shift;
	
	my $foundation = getFoundation($year);
	return (51 - $foundation) % 30;
}

=item getKeyOfBoundaries( $year )

Given C<$year>, a year AD, returns the Key of Boundaries, a letter indicating the structure of the year

=cut

sub getKeyOfBoundaries {
	my $year = shift;
	use utf8;
	my @letters = ("А", "Б", "В", "Г", "Д", "Е", "Ж", "Ѕ", "З", "И", "І", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "У", "Ф", "Х", "Ѿ", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Ѣ", "Ю", "Ѫ", "Ѧ");
	
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

Given C<$year>, a year AD, returns the date of the Gregorian Easter (according to the Julian calendar)
If C<$year> is less than 1583, this routine croaks.
This routine uses the formulae as given by Meuss, Astronomical Algorithms, Chapter 8

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

Given C<$year>, a year AD, returns the date of the Jewish Passover (15 Nisan) according to the Julian calendar
The formulae are due to Meeus, Astronomical Algorithms, Chapter 9.

Note that this is the actual, modern Jewish Pesach, not the ecclesiastical Old Testament Passover.
The ecclesiastical old testament Passover can be obtained from the Epakta.

=cut

sub getPassover {
	my $year = shift;
	
	my $a = (12 * $year + 12) % 19;
	my $b = $year % 4;
	my $Q = -1.904412361576 + 1.554241796621 * $a + 0.25 * $b - 0.003177794022 * $year;
	my $j = (int($Q) + 3 * $year + 5 * $b + 2) % 7;
	my $r = $Q - int($Q);
	
	my $d = int($Q) + 22;
	if ($j = 2 || $j == 4 || $j == 6) {
		$d++;
	} elsif ($j == 1 && $a > 6 && $r >= 0.632870370) {
		$d += 2;
	} elsif ($j == 0 && $a > 11 && $r >= 0.897723765) {
		$d++;
	}
	
	return $d > 31 ? new Ponomar::JDate(4, $d - 31, $year) :
			new Ponomar::JDate(3, $d, $year);
}

	
__END__

