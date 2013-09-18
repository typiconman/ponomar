package Ponomar::Saint;

=head1 Ponomar::Saint

Ponomar::Saint : a Commemoration / Saint object for the Ponomar API

=cut

use strict;
require 5.004;
require Carp;
use vars qw( $VERSION $parser @GLOBALS );
use overload
	'==' => "equals",
	'!=' => "notEquals",
	'""' => "stringify";
use XML::Parser;
use Ponomar::Util;
use Ponomar::I18n;
use Ponomar::Service;
use Ponomar::Reading;

BEGIN {
	$VERSION = 0.01;
	@GLOBALS = qw /dow doy nday Year GS Tone dRank/;	
}
our ($dow, $doy, $nday, $ndayP, $ndayF, $dRank, $Year, $language, $GS);

=head3 METHODS

=over 4

=item new (%attrs)

Creates a new Saint object and runs init(), loading XML data for this Saint

Saint object have the following properties

	CId : the _C_ommemoration ID
	Name: A hash of Names for the Saint, which contains keys Nominative, Genetive, Short, etc.
	Src: the Src of the commemoration (triodion, pentecostarion, or menaion)
	Services: an array consisting the Services defined for this Saint 

B<CURRENT LIMITATION>: ONLY ONE SET OF SERVICES IS ALLOWED FOR A SAINT. THIS NEEDS TO BE FIXED!

Additional limitation: I have internally set C<$GS> to be equal to 1. This needs to be fixed.

=cut

sub new {
	my $class = shift;
	my %pars = @_;
$GS = 1; ## FIXME
	my $self = bless \%pars, $class;
	$self->init();
	return $self;
}

sub init {
	my $self = shift;
	
	$parser = XML::Parser->new(
		Handlers => {
			Start   => sub { $self->startElement(@_) },
			End     => sub { $self->endElement(@_) },
			Char    => sub { $self->text(@_) },
			Default => sub { $self->default(@_) } 
		});

	local $language = $self->{Lang};
	my $date = $self->{Date};
	my $year = $date->getYear();
	my $thispascha = getPascha($year);
	my $nextpascha = getPascha($year + 1);
	my $lastpascha = getPascha($year - 1);
	
	#### GET TODAY'S GLOBAL VARIABLES
	local $dow   = $date->getDayOfWeek();
	local $doy   = $date->getDoy();
	local $nday  = $date->getDaysSince($thispascha);
	local $ndayP = $date->getDaysSince($lastpascha);
	local $ndayF = $date->getDaysSince($nextpascha);
	local $Year  = $date->getYear();

	my $CId = $self->{CId};
	foreach my $file (findTopDown($language, "xml/lives/$CId.xml")) {
		eval {
			$parser->parsefile( $file );
		};
		Carp::croak(__PACKAGE__ . "::init() - Parsing error $@ in file " . $file) if ($@);
	}
	
	return 1;
}

=item getKey($key)

Returns the value of the C<$key> in this Saint, e.g.,

	$self->getKey('CId')

returns the CId of the Saint

=cut

sub getKey ($$) {
	my $self = shift;
	my $key  = shift;
	
	return $self->{$key};
}

=item setKey($key, $value)

Sets the $key of the Saint equal to $value, e.g.,

	$self->setKey('CId', 9000)

=cut

sub setKey ($$$) {
	my $self = shift;
	my $key  = shift;
	my $value = shift;
	
	$self->{$key} = $value;
	return $value;
}

=item equals($other)

Tests to see if this saint is equal to C<$other> saint

The following are equivalent:

1. C<$saintA> is equal to C<$saintB>

2. C<< $saintA->{Cid} >> is equal to C<< $saintB->{Cid} >>

This method has been overwritten as ==, so you can write C<$saintA == $saintB>.

=cut

sub equals {
	my $self = shift;
	my $other = shift;
	
	return $self->{CId} eq $other->{CId};
}

=item notEquals($other)

Tests to see if this saint is not equal to C<$other> saint

The following are equivalent:

1. C<$saintA> is not equal to C<$saintB>

2. C<< $saintA->{Cid} >> is not equal to C<< $saintB->{Cid} >>

This method has been overwritten as C<!=>, so you can write C<$saintA != $saintB>.

=cut

sub notEquals {
	my $self = shift;
	my $other = shift;
	
	return $self->{CId} ne $other->{CId};
}

=item stringify

Returns a string. By definition, the stringification of the Saint is his CId. This method is overwritten by "", so you can write C<"$saint">, if you'd like

=cut

sub stringify {
	my $self = shift;

	return $self->{CId};
}

=item addService($type)

Adds a Service object of type C<$type> to self. Type here is the type of service, e.g., vespers, matins, liturgy, etc.

=cut

sub addService {
	my $self = shift;
	my $type = shift;
	my $service = new Ponomar::Service( Type => $type, dRank => $self->{Type}, parent => $self );
	push @{ $self->{_services} }, $service;
	return 1;
}

=item getServices( [$type] )

Returns an array of Service objects associated with this saint. If C<$type> is specified, returns only those Service objects of a particular C<$type>. C<$type> is the Type of service, e.g., vespers, matins, liturgy, etc.

=cut

sub getServices {
	my $self = shift;
	my $type = shift;
	return defined $type ?
		grep { $_->getType() eq $type } @{ $self->{_services} } :
		@{ $self->{_services} };
}

=item hasServices( [$type] )

Returns true if the Saint has associated Service objects and false otherwise. If C<$type> is specified, returns true if Saint has associated Service of C<$type> and false otherwise.

=cut

sub hasServices {
	my $self = shift;
	my $type = shift;
	
	return defined $type ?
		scalar grep { $_->getType() eq $type } @{ $self->{_services} } :
		scalar $self->{_services};
}

=item hasIcons ( [$lang] )

Returns true if the Saint has associated Icons. If C<$lang> is specified, returns true if the Saint has associated Icons in language C<$lang>. Otherwise, returns false.

=back

=cut

sub hasIcons {
	my $self = shift;
	my $lang = shift;
	
	if (defined $lang) {
		eval {
			findBottomUp( $lang, '/icons/' . $self->{CId} );
		};
		return $@ ? 0 : 1;
	}
	my @languages = Ponomar::I18n::getAvailableLanguages();
	foreach (@languages) {
		eval {
			findBottomUp( $_, '/icons/' . $self->{CId} );
		};
		return 1 unless ($@);
	}
	return 0;
}

############################# THE FOLLOWING SUBS ARE PRIVATE ################
sub default {
	return;
}

sub text {
	## FIXME: LIFE SHOULD BE READ HERE
	return;
}

sub startElement {
	my( $self, $parseinst, $element, %attrs ) = @_;
	
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
			@{ $self->{Name} }{keys %attrs} = values %attrs;
			last SWITCH;
		}
		if ($element eq "SERVICE") {
			$self->{Type} = $attrs{Type};
			last SWITCH;
		}
		if ($element eq "SCRIPTURE") {
			last SWITCH unless defined $self->{_whichService};
			my $type = $attrs{Type};
			delete @attrs{ qw(Type) };
			my $reading = new Ponomar::Reading( %attrs );
			$reading->setSaint($self->{CId});
			### XXX: BAD CODE HERE, THERE SHOULD BE NO NEED FOR THE GREP
			grep { $_->addReading( $reading ) } $self->getServices($self->{_whichService});
			last SWITCH;
		}
		if ($element eq "VESPERS") {
			$self->{_whichService} = 'vespers';
			$self->addService('vespers');
			last SWITCH;
		}
		if ($element eq "MATINS") {
			$self->{_whichService} = 'matins';
			$self->addService('matins');
			last SWITCH;
		}
		if ($element eq "LITURGY") {
			$self->{_whichService} = 'liturgy';
			$self->addService('liturgy');
			last SWITCH;
		}
		if ($element eq "SEXTE") {
			$self->{_whichService} = 'sexte';
			$self->addService('sexte');
			last SWITCH;
		}
		if ($element eq "PRIMES") {
			$self->{_whichService} = 'prime';
			$self->addService('prime');
			last SWITCH;
		}
		if ($element eq "TERCE") {
			$self->{_whichService} = 'terce';
			$self->addService('terce');
			last SWITCH;
		}
		if ($element eq "NONE") {
			$self->{_whichService} = 'none';
			$self->addService('none');
			last SWITCH;
		}
	};
	return 1;
}

sub endElement {
	my( $self, $parseinst, $element, %attrs ) = @_;
	
	if ($element eq "VESPERS" || $element eq "MATINS" || 
		$element eq "LITURGY" || $element eq "SERVICE" ||
		$element eq "PRIMES" || $element eq "TERCE" ||
		$element eq "SEXTE" || $element eq "NONE") {
		undef $self->{_whichService};
	}
	return 1;
}
#############################################################################

1;
