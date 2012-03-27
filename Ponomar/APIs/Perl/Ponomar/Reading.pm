package Ponomar::Reading;

=head1 Ponomar::Reading

Ponomar::Reading - a scripture reading object for the Ponomar API

=cut

use strict;
require 5.004;
require Carp;
use vars qw( $VERSION );
use overload
	'==' => "equals",
	'!=' => "notEquals";

BEGIN {
	$VERSION = 0.01;
}

=head3 METHODS

=over 4

=item new( %attributes )

Creates a new Reading object

A Reading object has the following elements

	Reading => The Reading (e.g., Gen_1:1-13)
	Pericope => The Pericope (e.g., 103 -- OPTIONAL)
	EffWeek => The EffWeek (e.g., 17 -- OPTIONAL)
	saint => The Assigned Saint or Commemoration ID

=cut

sub new {
	my $class = shift;
	my $self  = { @_ };
	bless $self, $class;
	return $self;
}

=item getReading()

Returns the Reading of the object (e.g., C<Gen_1:1-13>)

=cut

sub getReading {
	my $self = shift;
	return $self->{Reading};
}

=item setReading($string)

Sets the Reading of the object to $string

=cut

sub setReading {
	my $self = shift;
	my $reading = shift;
	$self->{Reading} = $reading;
}

sub getPericope {
	my $self = shift;
	return $self->{Pericope};
}

sub setPericope {
	my $self = shift;
	my $pericope = shift;
	$self->{Pericope} = $pericope;
}

sub getEffWeek {
	my $self = shift;
	return $self->{EffWeek};
}

sub setEffWeek {
	my $self = shift;
	my $effWeek = shift;
	$self->{EffWeek} = $effWeek;
}

sub getSaint {
	my $self = shift;
	return $self->{saint};
}

sub setSaint {
	my $self = shift;
	my $saint = shift;
	$self->{saint} = $saint;
}

=item equals($other)

Tests to see if this Readings object is equal to another object

The following are equivalent:

1. C<$ReadingsA> is equal to C<$ReadingsB>

2. C<< $ReadingsA->{Reading} = $ReadingsB->{Reading} >> and
C<< $ReadingsA->{Saint}   = $ReadingsB->{Saint} >>
   
Note that this method has been overloaded as C<==> Thus, you can write C<$ReadingsA == $ReadingsB>.

=cut

sub equals {
	my $self = shift;
	my $other = shift;
	
	return  ($self->{reading} == $other->{reading} &&
		 $self->{saint}   == $other->{saint});
}

=item notEquals($other)

Tests to see if this Readings object is not equal to another object

The following are equivalent:

1. C<$ReadingsA> is not equal to C<$ReadingsB>

2. C<< $ReadingsA->{Reading} != $ReadingsB->{Reading} >> OR
C<< $ReadingsA->{Saint}    != $ReadingsB->{Saint} >>

Note that this method has been overloaded as C<!=> Thus, you can write C<$ReadingsA != $ReadingsB>.

=back

=cut

sub notEquals {
	my $self = shift;
	my $other = shift;
	
	return ($self->{reading} != $other->{reading} ||
		$self->{saint}   != $other->{saint});
}

1;

__END__
