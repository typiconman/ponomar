package Ponomar::Sunrise;

=head1 Ponomar::Sunrise

Ponomar::Sunrise - Module that provides functions for the computation of Sunrise and Sunset

=cut

use strict;

use POSIX qw(floor);
use Math::Trig;
use Carp;
use vars qw( $VERSION @ISA @EXPORT @EXPORT_OK %EXPORT_TAGS $RADEG $DEGRAD );

require Exporter;

@ISA       = qw( Exporter );
@EXPORT    = qw(  );
@EXPORT_OK = qw( DEFAULT CIVIL NAUTICAL AMATEUR ASTRONOMICAL );
%EXPORT_TAGS = ( 
	constants => [ @EXPORT_OK ],
	);

$VERSION =  '0.91';
$RADEG   = ( 180 / pi );
$DEGRAD  = ( pi / 180 );
my $INV360     = ( 1.0 / 360.0 );

my $upper_limb = '1';

sub sun_rise_set {
    my ($d, $lon, $lat,$altit) = @_;
    my $sidtime = revolution( GMST0($d) + 180.0 + $lon );

    my ( $sRA, $sdec ) = sun_RA_dec($d);
    my $tsouth  = 12.0 - rev180( $sidtime - $sRA ) / 15.0;
    my $sradius = 0.2666 / $sRA;

    if ($upper_limb) {
        $altit -= $sradius;
    }

    # Compute the diurnal arc that the Sun traverses to reach 
    # the specified altitude altit: 

    my $cost =
      ( sind($altit) - sind($lat) * sind($sdec) ) /
      ( cosd($lat) * cosd($sdec) );

    my $t;
    if ( $cost >= 1.0 ) {
        carp "Sun never rises!!\n";
        $t = 0.0;    # Sun always below altit
    }
    elsif ( $cost <= -1.0 ) {
        carp "Sun never sets!!\n";
        $t = 12.0;    # Sun always above altit
    }
    else {
        $t = acosd($cost) / 15.0;    # The diurnal arc, hours
    }

    # Store rise and set times - in hours UT 

    my $hour_rise_ut = $tsouth - $t;
    my $hour_set_ut  = $tsouth + $t;
    return($hour_rise_ut, $hour_set_ut);
}

#########################################################################################################
sub GMST0 {
#
#
# FUNCTIONAL SEQUENCE for GMST0 
#
# _GIVEN
# Day number
#
# _THEN
#
# computes GMST0, the Greenwich Mean Sidereal Time  
# at 0h UT (i.e. the sidereal time at the Greenwhich meridian at  
# 0h UT).  GMST is then the sidereal time at Greenwich at any     
# time of the day..
# 
#
# _RETURN
#
# Sidtime
#
    my ($d) = @_;

    my $sidtim0 = revolution( ( 180.0 + 356.0470 + 282.9404 ) + ( 0.9856002585 + 4.70935E-5 ) * $d );
    return $sidtim0;
}

sub sunpos {

#
#
# FUNCTIONAL SEQUENCE for sunpos
#
# _GIVEN
#  day number
#
# _THEN
#
# Computes the Sun's ecliptic longitude and distance 
# at an instant given in d, number of days since     
# 2000 Jan 0.0. 
# 
#
# _RETURN
#
# ecliptic longitude and distance
# ie. $True_solar_longitude, $Solar_distance
#
    my ($d) = @_;

    #                       Mean anomaly of the Sun 
    #                       Mean longitude of perihelion 
    #                         Note: Sun's mean longitude = M + w 
    #                       Eccentricity of Earth's orbit 
    #                       Eccentric anomaly 
    #                       x, y coordinates in orbit 
    #                       True anomaly 

    # Compute mean elements 
    my $Mean_anomaly_of_sun = revolution( 356.0470 + 0.9856002585 * $d );
    my $Mean_longitude_of_perihelion = 282.9404 + 4.70935E-5 * $d;
    my $Eccentricity_of_Earth_orbit  = 0.016709 - 1.151E-9 * $d;

    # Compute true longitude and radius vector 
    my $Eccentric_anomaly =
      $Mean_anomaly_of_sun + $Eccentricity_of_Earth_orbit * $RADEG *
      sind($Mean_anomaly_of_sun) *
      ( 1.0 + $Eccentricity_of_Earth_orbit * cosd($Mean_anomaly_of_sun) );

    my $x = cosd($Eccentric_anomaly) - $Eccentricity_of_Earth_orbit;

    my $y =
      sqrt( 1.0 - $Eccentricity_of_Earth_orbit * $Eccentricity_of_Earth_orbit )
      * sind($Eccentric_anomaly);

    my $Solar_distance = sqrt( $x * $x + $y * $y );    # Solar distance
    my $True_anomaly = atan2d( $y, $x );               # True anomaly

    my $True_solar_longitude =
      $True_anomaly + $Mean_longitude_of_perihelion;    # True solar longitude

    if ( $True_solar_longitude >= 360.0 ) {
        $True_solar_longitude -= 360.0;    # Make it 0..360 degrees
    }

    return ( $Solar_distance, $True_solar_longitude );
}

sub sun_RA_dec {

#
#
# FUNCTIONAL SEQUENCE for sun_RA_dec 
#
# _GIVEN
# day number, $r and $lon (from sunpos) 
#
# _THEN
#
# compute RA and dec
# 
#
# _RETURN
#
# Sun's Right Ascension (RA) and Declination (dec)
# 
#
    my ($d) = @_;

    # Compute Sun's ecliptical coordinates 
    my ( $r, $lon ) = sunpos($d);

    # Compute ecliptic rectangular coordinates (z=0) 
    my $x = $r * cosd($lon);
    my $y = $r * sind($lon);

    # Compute obliquity of ecliptic (inclination of Earth's axis) 
    my $obl_ecl = 23.4393 - 3.563E-7 * $d;

    # Convert to equatorial rectangular coordinates - x is unchanged 
    my $z = $y * sind($obl_ecl);
    $y = $y * cosd($obl_ecl);

    # Convert to spherical coordinates 
    my $RA  = atan2d( $y, $x );
    my $dec = atan2d( $z, sqrt( $x * $x + $y * $y ) );

    return ( $RA, $dec );

}    # sun_RA_dec

sub sind {
    sin( ( $_[0] ) * $DEGRAD );
}

sub cosd {
    cos( ( $_[0] ) * $DEGRAD );
}

sub tand {
    tan( ( $_[0] ) * $DEGRAD );
}

sub atand {
    ( $RADEG * atan( $_[0] ) );
}

sub asind {
    ( $RADEG * asin( $_[0] ) );
}

sub acosd {
    ( $RADEG * acos( $_[0] ) );
}

sub atan2d {
    ( $RADEG * atan2( $_[0], $_[1] ) );
}

sub revolution {
#
#
# FUNCTIONAL SEQUENCE for revolution
#
# _GIVEN
# any angle
#
# _THEN
#
# reduces any angle to within the first revolution 
# by subtracting or adding even multiples of 360.0
# 
#
# _RETURN
#
# the value of the input is >= 0.0 and < 360.0
#

    my $x = $_[0];
    return ( $x - 360.0 * floor( $x * $INV360 ) );
}

sub rev180 {
#
#
# FUNCTIONAL SEQUENCE for rev180
#
# _GIVEN
# 
# any angle
#
# _THEN
#
# Reduce input to within +180..+180 degrees
# 
#
# _RETURN
#
# angle that was reduced
#
    my ($x) = @_;
    
    return ( $x - 360.0 * floor( $x * $INV360 + 0.5 ) );
}

sub equal {
    my ($A, $B, $dp) = @_;

    # needs to be untainted (why?)
    if ($A =~ /(^-?\d+\.?\d*$)/) {
	$A = $1;
    } else { carp "not a real number"; }
    if ($B =~ /(^-?\d+\.?\d*$)/) {
	$B = $1;
    } else { carp "not a real number"; }

    return sprintf("%.${dp}g", $A) eq sprintf("%.${dp}g", $B);
  }


sub convert_hour   {

#
#
# FUNCTIONAL SEQUENCE for convert_hour 
#
# _GIVEN
# Hour_rise, Hour_set, Time zone offset, DST setting
# hours are in UT
#
# _THEN
#
# convert to local time
# 
#
# _RETURN
#
# hour:min rise and set 
#

  my ($hour_rise_ut, $hour_set_ut, $TZ, $isdst) = @_;

  my $rise_local = $hour_rise_ut + $TZ;
  my $set_local = $hour_set_ut + $TZ;
  if ($isdst) {
    $rise_local +=1;
    $set_local +=1;
  }

  # Rise and set should be between 0 and 24;
  if ($rise_local<0) {
    $rise_local+=24;
  } elsif ($rise_local>24) {
    $rise_local -=24;
  }
  if ($set_local<0) {
    $set_local+=24;
  } elsif ($set_local>24) {
    $set_local -=24;
  }

  my $hour_rise =  int ($rise_local);
  my $hour_set  =  int($set_local);

  my $min_rise  = floor(($rise_local-$hour_rise)*60+0.5);
  my $min_set   = floor(($set_local-$hour_set)*60+0.5);

  if ($min_rise>=60) {
    $min_rise -=60;
    $hour_rise+=1;
    $hour_rise-=24 if ($hour_rise>=24);
  }
  if ($min_set>=60) {
    $min_set -=60;
    $hour_set+=1;
    $hour_set-=24 if ($hour_set>=24);
  }

  if ( $min_rise < 10 ) {
    $min_rise = sprintf( "%02d", $min_rise );
  }
  if ( $min_set < 10 ) {
    $min_set = sprintf( "%02d", $min_set );
  }
  $hour_rise = sprintf( "%02d", $hour_rise );
  $hour_set  = sprintf( "%02d", $hour_set );
  return ( "$hour_rise:$min_rise", "$hour_set:$min_set" );

}

sub DEFAULT      () { -0.833 }
sub CIVIL        () { - 6 }
sub NAUTICAL     () { -12 }
sub AMATEUR      () { -15 }
sub ASTRONOMICAL () { -18 }

=head3 DESCRIPTION

Modified Astro::Sunrise to work with dates on the Julian Calendar. Removed dependency on DateTime. Removed wrapper methods, which have been moved to Ponomar::JDate. Basic computation kept in tact.

=head3 SPECIAL THANKS

Modified from Astro::Sunrise, which is by
Ron Hill
rkhill@firstlight.net

=head3 COPYRIGHT and LICENSE

Here is the copyright information provided by Ron Hill in Astro::Sunrise:

Here is the copyright information provided by Paul Schlyer:

Written as DAYLEN.C, 1989-08-16

Modified to SUNRISET.C, 1992-12-01

(c) Paul Schlyter, 1989, 1992

Released to the public domain by Paul Schlyter, December 1992

Permission is hereby granted, free of charge, to any person obtaining a
copy of this software and associated documentation files (the "Software"),
to deal in the Software without restriction, including without limitation
the rights to use, copy, modify, merge, publish, distribute, sublicense,
and/or sell copies of the Software, and to permit persons to whom the
Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included
in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
THE SOFTWARE.

=head3 SEE ALSO

L<Astro::Sunrise>

=cut

1;
