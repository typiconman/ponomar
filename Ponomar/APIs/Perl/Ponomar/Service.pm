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
	$VERSION = 0.02;
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

=cut

sub new {
	my $class = shift;
	my $self  = { @_ };
	$GS = $self->{GS} || 0;
	delete $self->{GS};
	bless $self, $class;
	# now load the instructions
	if ($self->{Type} eq LITURGY) {
		## SET UP XML PARSER
		my $parser = XML::Parser->new(
			Handlers => {
			Start   => sub { $self->startElement(@_) },
			End     => sub { $self->endElement(@_) },
			Char    => sub { $self->text(@_) },
			Default => sub { $self->default(@_) } 
		});
		$parser->parsefile( findBottomUp($language, "xml/Commands/DivineLiturgy.xml") );
		# now we have a bunch of commands in @{ $self->{commands} }
	}
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

=item clearCommands ( [$commandName] )

Removes all liturgical commands named C<$commandName> from this service object. 
If C<$commandName> is C<undef>, removes all liturgical commands period.
Returns nothing.

=back

=cut

sub clearCommands {
	my $self = shift;

	my $cmdName = shift;
	if (defined $cmdName) {
		@{ $self->{commands} } = grep { $_->{Name} ne $cmdName } @{ $self->{commands} }
	} else {
		@{ $self->{commands} } = ();
	}
	return;
}

=item addCommands ( $name, $value ) 

Given a liturgical command  with name C<$name> and boolean test <$value>, adds this command to the list of
available commands for this Service.
See the documentation for DivineLiturgy.xml for the list of allowable command names.
Note that C<$value> is not checked for correct syntax. (FIXME)
If either C<$name> or C<$value> is undefined, the mothod will croak.
Returns nothing

=back

=cut

sub addCommands {
	my $self = shift;
	my ($name, $value) = @_;

	Carp::croak (__PACKAGE__ . "::addCommands - Invalid input") unless (defined $name && defined $value);
	my %hash = (Name => $name, Value => $value);
	push (@{ $self->{commands} } , \%hash);
	return;
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
		return; ## XXX: fails on Sundays because $reading->getCmd is not defined!
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

	# step 1. Check if readings today are suppressed and not transferred
	foreach (@{ $self->{commands} }) {
		my $cmd = $_->{Value};

		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		next unless eval $cmd;

		if ( $_->{Name} eq "Suppress" || $_->{Name} eq "Class3Transfers") {
			$self->{_readings} = ();
			return;
		}
	}

	# if we got down here, then both Suppress and Class3Transfers are ZERO
	# now we check if any readings from tomorrow or yesterday are transferred to today
	# first check if transfer is implemented
	# TODO: this should check if we are outside of Pentecostarion / Lenten Triodion periods
	foreach (@{ $self->{commands} }) {
		my $cmd = $_->{Value};

		foreach (@GLOBALS) { # FIXME: get rid of all of this GLOBALS foreach'ing stuff
			$cmd =~ s/$_/\$$_/g;
		}

		if ( $_->{Name} eq "Transfer" && eval ($cmd) == 0) {
			return; # transferring not implemented, so we quit.
		}
	}

	# now check if we need to set up tomorrow
	foreach (@{ $self->{commands} }) {
		next unless $_->{Name} eq 'TransferRulesB';
		my $cmd = $_->{Value};
		
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		next unless eval $cmd;
		
		## IF WE GOT HERE, THEN we must check tomorrow for transfers
		## set up tomorrow
		my $tomorrow = $date->addDays(1);
		local $dow   = $tomorrow->getDayOfWeek();
		local $doy   = $tomorrow->getDoy();
		local $nday  = $tomorrow->getDaysSince($thispascha);
		local $ndayP = $tomorrow->getDaysSince($lastpascha);
		local $ndayF = $tomorrow->getDaysSince($nextpascha);
	
		my $ponomar  = Ponomar->new($tomorrow, $language);
		## get tomorrow's dRank
		local $dRank = max (  map { $_->getKey("Type") } $ponomar->getSaints('menaion') );

		# check if tomorrow is suppressed.
		foreach (@{ $self->{commands} }) {
			next unless $_->{Name} eq 'Class3Transfers';
			my $cmd = $_->{Value};
		
			foreach (@GLOBALS) {
				$cmd =~ s/$_/\$$_/g;
			}
			next unless eval $cmd;
			# if we've gotten down here, then we must transfer tomorrow's readings to today
			foreach ($ponomar->getReadings('liturgy', 'pentecostarion')) {
			#next unless ($_->getSaint() >= 9000 && $_->getSaint() <= 9315); FIXME
				push @{ $self->{_readings} }, $_;
			}
		}
	}

	## now we must perform the same operation for yesterday
	foreach (@{ $self->{commands} }) {
		next unless $_->{Name} eq 'TransferRulesF';
		my $cmd = $_->{Value};
		
		foreach (@GLOBALS) {
			$cmd =~ s/$_/\$$_/g;
		}
		next unless eval $cmd;
		
		# if we got here, then we have to check yesterday for transfers
		my $yesterday = $date->subtractDays(1);
		local $dow   = $yesterday->getDayOfWeek();
		local $doy   = $yesterday->getDoy();
		local $nday  = $yesterday->getDaysSince($thispascha);
		local $ndayP = $yesterday->getDaysSince($lastpascha);
		local $ndayF = $yesterday->getDaysSince($nextpascha);
	
		my $ponomar  = Ponomar->new($yesterday, $language);
		local $dRank = max (  map { $_->getKey("Type") } $ponomar->getSaints('menaion') );

		# check if yesterday is suppressed.
		foreach (@{ $self->{commands} }) {
			next unless $_->{Name} eq 'Class3Transfers';
			my $cmd = $_->{Value};
		
			foreach (@GLOBALS) {
				$cmd =~ s/$_/\$$_/g;
			}
			next unless eval $cmd;
			# if we've gotten down here, then we must transfer tomorrow's readings to today
			foreach ($ponomar->getReadings('liturgy', 'pentecostarion')) {
			#next unless ($_->getSaint() >= 9000 && $_->getSaint() <= 9315); FIXME
				push @{ $self->{_readings} }, $_;
			}
		}
	}
	return 1;
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
		delete $attrs{Comment};
		push @{ $self->{commands} }, \%attrs;
	}
	return 1;
}

sub endElement {
	my( $self, $parseinst, $element, %attrs ) = @_;
	
	return 1;
}

1;
