package Ponomar::JDate;

use strict;

require 5.004;
require Exporter;
require Carp;

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

CONVENTIONS: January is treated as Month 1. Sunday is treated as day of week 0. January 1 is treated as day of year 0.

These conventions are due to some poor choices of conventions back in the day when Ponomar was first written, in Visual Basic (ASP), and hosted on the Brinkster server (I believe this was in 2005).

=head3 METHODS

=over 4

=item C<new($julian_day)> OR C<new($month, $day, $year)>

Creates a new instance of Ponomar::JDate either set to Month, Day, Year OR to the Julian Day

What is a Julian Day? Read here: http://en.wikipedia.org/wiki/Julian_day

=cut

sub new ($;$$) {
	my $class = shift;
	my $mn_jday;
	
	if (scalar(@_) == 1) {
		$mn_jday = shift;
	} else {
		my ($month, $day, $year) = @_;

		my $a = int((14 - $month) / 12);
		my $y = $year + 4800 - $a;
		my $m = $month + 12 * $a - 3;
		$mn_jday = $day + int((153 * $m + 2) / 5) + 365 * $y + int($y / 4) - 32083;
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
	my $jbar = $mn_jday + 32083;
	## Compute the number of four-year Julian cycles that have elapsed since mn_jday
	## There are 1461 days in each cycle
	## Multiply this number by four (years/cycle)
	my $n1 = int($jbar / 1461) * 4;
	## Compute the number of days since the last four-year cycle
	## Divide by 365 days in a year
	my $n2 = int($jbar % 1461) / 365;
	my $da = int($jbar % 1461);
	## Add one if we are after December 31, since JDate starts March 1
	my $adj = ($da % 365) > 306 ? 1 : 0;

	return int($n1 + $n2) - 4800 + $adj;
}

=item getYearAM()

Returns the Year from the (Byzantine) Creation of the World (anno mundi)
The Creation of the World took place on March 1, 5508 BC.

=cut

sub getYearAM ($) {
	my $self = shift;
	
	my $y = $self->getYear();
	my $cutoff = Ponomar::JDate->new(9, 1, $y);
	return $self->before($cutoff) ? $y + 5508 : $y + 5509;
}

=item getMonth()

Returns the Month of the JDate object

Example:

	$date = new Ponomar::JDate(2, 1, 2001);
	$date->getMonth(); # returns 2

NOTE THAT: January is month 1

=cut

sub getMonth ($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday};
	my $jbar = $mn_jday + 32083;
	## Take jbar modulo 1461 to get the number of days since the last four-year cycle
	my $da = int($jbar % 1461);
	## Take da modulo 365 to get the number of days since the last 1 March
	my $m = &mod($da, 365);
	## now, subtract off days for each of the months
	my $j = 2;
	
	while ($m > $DAYS_IN_LEAP[$j]) {
		$m -= $DAYS_IN_LEAP[$j];
		$j++;
		if ($j == 12) {
			$j = 0;
		}
	}
	return $j + 1;
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
	my $jbar = $mn_jday + 32083;
	## repeat above steps until m
	my $da = &mod($jbar, 1461);
	my $m = 0;
	if ($da == 1461) {
		$m = 29; ## FEBRUARY 29
	} else {
		$m  = &mod($da, 365);
		## this number is the number of days since the last March 1
		## now, take off days for each month
		my $k = 2;
		while ($m > $DAYS_IN_MONTH[$k]) {
			$m -= $DAYS_IN_MONTH[$k];
			$k++;
			if ($k == 12) {
				$k = 0;
			}
		}
	}
	return $m; ## the number of days that will remain at the end
}

=item getDayOfWeek()

Returns the day of the week of the JDate object

Example:

	$date = new Ponomar::JDate(2, 1, 2001);
	$date->getDayOfWeek(); # returns 2

NOTE THAT: Sunday is day of week 0

=cut

sub getDayOfWeek ($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday};
	my $temp = int($mn_jday % 7) + 1;
	if ($temp == 7) {
		$temp = 0;
	}

	return $temp;
}

=item getDayOfWeekString 

Returns the day of the week as a string

=cut

sub getDayOfWeekString ($) {
	my $self = shift;
	
	return $WEEKDAY_NAMES[$self->getDayOfWeek()];
}

=item getDoy()

Returns the day of the year (doy) of a JDate object.

Note that January 1 is doy 0. February 29, if it exists, is doy 366

=cut

sub getDoy ($) {
	my $self = shift;
	
	my $jbar = $self->{_mnjday} + 32083;
	my $da   = mod($jbar, 1461);
	return $da == 1461 ? 366 : mod($da + 59, 365) - 1; ## Jan 1 is doy 0
}

=item getYearGregorian()

Returns the year of the JDate object according to the (proleptic) Gregorian calendar.

=cut

sub getYearGregorian ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	my $j1 = 0;
	if ($mn_jday >= 2299160.5) {
		my $tmp = int((($mn_jday - 1867216.0) - 0.25) / 36524.25);
		$j1 = $mn_jday + 1 + $tmp - int(0.25 * $tmp);
	} else {
		$j1 = $mn_jday;
	}

	my $j2 = $j1 + 1524.0;
	my $j3 = int(6680.0 + (($j2 - 2439870.0) - 122.1) / 365.25);
	my $j4 = int($j3 * 365.25);
	my $j5 = int(($j2 - $j4) / 30.6001);
	my $m = int($j5 - 1.0);
	if ($m > 12) {
		$m -= 12;
	}

	my $y = int($j3 - 4715.0);

	if ($m > 2) {
		--$y;
	}
	if ($y <= 0) {
		--$y;
	}
	return $y;
}

=item getMonthGregorian()

Returns the month of the JDate object according to the (proleptic) Gregorian calendar

=cut

sub getMonthGregorian ($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday};
	my $j1 = 0;

	if ($mn_jday >= 2299160.5) {
		my $tmp = int((($mn_jday - 1867216.0) - 0.25) / 36524.25);
		$j1 = $mn_jday + 1 + $tmp - int(0.25 * $tmp);
	} else {
		$j1 = $mn_jday;
	}

	my $j2 = $j1 + 1524.0;
	my $j3 = int(6680.0 + (($j2 - 2439870.0) - 122.1) / 365.25);
	my $j4 = int($j3 * 365.25);
	my $j5 = int(($j2 - $j4) / 30.6001);

	my $d = int($j2 - $j4 - int($j5 * 30.6001));
	my $m = int($j5 - 1.0);
	if ($m > 12) {
		$m -= 12;
	}
	return $m;
}

=item getDayGregorian()

Returns the day of the Month of the JDate object according to the (proleptic) Gregorian calendar.

=cut

sub getDayGregorian ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	my $j1 = 0;
	if ($mn_jday >= 2299160.5) {
		my $tmp = int((($mn_jday - 1867216.0) - 0.25) / 36524.25);
		$j1 = $mn_jday + 1 + $tmp - int(0.25 * $tmp);
	} else {
		$j1 = $mn_jday;
	}

	my $j2 = $j1 + 1524.0;
	my $j3 = int(6680.0 + (($j2 - 2439870.0) - 122.1) / 365.25);
	my $j4 = int($j3 * 365.25);
	my $j5 = int(($j2 - $j4) / 30.6001);

	return int($j2 - $j4 - int($j5 * 30.6001));
}

=item getDaysSince($date)

Returns the number of days since $date, another JDate object

=cut

sub getDaysSince ($$) {
	my $self = shift;
	my $other = shift;
	return $self->{_mnjday} - $other->{_mnjday};
}

=item getDaysUntil($date)

Returns the number of days until $date, another JDate object

=cut

sub getDaysUntil ($$) {
	my $self = shift;
	my $other = shift;
	
	return $other->{_mnjday} - $self->{_mnjday};
}

=item getWeeksSince($date)

Returns the number of weeks since $date, another JDate object

=cut

sub getWeeksSince ($$) {
	my $self = shift;
	my $other = shift;
	
	return ($self->{_mnjday} - $other->{_mnjday}) / 7;
}

=item getWeeksUntil($date)

Returns the number of weeks until $date, another JDate object

=cut

sub getWeeksUntil ($$) {
	my $self = shift;
	my $other = shift;
	
	return ($other->{_mnjday} - $self->{_mnjday}) / 7;
}

=item addDays($integer)

Returns a new JDate object, advanced by $integer days

=cut

sub addDays ($$) {
	my $self = shift;
	my $n = shift;

	return Ponomar::JDate->new($self->{_mnjday} + $n);
}

=item addOneDay()

Returns a new JDate object, advanced by one day

=cut
sub addOneDay($) {
	my $self = shift;
	
	$self->{_mnjday}++;
}

=item addMonths($integer)

Returns a new JDate object, advanced by $integer months

=cut

sub addMonths($$) {
	my $self = shift;
	my $n    = shift;
	
	my $m    = $self->getMonth();
	return $m == 12 ? Ponomar::JDate->new(1, $self->getDay(), $self->getYear() + 1) :
		Ponomar::JDate->new($m + 1, $self->getDay(), $self->getYear);
}

=item subtractDays($integer)

Returns a new JDate object, diminished by $integer days

=cut

sub subtractDays ($$) {
	my $self = shift;
	my $n = shift;
	
	return Ponomar::JDate->new($self->{_mnjday} - $n);
}

=item subtractOneDay()

Returns a new JDate object, diminished by one day

=cut

sub subtractOneDay($) {
	my $self = shift;
	
	$self->{_mnjday}--;
}

=item subtractMonths($integer)

Returns a new JDate object, diminished by $integer months

=cut

sub subtractMonths($$) {
	my $self = shift;
	my $n    = shift;
	
	my $m    = $self->getMonth();
	return $m == 1 ? Ponomar::JDate->new(12, $self->getDay(), $self->getYear() - 1) :
		Ponomar::JDate->new($m - 1, $self->getDay(), $self->getYear());
}

=item equals($date)

Returns true of this JDate object and the other object $date are the same Julian day. Returns false otherwise.

=cut

sub equals ($$) {
	my $self = shift;
	my $other = shift;
	
	return $self->{_mnjday} == $other->{_mnjday};
}

=item before($date)

Returns true if self is before $date

=cut
sub before($$) {
	my $self = shift;
	my $other = shift;
	
	return $self->{_mnjday} < $other->{_mnjday};
}

=item after($date)

Returns true if self is after $date 

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

## RETURNS THE NEAREST SUNDAY TO A JDATE OBJECT
## XXX: IF TODAY IS A SUNDAY, RETURNS TODAY
sub getNearestSunday ($) {
	my $self = shift;

	my $mn_jday = $self->{_mnjday};
	my $dow = int($mn_jday % 7) + 1;
	if ($dow == 7) {
		$dow = 0;
	}
	
	return $dow <= 3 ? JDate->new($mn_jday - $dow) : JDate->new($mn_jday + 7 - $dow);
}

## RETURNS THE PREVIOUS SUNDAY BEFORE A JDATE OBJECT
## XXX: IF TODAY IS A SUNDAY, RETURNS ONE WEEK BEFORE!
sub getPreviousSunday ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	my $dow = int($mn_jday % 7) + 1;
	
	return Ponomar::JDate->new($mn_jday - $dow);
}

## RETURNS THE NEXT SUNDAY AFTER A JDATE OBJECT
## XXX: IF TODAY IS A SUNDAY, RETURNS ONE WEEK LATER!
sub getNextSunday ($) {
	my $self = shift;
	
	my $mn_jday = $self->{_mnjday};
	my $dow = int($mn_jday % 7) + 1;
	if ($dow == 7) {
		$dow = 0;
	}
	
	return Ponomar::JDate->new($mn_jday + 7 - $dow);
}

## VARIOUS ESOTERIC MATHEMATICAL FUNCTIONS THAT MAY OR MAY NOT BE ACTUALLY USEFUL BEHAVIOR
## HOWEVER, THEY ALLOW JDATES TO BEHAVE LIKE INTEGERS, WHICH IN A SENSE, THEY ARE

## SCALAR PRODUCT
sub times ($$) {
	my $self = shift;
	my $n = shift;
	
	return JDate->new($self->{_mnjday} * $n);
}

## RETURNS THE CLOSEST JDATE TO THE CURRENT JDATE DIVIDED BY N
## THIS MAY BE MARGINALLY USEFULL IN FINDING OUT THINGS LIKE MIDPOINTS OF TIME PERIODS
sub divide ($$) {
	my $self = shift;
	my $n = shift;
	
	Carp::croak ("Division by zero encountered") if ($n == 0);
	return JDate->new(int($self->{_mnjday} / $n));
}

## RETURNS THE JDATE MOD N
## THIS IS PROBABLY COMPLETELY USELESS
sub modulo ($$) {
	my $self = shift;
	my $n = shift;
	
	return $self->{_mnjday} % $n;
}


=item getSunrise

This function will return the sunrise/sunset for a given day.

 Eastern longitude is entered as a positive number
 Western longitude is entered as a negative number
 Northern latitude is entered as a positive number
 Southern latitude is entered as a negative number

Examples:

C<($sunrise, $sunset) = $date->getSunrise(longitude, latitude, TimeZone, DST);>

C<($sunrise, $sunset) = $date->getSunrise(longitude, latitude, TimeZone, DST, ALT);>

Returns the sunrise and sunset times, in HH:MM format.
(Note: Time Zone is the offset from GMT and DST is daylight
savings time, 1 means DST is in effect and 0 is not).  In the first form,
a default altitude of -.0833 is used.  In the second form, the altitude
is specified as the last argument.  Note that adding 1 to the
Time Zone during DST and specifying DST as 0 is the same as indicating the
Time Zone correctly and specifying DST as 1.

a) Compute sunrise or sunset as always, with one exception: to convert LHA from degrees to hours,
   divide by 15.04107 instead of 15.0 (this accounts for the difference between the solar day 
   and the sidereal day.

b) Re-do the computation but compute the Sun's RA and Decl, and also GMST0, for the moment 
   of sunrise or sunset last computed.

c) Iterate b) until the computed sunrise or sunset no longer changes significantly. 
   Usually 2 iterations are enough, in rare cases 3 or 4 iterations may be needed.

There are a number of sun altitides to chose from.  The default is
-0.833 because this is what most countries use. Feel free to
specify it if you need to. Here is the list of values to specify
altitude (ALT) with, including symbolic constants for each.

=over 4

=item B<0> degrees

Center of Sun's disk touches a mathematical horizon

=item B<-0.25> degrees

Sun's upper limb touches a mathematical horizon

=item B<-0.583> degrees

Center of Sun's disk touches the horizon; atmospheric refraction accounted for

=item B<-0.833> degrees, DEFAULT

Sun's supper limb touches the horizon; atmospheric refraction accounted for

=item B<-6> degrees, CIVIL

Civil twilight (one can no longer read outside without artificial illumination)

=item B<-12> degrees, NAUTICAL

Nautical twilight (navigation using a sea horizon no longer possible)

=item B<-15> degrees, AMATEUR

Amateur astronomical twilight (the sky is dark enough for most astronomical observations)

=item B<-18> degrees, ASTRONOMICAL

Astronomical twilight (the sky is completely dark)

=back

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

1;

__END__

