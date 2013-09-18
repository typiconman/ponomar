package Ponomar::Service;

=head1 Ponomar::Service

Ponomar::Service - A service object for the Ponomar API

=cut

use strict;
require 5.004;
require Carp;
use vars qw( $VERSION @GLOBALS );
use Ponomar::Util;
use XML::Parser;

BEGIN {
	$VERSION = 0.01;
	@GLOBALS = qw /dow doy nday Year GS Tone dRank/;	
}

our ($dow, $doy, $nday, $ndayP, $ndayF, $dRank, $Year, $language, $GS);
### SERVICE TYPES
sub VESPERS { 'vespers'; }
sub MATINS  { 'matins';  }
sub LITURGY { 'liturgy'; }

=head3 METHODS

=over 4

=item new(%properties)

Creates a new Service object

Service objects have the following properties
	type: The type of service (vespers, matins, etc)
	dRank: The Rank of this service (NOT the rank of the day)
	parent: a reference to a Saint object which begat this Service

BUG ALERT: C<$GS> is treated as 1 for now.

=cut

sub new {
	my $class = shift;
	my $self  = { @_ };
$GS = 1; ## FIXME
	bless $self, $class;
	return $self;
}

=item addReading($reading)

Assings a Reading object C<$reading> to the service.

=cut

sub addReading {
	my ($self, $reading) = @_;
	push @{ $self->{_readings} }, $reading;
	return 1;
}

=item deleteReading($reading)

Removes a Reading object C<$reading> from the service.

=cut

sub deleteReading {
	my ($self, $reading) = @_;
	@{ $self->{_readings} } = grep { $_ != $reading } @{ $self->{_readings} };
	return 1;
}

=item getReadings

Returns the set of Readings objects assigned to the service

=cut

sub getReadings {
	my $self = shift;
	return scalar $self->{_readings} ? @{ $self->{_readings} } : undef;
}

=item hasReadings

Returns true if the Service has Readings objects assigned

=cut

sub hasReadings {
	my $self = shift;
	return scalar $self->{_readings};
}

=item getType

Returns the Type of the service (e.g., vespers, matins, etc.)

=cut

sub getType {
	my $self = shift;

	return $self->{Type};
}

=item execCommands( $dRank )

Given the C<$dRank> of the day, executes a set of commands associated with this service. These commands may generate instructions, rearrange readings or do other operations.

Presently, only one set of Commands is supported, this is the set of commands recorded in C<DivineLiturgy.xml>. Thus, if the type of the service object is liturgy, the file C<DivineLiturgy.xml> is processed and readings are suppressed or transferred as necessary.

If the type is not liturgy, nothing is done and undef is returned.

If C<< $self->{parent} >> is not a reference to a Saint, which is the parent of this Service,
the method will croak.

=back

=cut

sub execCommands {
	my $self = shift;
	local $dRank = shift;

	if ($self->{Type} eq 'matins' && $self->hasReadings()) {
		return;
		foreach my $reading ($self->getReadings()) {
			my $cmd = $reading->getCmd();

			foreach (@GLOBALS) {
				$cmd =~ s/$_/\$$_/g;
			}
			$self->deleteReading($reading) unless eval $cmd;
		}
	}
	
	return unless $self->{Type} eq 'liturgy';
	Carp::croak (__PACKAGE__ . "::execCommands - Invalid parent") unless (ref $self->{parent} eq "Ponomar::Saint");

	local $language = $self->{parent}->getKey('Lang');
	my $date = $self->{parent}->getKey('Date');
	local $Year = $date->getYear();
	my $thispascha = getPascha($Year);
	my $nextpascha = getPascha($Year + 1);
	my $lastpascha = getPascha($Year - 1);

	#### GET TODAY'S GLOBAL VARIABLES
	local $dow   = $date->getDayOfWeek();
	local $doy   = $date->getDoy();
	local $nday  = $date->getDaysSince($thispascha);
	local $ndayP = $date->getDaysSince($lastpascha);
	local $ndayF = $date->getDaysSince($nextpascha);
		
	## SET UP XML PARSER
	my $parser = XML::Parser->new(
		Handlers => {
			Start   => sub { $self->startElement(@_) },
			End     => sub { $self->endElement(@_) },
			Char    => sub { $self->text(@_) },
			Default => sub { $self->default(@_) } 
		});
	$parser->parsefile( findBottomUp($language, "xml/Commands/DivineLiturgy.xml") );
	
	## now we have a bunch of commands in $self->{commands}
	foreach (@{ $self->{commands} }) {
		my $cmd = $_->{Value};

		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		next unless eval $cmd;

		if ( $_->{Name} eq "Suppress" || $_->{Name} eq "TransferRulesB" || $_->{Name} eq "TransferRulesF" ) {
			## IF WE'RE HERE, THEN WE MUST DELETE THE READINGS ASSOCIATED WITH THIS SERVICE
			$self->{_readings} = () unless $self->{dRank} >= 1;
		}
	}

	## set up tomorrow
	my $tomorrow = $date->addDays(1);
	local $dow   = $tomorrow->getDayOfWeek();
	local $doy   = $tomorrow->getDoy();
	local $nday  = $tomorrow->getDaysSince($thispascha);
	local $ndayP = $tomorrow->getDaysSince($lastpascha);
	local $ndayF = $tomorrow->getDaysSince($nextpascha);
	
	my $ponomar  = Ponomar->new($tomorrow, $language);
	## get tomorrow's dRank
	local $dRank = max (  map { $_->getKey("Type") } $ponomar->getSaints() );
	foreach (@{ $self->{commands} }) {
		next unless $_->{Name} eq 'TransferRulesB';
		my $cmd = $_->{Value};
		
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		next unless eval $cmd;
		
		## IF WE GOT HERE, THEN TOMORROW'S READINGS ARE READ TODAY
		foreach ($ponomar->getReadings('liturgy', 'pentecostarion')) {
			next unless ($_->getSaint() >= 9000 && $_->getSaint() <= 9315);
			push @{ $self->{_readings} }, $_;
		}
	}
	
	## set up yesterday
	my $yesterday = $date->subtractDays(1);
	local $dow   = $yesterday->getDayOfWeek();
	local $doy   = $yesterday->getDoy();
	local $nday  = $yesterday->getDaysSince($thispascha);
	local $ndayP = $yesterday->getDaysSince($lastpascha);
	local $ndayF = $yesterday->getDaysSince($nextpascha);
	
	$ponomar  = Ponomar->new($yesterday, $language);
	local $dRank = max (  map { $_->getKey("Type") } $ponomar->getSaints() );
	foreach (@{ $self->{commands} }) {
		next unless $_->{Name} eq 'TransferRulesF';
		my $cmd = $_->{Value};
		
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		next unless eval $cmd;
		
		## IF WE GOT HERE, THEN YESTERDAY'S READINGS ARE READ TODAY
		## CAUTION! THE SOURCE OF THE READINGS MUST BE THE DAY
		foreach ($ponomar->getReadings('liturgy', 'pentecostarion')) {
			next unless ($_->getSaint() >= 9000 && $_->getSaint() <= 9315);
			push @{ $self->{_readings} }, $_;
		}
	}
	
	# let the Perl garbage collector kick in
	undef $self->{commands};
	undef $ponomar;
}

################# PRIVATE METHODS ##########################
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
	if ($element eq "COMMAND") {
		delete $attrs{Cmd};
		push @{ $self->{commands} }, \%attrs;
	}
	return 1;
}

sub endElement {
	my( $self, $parseinst, $element, %attrs ) = @_;
	
	return 1;
}

1;
