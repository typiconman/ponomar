package Ponomar::JDate;

use strict;

require 5.004;
require Exporter;
require Carp;
use POSIX qw(floor);

our $VERSION = '0.01';
our @ISA = qw( Exporter );
our @EXPORT = ();
our @EXPORT_OK = ();

require Ponomar::Sunrise;
use overload
	'==' 	=> "equals",
	'<'	=> "before",
	'>'	=> "after",
	'++'	=> "addOneDay",
	'--'	=> "subtractOneDay",
	'='	=> sub { $_[0]->new($_[0]->{_mnjday}) }; # ugh, perl, Really?
	
my @DAYS_IN_MONTH = (31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
my @DAYS_IN_LEAP  = (31, 29, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31);
my @MONTH_NAMES   = qw(january february march april may june july august september october november december);
my @WEEKDAY_NAMES = qw(sunday monday tuesday wednesday thursday friday saturday);

##################### INTERNAL SUBS #####################################
# overloaded modulo operator
sub mod {
	my ($divisor, $modulo) = @_;

	return $divisor % $modulo == 0 ? $modulo : $divisor % $modulo;
}

##################################################################

=head1 Ponomar::JDate

Ponomar::JDate - A module for working with dates on the (proleptic) Julian Calendar

=head3 SYNOPSIS

	use Ponomar::JDate;
	$date = new Ponomar::JDate(2, 1, 2001); # RETURNS February 1, 2001
	$date2 = $date->addDays(1);	# returns February 2, 2001
	$date2->equals(new Ponomar::JDate(2, 2, 2001)); # RETURNS true

By convention January is treated as Month 1. Sunday is treated as day of week 0. January 1 is treated as day of year 0.

=head3 METHODS

=over 4

=item C<new($julian_day)> OR C<new($month, $day, $year)>

Creates a new instance of Ponomar::JDate either set to Month, Day, Year 
where Month, Day, Year is a calendar date on the Julian Calendar or to the Julian Day.

The months begin with 1 for January and run to 12 for December.
Though years BC are generally not used, if necessary, the code is defined
so that the C<$year> before AD 1 is -1 (B<NOT> 0).
The C<$day> may have a fractional component.

Note that Julian Days begin at Noon UTC, so we usually have a 0.5 around,
which is a bit annoying, but is done this way to keep all formulae in this code
the same as in Meuss, Astronomical Algorithms (1st edition).

=cut

sub new ($;$$) {
	my $class = shift;
	my $mn_jday;
	
	if (scalar(@_) == 1) {
		$mn_jday = shift;
	} else {
		my ($month, $day, $year) = @_;

		if ($month == 1 || $month == 2) {
			$month += 12;
			$year--;
		}

		$mn_jday = floor(365.25 * ($year + 4716)) + floor(30.6001 * ($month + 1)) + $day - 1524.5;
	}
	
	my $self = {
		_mnjday => $mn_jday
	};
	
	bless $self;
	return $self;
}

=item getYear()

Returns the Year of the JDate object

Example:

	$date = new Ponomar::JDate(2, 1, 2001);
	$date->getYear(); # returns 2001

=cut

sub getYear ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	$mn_jday += 0.5;
	my $a = floor($mn_jday);
	my $B = $a + 1524;
	my $C = floor(($B - 122.1) / 365.25);
	my $D = floor(365.25 * $C);
	my $E = floor(($B - $D) / 30.6001);
	my $m = $E < 14 ? $E - 1 : $E - 13;
	return $m > 2 ? $C - 4716 : $C - 4715;
#	return int($n1 + $n2) - 4800 + $adj;
}

=item getYearAM()

Returns the Year from the (Byzantine) Creation of the World (anno mundi).
The Creation of the World took place on March 1, 5508 BC.

=cut

sub getYearAM ($) {
	my $self = shift;
	
	my $y = $self->getYear();
	my $cutoff = Ponomar::JDate->new(9, 1, $y);
	return $self->before($cutoff) ? $y + 5508 : $y + 5509;
}

=item getMonth()

Returns the Month of the JDate object.

Example:

	$date = new Ponomar::JDate(2, 1, 2001);
	$date->getMonth(); # returns 2

B<NB>: January is month 1

=cut

sub getMonth ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	$mn_jday += 0.5;
	my $a = floor($mn_jday);
	my $B = $a + 1524;
	my $C = floor(($B - 122.1) / 365.25);
	my $D = floor(365.25 * $C);
	my $E = floor(($B - $D) / 30.6001);
	return $E < 14 ? $E - 1 : $E - 13;
}

=item getDay()

Returns the day of the month of the JDate object.

Example:

	$date = new Ponomar::JDate(2, 1, 2001);
	$date->getDay(); # returns 1

=cut

sub getDay ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	$mn_jday += 0.5;
	my $a = floor($mn_jday);
	my $B = $a + 1524;
	my $C = floor(($B - 122.1) / 365.25);
	my $D = floor(365.25 * $C);
	my $E = floor(($B - $D) / 30.6001);
	my $F = $mn_jday - $a;
	return $B - $D - floor(30.6001 * $E) + $F;
}

=item getHour()

Returns the hour component of the JDate object (which may be fractional).

This is useful for such things as vernal equinox, sunrize and moon calculations.

The result is always in hours since midnight UTC, and by convention
the JDay starts at noon UTC.

=cut

sub getHour($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday} - 0.5;
	my $r = $mn_jday - int ($mn_jday);
	return int($r * 24);
}


=item getMinute()

Returns the minute component of the JDate object (which may be fractional).

This is useful for such things as vernal equinox, sunrize and moon calculations.

The result is always in minutes since the last hour UTC.

=cut

sub getMinute($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday} - 0.5;
	my $r = $mn_jday - int ($mn_jday);
	return int( ($r * 24 - int($r * 24)) * 60);
}

=item getSecond()

Returns the second component of the JDate object (which may be fractional).

This is useful for such things as vernal equinox, sunrize and moon calculations.

The result is always in seconds since the last minute UTC.

=cut

sub getSecond($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday} - 0.5;
	my $r = $mn_jday - int ($mn_jday);
	return int( ( ($r * 24 - int($r * 24)) * 60 - int( ($r * 24 - int($r * 24)) * 60) ) * 60);
}

=item getDayOfWeek()

Returns the day of the week of the JDate object.

Example:

	$date = new Ponomar::JDate(2, 1, 2001);
	$date->getDayOfWeek(); # returns 2

B<NB>: Sunday is day of week 0.

=cut

sub getDayOfWeek ($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday};
	return floor($mn_jday + 1.5) % 7;
#	return $temp;
}

=item getDayOfWeekString 

Returns the day of the week as a string.

=cut

sub getDayOfWeekString ($) {
	my $self = shift;
	
	return $WEEKDAY_NAMES[$self->getDayOfWeek()];
}

=item getDoy()

Returns the day of the year (doy) of a JDate object.

Note that January 1 is doy 0. February 29, if it exists, is doy 366.

=cut

sub getDoy ($) {
	my $self = shift;
	
	my $jbar = $self->{_mnjday} + 32083.5;
	my $da   = mod($jbar, 1461);
	return $da == 1461 ? 366 : mod($da + 59, 365) - 1; ## Jan 1 is doy 0
}

=item getYearGregorian()

Returns the year of the JDate object according to the (proleptic) Gregorian calendar.

=cut

sub getYearGregorian ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	$mn_jday += 0.5;
	my $a;
	if (floor($mn_jday) < 2299161) {
		$a = floor($mn_jday);
	} else {
		my $alpha = floor((floor($mn_jday) - 1867216.25) / 36524.25);
		$a = floor($mn_jday) + 1 + $alpha - floor($alpha / 4);
	}
	my $C = floor(($a + 1524 - 122.1) / 365.25);
	my $D = floor(365.25 * $C);
	my $E = floor(($a + 1524 - $D) / 30.6001);
	my $m = $E < 14 ? $E - 1 : $E - 13;
	return $m > 2 ? $C - 4716 : $C - 4715;
}

=item getMonthGregorian()

Returns the month of the JDate object according to the (proleptic) Gregorian calendar.

=cut

sub getMonthGregorian ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	$mn_jday += 0.5;
	my $a;
	if (floor($mn_jday) < 2299161) {
		$a = floor($mn_jday);
	} else {
		my $alpha = floor((floor($mn_jday) - 1867216.25) / 36524.25);
		$a = floor($mn_jday) + 1 + $alpha - floor($alpha / 4);
	}
	my $C = floor(($a + 1524 - 122.1) / 365.25);
	my $D = floor(365.25 * $C);
	my $E = floor(($a + 1524 - $D) / 30.6001);
	return $E < 14 ? $E - 1 : $E - 13;
}

=item getDayGregorian()

Returns the day of the Month of the JDate object according to the (proleptic) Gregorian calendar.

=cut

sub getDayGregorian ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	$mn_jday += 0.5;
	my $a;
	if (floor($mn_jday) < 2299161) {
		$a = floor($mn_jday);
	} else {
		my $alpha = floor((floor($mn_jday) - 1867216.25) / 36524.25);
		$a = floor($mn_jday) + 1 + $alpha - floor($alpha / 4);
	}
	my $C = floor(($a + 1524 - 122.1) / 365.25);
	my $D = floor(365.25 * $C);
	my $E = floor(($a + 1524 - $D) / 30.6001);
	my $F = $mn_jday - floor($mn_jday);
	return $a + 1524 - $D - floor(30.6001 * $E) + $F;
}

=item getDaysSince($date)

Returns the number of days since C<$date>, another JDate object.

=cut

sub getDaysSince ($$) {
	my $self = shift;
	my $other = shift;
	return $self->{_mnjday} - $other->{_mnjday};
}

=item getDaysUntil($date)

Returns the number of days until C<$date>, another JDate object.

=cut

sub getDaysUntil ($$) {
	my $self = shift;
	my $other = shift;
	
	return $other->{_mnjday} - $self->{_mnjday};
}

=item getWeeksSince($date)

Returns the number of weeks since C<$date>, another JDate object.

=cut

sub getWeeksSince ($$) {
	my $self = shift;
	my $other = shift;
	
	return ($self->{_mnjday} - $other->{_mnjday}) / 7;
}

=item getWeeksUntil($date)

Returns the number of weeks until C<$date>, another JDate object.

=cut

sub getWeeksUntil ($$) {
	my $self = shift;
	my $other = shift;
	
	return ($other->{_mnjday} - $self->{_mnjday}) / 7;
}

=item addDays($integer)

Returns a new JDate object, advanced by C<$integer> days.

=cut

sub addDays ($$) {
	my $self = shift;
	my $n = shift;

	return Ponomar::JDate->new($self->{_mnjday} + $n);
}

=item addOneDay()

Returns a new JDate object, advanced by one day.

=cut

sub addOneDay($) {
	my $self = shift;
	
	$self->{_mnjday}++;
}

=item addMonths($integer)

Returns a new JDate object, advanced by C<$integer> months.

=cut

sub addMonths($$) {
	my $self = shift;
	my $n    = shift;
	
	my $m    = $self->getMonth();
	return $m == 12 ? Ponomar::JDate->new(1, $self->getDay(), $self->getYear() + 1) :
		Ponomar::JDate->new($m + 1, $self->getDay(), $self->getYear);
}

=item subtractDays($integer)

Returns a new JDate object, diminished by C<$integer> days.

=cut

sub subtractDays ($$) {
	my $self = shift;
	my $n = shift;
	
	return Ponomar::JDate->new($self->{_mnjday} - $n);
}

=item subtractOneDay()

Returns a new JDate object, diminished by one day.

=cut

sub subtractOneDay($) {
	my $self = shift;
	
	$self->{_mnjday}--;
}

=item subtractMonths($integer)

Returns a new JDate object, diminished by C<$integer> months.

=cut

sub subtractMonths($$) {
	my $self = shift;
	my $n    = shift;
	
	my $m    = $self->getMonth();
	return $m == 1 ? Ponomar::JDate->new(12, $self->getDay(), $self->getYear() - 1) :
		Ponomar::JDate->new($m - 1, $self->getDay(), $self->getYear());
}

=item equals($date)

Returns true of this JDate object and the object C<$date> are the same Julian day. Returns false otherwise.

=cut

sub equals ($$) {
	my $self = shift;
	my $other = shift;
	
	return $self->{_mnjday} == $other->{_mnjday};
}

=item before($date)

Returns true if self is before C<$date>.

=cut

sub before($$) {
	my $self = shift;
	my $other = shift;
	
	return $self->{_mnjday} < $other->{_mnjday};
}

=item after($date)

Returns true if self is after C<$date>. 

=cut

sub after($$) {
	my $self = shift;
	my $other = shift;
	
	return $self->{_mnjday} > $other->{_mnjday};
}

=item getJulianDay()

Returns the Julian day of the object.

=cut

sub getJulianDay ($) {
	my $self = shift;
	
	return $self->{_mnjday};
}

=item getNearestSunday()

Returns a new JDate object with the nearest Sunday to a JDate object.
B<NB>: if the JDate object is a Sunday, returns itself.

=cut

sub getNearestSunday ($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday};
	my $dow = floor($mn_jday + 1.5) % 7;	
	return $dow <= 3 ? JDate->new($mn_jday - $dow) : JDate->new($mn_jday + 7 - $dow);
}

=item getPreviousSunday()

Returns a new JDate object with the previous Sunday to a JDate object.
B<NB>: if the JDate object is a Sunday, returns one week before.

=cut

sub getPreviousSunday ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	my $dow =  floor($mn_jday + 1.5) % 7;
	return Ponomar::JDate->new($mn_jday - $dow);
}

=item getNextSunday()

Returns a new JDate object with the next Sunday to a JDate object.
B<NB>: if the JDate object is a Sunday, returns one week later.

=cut

sub getNextSunday ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	my $dow = floor($mn_jday + 1.5) % 7;
	return Ponomar::JDate->new($mn_jday + 7 - $dow);
}

=item times($n)

Returns the a new JDate object with the Julian Day multiplied by C<$n>.
Probably not very useful.

=cut

sub times ($$) {
	my $self = shift;
	my $n = shift;
	
	return JDate->new($self->{_mnjday} * $n);
}

=item divide($n)

Returns the closest JDate object to the current JDate object divided by  C<$n>.
This may be marginally useful in finding out things like midpoints of time periods.

=cut

sub divide ($$) {
	my $self = shift;
	my $n = shift;
	
	Carp::croak ("Division by zero encountered") if ($n == 0);
	return JDate->new(int($self->{_mnjday} / $n));
}

=item module($n)

Returns the JDate object C<mod> C<$n>.
Probably completely useless.

=cut

sub modulo ($$) {
	my $self = shift;
	my $n = shift;
	
	return $self->{_mnjday} % $n;
}


=item getSunrise($longitude, $latitude, $TimeZone, [$DST, $ALT])

Return the sunrise/sunset for a given day.

 Eastern longitude is entered as a positive number
 Western longitude is entered as a negative number
 Northern latitude is entered as a positive number
 Southern latitude is entered as a negative number

Example:

C<< ($sunrise, $sunset) = $date->getSunrise($longitude, $latitude, $TimeZone, $DST, $ALT); >>

Returns the sunrise and sunset times, in HH:MM format.
Note: C<$Time Zone> is the offset from UTC and $<DST> is daylight
saving time (C<1> means DST is in effect and C<0> means it is not).  If C<$ALT> is not specified,
a default altitude of C<-.0833> is used. Note that adding C<1> to C<$TimeZone> during DST
and specifying C<$DST> as C<0> is the same as indicating the
Time Zone correctly and specifying C<$DST> as C<1>.

There are a number of values of C<$ALT> to choose from.  The default is
C<-0.833> because this is what most countries use. Here is the list of other common values:

=over 4

=item C<0> degrees

Center of Sun's disk touches a mathematical horizon

=item C<-0.25> degrees

Sun's upper limb touches a mathematical horizon

=item C<-0.583> degrees

Center of Sun's disk touches the horizon; atmospheric refraction accounted for

=item C<-0.833> degrees, DEFAULT

Sun's supper limb touches the horizon; atmospheric refraction accounted for

=item C<-6> degrees, CIVIL

Civil twilight (one can no longer read outside without artificial illumination)

=item C<-12> degrees, NAUTICAL

Nautical twilight (navigation using a sea horizon no longer possible)

=item C<-15> degrees, AMATEUR

Amateur astronomical twilight (the sky is dark enough for most astronomical observations)

=item C<-18> degrees, ASTRONOMICAL

Astronomical twilight (the sky is completely dark)

=back

=cut

sub getSunriseSunset ($$$$;$$) {
	my $self = shift;
	my ($lon, $lat, $TZ, $isdst, $alt) = @_;
	my $altit = $alt || -0.833;

	## NOTE: 2451545 IS THE JULIAN DAY OF DECEMBER 19, 1999 (ACCORDING TO THE JULIAN CALENDAR)	
	my $d = $self->{_mnjday} - 2451545 + 0.5 - $lon / 360.0;
	my ($tmp_rise_1, $tmp_set_1) = Ponomar::Sunrise::sun_rise_set($d, $lon, $lat, $altit, 15.04107);
	
	my $tmp_rise_2 = 9;
	my $tmp_rise_3 = 0;
	until ( Ponomar::Sunrise::equal($tmp_rise_2, $tmp_rise_3, 8) ) {
		my $d_sunrise_1 = $d + $tmp_rise_1 / 24.0;
		($tmp_rise_2, undef) = Ponomar::Sunrise::sun_rise_set($d_sunrise_1, $lon, $lat, $altit, 15.04107);
		$tmp_rise_1 = $tmp_rise_3;
		my $d_sunrise_2 = $d + $tmp_rise_2 / 24.0;
		($tmp_rise_3, undef) = Ponomar::Sunrise::sun_rise_set($d_sunrise_2, $lon, $lat, $altit, 15.04107);
	}
	
	my $tmp_set_2 = 9;
	my $tmp_set_3 = 0;
	
	until ( Ponomar::Sunrise::equal($tmp_set_2, $tmp_set_3, 8) ) {
		my $d_sunset_1 = $d + $tmp_set_1 / 24.0;
		(undef, $tmp_set_2) = Ponomar::Sunrise::sun_rise_set($d_sunset_1, $lon, $lat, $altit, 15.04107);
		$tmp_set_1 = $tmp_set_3;
		my $d_sunset_2 = $d + $tmp_set_2 / 24.0;
		(undef, $tmp_set_3) = Ponomar::Sunrise::sun_rise_set($d_sunset_2, $lon, $lat, $altit, 15.04107);
	}
	
	return Ponomar::Sunrise::convert_hour($tmp_rise_3, $tmp_set_3, $TZ, $isdst);
}

# local (non-exportable sub) to test if a year is a leap year according to the Milankovich calendar
sub mIsMilankovichLeap {
	my $year = shift;
	if ($year % 4 == 0) {
		if ($year % 100 == 0) {
			return ( (($year / 100) % 9 == 2) || (($year / 100) % 9) == 6);
		} else {
			return 1;
		}
	}
	return 0;
}


=item getMilankovichYear()

Returns the year of the JDate object according to the (proleptic) Milankovich calendar.

=cut

sub getMilankovichYear {
	my $self = shift;
	my $Days = floor ($self->{_mnjday} - 1721425.5);
	my $PriorCenturies = floor($Days / 36524);
	my $R1 = $Days - 36524 * $PriorCenturies - floor((2 * $PriorCenturies + 6) / 9);
	my $PriorSubCycles = floor($R1 / 1461);
	$R1 %= 1461;
	my $PriorYears = floor ($R1 / 365);
	my $year = 100 * $PriorCenturies + 4 * $PriorSubCycles + $PriorYears;
	return $R1 % 365 == 0 ? $year : $year + 1;
}

=item getMilankovichMonth()

Returns the month of the JDate object according to the (proleptic) Milankovich calendar.

=cut

sub getMilankovichMonth {
	my $self = shift;
	# TODO: fix this computation so that there is no duplication of code with the above method
	my $Days = floor ($self->{_mnjday} - 1721425.5);
	my $PriorCenturies = floor($Days / 36524);
	my $R1 = $Days - 36524 * $PriorCenturies - floor((2 * $PriorCenturies + 6) / 9);
	my $PriorSubCycles = floor($R1 / 1461);
	$R1 %= 1461;
	my $PriorYears = floor ($R1 / 365);
	my $year = 100 * $PriorCenturies + 4 * $PriorSubCycles + $PriorYears;
	$year++ unless ($R1 % 365 == 0);
	$R1 = $R1 % 365 == 0 ?
		$R1 = mIsMilankovichLeap($year) && $PriorSubCycles == 0 ? 366 : 365 :
		$R1 % 365;
	my $correction = $R1 - 1 < (31 + 28 + mIsMilankovichLeap($year)) ? 0 : 2 - mIsMilankovichLeap($year);
	return floor((12 * ($R1 - 1 + $correction) + 373) / 367);
}

=item getMilankovichDay()

Returns the day of the JDate object according to the (proleptic) Milankovich calendar.

=back

=cut

sub getMilankovichDay {
	my $self = shift;
	my $year = $self->getMilankovichYear();
	my $month = $self->getMilankovichMonth();

	# cheat: get the Julian Day for the first day of this month
	my $f = 1721425.5 + 365 * ($year - 1) + floor(($year - 1) / 4) + floor((367 * $month - 362) / 12) + 1;
	if ($month > 2) {
		if (mIsMilankovichLeap($year)) {
			$f--;
		} else {
			$f -= 2;
		}
	}

	# correction for prior century leap years
	$f += -1 * floor(($year - 1) / 100) + floor( (2 * floor(($year - 1) / 100) + 6) / 9);
	return floor($self->{_mnjday} - $f + 1);
}

1;

__END__
