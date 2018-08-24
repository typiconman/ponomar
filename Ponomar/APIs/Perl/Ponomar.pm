package Ponomar;

=head1 Introduction to the Ponomar API

Ponomar is an Object Oriented API; the set of Ponomar classes is designed to eliminate the need for low-level interaction with XML (or YAML) data and CMDs (liturgical CoMmanDs). Basic implementations of calendar software can be written using this API with about 5 lines of code. More complex implementations will require array-based manipulation of Ponomar objects (as in, lots of C<grep>, C<map> and C<foreach>); but it beats working with the XML directly.

Note that the API is designed to be format independent. That is, data outputed by the API is strictly Unicode text; it lacks formatting or markup. (THIS IS NOT ACTUALLY TRUE: certain XML files in Ponomar contain markup, e.g., C<< <SUP> >> tags. This is problematic and should be considered a defect of the API).

Note that Ponomar works with Julian Dates (actually, Julian B<days>). Unless a method specifically contains the words `Gregorian' or `Milankovich',
all calculations and dates are according to the Julian Calendar.

The non-OOO Ponomar::Util class provides handy functions for generating common dates including Today and any year's Pascha without having to deal with the Ponomar::JDate object. See the documentation for Ponomar::Util for details.

=head2 SYNOPSIS

 use Ponomar;
 use Ponomar::JDate;

 $ponomar = new Ponomar(new Ponomar::JDate(1, 1, 2001), 'en');
 print join('; ', map { $_->getKey('Name')->{Nominative} }
     $ponomar->getSaints('menaion'));

=head2 Workflow

Upon initialization, Ponomar immediately loads the Ponomar::I18n helper class, which handles all Internationalization (I18n) support for Ponomar. The C<load()> method of Ponomar::I18n loads into memory Ponomar locale data. Locale data is stored in YAML (YAML A'int a Markup Language) format in the file C<locales.yml>. 

The user begins by creating a Ponomar object. The Ponomar object takes two initial parameters, the C<$date> and the C<$locale>. The C<$locale> is an ISO 639-2 language code (string). The C<$date> is an instance of the Ponomar::JDate class (see documentation for Ponomar::JDate), which is in essence a glorified Integer (Julian Day object).

The constructor of Ponomar calls the C<init()> method, which loads XML data for the given day and locale. The reading of XML DATA proceeds in the following FIFO (First In First Out) order:

=over 4

=item Step 1. The relevant top-level saint files are read for Pentecostarion / Triodion. This creates a set of Ponomar::Saint objects with two properties:

=over 4

=item a. The CId (Commemoration Id)

=item b. The associated JDate object

=item c. The locale

=back

B<Important>: Ponomar Saints are not date-independent entities: saint data may contain CMDs which are date dependent. b. and c., above, are inherited from the underlying Ponomar object.

=item Step 2. At this stage, the Tone is set.

=item Step 3. Loading of Saint data for Pentecostarion / Triodion

Immediately upon construction, the Ponomar::Saint object loads all top-down XML files associated with CId. This will load the NAMEs of the Saint as well as the LIFEs and the SERVICEs.

=item Step 4. Loading of service data for Pentecostarion / Triodion

Presently, SERVICE tags are more or less wrappers. The only useful information obtained from the SERVICE tag is the Type (more properly, ``rank'') of the service. This is used to set the Type property of the Saint object. SERVICE objects also handle Commands (see the documentation of Ponomar::Service for details).

Upon encountering individual services (VESPERS, MATINS, LITURGY, etc), Ponomar::Saint creates a new instance  of Ponomar::Service. The Service object contains only two properties, the Type (which is type of service, more properly, the type of ``office'', e.g., `vespers', `matins', `liturgy', etc) and the C<dRank> of the service (which is the rank and is inheretied from the Type property of the Ponomar::Saint parent). Note that C<dRank> is not the same as the day's C<dRank>, computed below. The confusion in terminology is unfortunate.

B<BUG NOTICE>: As presently written, the API accepts only one SERVICE per Saint. That is, only one type of service may be read in, e.g., reading a second VESPERS service into a Saint will create two Service objects that are completely indistinguishable. This means that subsequent Scripture readings, if any, will be assigned to both Service objects. This will have unforseen consequences, especially for the sorting algorithm of Scriptures. This is a bug.

=item Step 5. Loading of Menaion data

Steps 1, 3 and 4 are repeated for the Menaion-based data.

=item Step 6. The C<dRank> (rank of the day) is set.

Presently, C<dRank> is the max of all ranks available for a given day. As currently written, the API does not allow the user to select between service or commemoration alternatives. 

=item Step 7. The day's fasting information is computed.

=back

Initialization stops at this point and your Ponomar object is ready to work.

B<NOTE>: The initial initialization does not handle suppression or transfer of readings. This is handled by calling the C<executeCommands> method of relevant Service objects. See the documentation for Ponomar::Service for details.

=head1 The C<Ponomar> Class

=cut

use strict;
require 5.004;
require Carp;
use vars qw( $VERSION $parser @GLOBALS);
use XML::Parser;
use Ponomar::JDate;
use Ponomar::Saint;
use Ponomar::Util;
use Ponomar::I18n;
use Ponomar::Bible;

BEGIN {
	$VERSION = 0.01;
	### TODO: ADD EXPORTER STUFF HERE
	
	@GLOBALS = qw /dow doy nday Year GS dRank/;
}

INIT {
	require File::Basename; ## TODO: CHANGE THIS TO PERL 5 SYNTAX
	## LOADS INTERNATIONALIZATION SUPPORT FOR PONOMAR
	## TODO: GET RID OF Ponomar::I18n and switch to the new CLDR module for Perl
	Ponomar::I18n::load(File::Basename::dirname($INC{__PACKAGE__ . '.pm'}) . "/Ponomar/locales.yml");
}

END {
	Ponomar::I18n::unload();
}

use constant TRUE  => 1;
use constant FALSE => "";

our ($dow, $doy, $nday, $ndayP, $ndayF, $Year, $src, $dRank, $GS);

############################### LOCAL SUBS ######################################
sub default {
	return;
}

sub text {
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
	if ($attrs{Tone}) {
		my $tone = $attrs{Tone};
		foreach (@GLOBALS) {
			$tone =~ s/([^\$])$_/$1\$$_/g;
		}

		$attrs{Tone} = $self->{_tone} = int(eval($tone)) + 8 * (int(eval($tone)) == 0);
	}
	SWITCH: {
		if ($element eq "SAINT") {
			my $CId = $attrs{CId};
			my $menologion= defined $attrs{Src} ? $attrs{Src} : "";
			# create a new Saint object
			my $saint = Ponomar::Saint->new( CId => $CId, Src => $src, Date => $self->{_date}, Lang => $self->{_lang}, GS => $GS, SIds => $attrs{SId}, Menologion=> $menologion ); #ponomar => $self 
			push @{ $self->{_saints} }, $saint;
			last SWITCH;
		}
		if ($element eq "PERIOD") {
			$self->{_readPeriod} = 1;
			last SWITCH;
		}
		if ($element eq "RULE" && $self->{_readPeriod}) {
			$self->{_fast} = $attrs{Case};
			last SWITCH;
		}
	}; # switch
	
	return;
}

sub endElement {
	my ( $self, $parseinst, $element, %attrs ) = @_;

	if ($element eq "PERIOD") {
		$self->{_readPeriod} = 0;
	}
	return;
}
############################## END LOCAL SUBS ###################################

=head2 METHODS

=over 4

=item new($date, $language, [$GS] )

Creates a new Ponomar object for Julian date C<$date> and language C<$language>, using Gospel Selection algorithm $GS.
Runs the initial initialization process, reading XML for this C<$date>. Returns a reference to the new object. E.g.:

	$ponomar = new Ponomar(Ponomar::Util::getToday(), 'en')

The deprecated paramater C<$GS> determines if the Lucan Jump is used in the selection of scriptures.
As of August 2018, non-Lucan Jump implementations are no longer supported (C<$GS> is always treated as C<1> regardless of user input).

=cut

sub new {
	my $class = shift;
	my $date = shift;
	my $language = defined $_[0] ? $_[0] : 'en';
	my $lectionary_style = defined $_[1] ? $_[1] : 1;
# TODO: CLEAN UP THIS CODE
# WE SHOULD ALLOW THE USER TO PASS THE PARAMETERS IN ANY ORDER, AND WITH CLEARLY
# SPECIFIED DEFAULT VALUES.
	my $self = {
		_date => $date,
		_lang => $language,
		_GS => $lectionary_style
	};
	$GS = $lectionary_style;
	bless $self, $class;
	$self->init();
	return $self;
}

sub init {
	my $self = shift;
	my $date = $self->{_date};
	my $language = $self->{_lang};
	
	$parser = XML::Parser->new(
		Handlers => {
			Start   => sub { $self->startElement(@_) },
			End     => sub { $self->endElement(@_) },
			Char    => sub { $self->text(@_) },
			Default => sub { $self->default(@_) } 
		});

	##########
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
	
	#### FIGURE OUT WHERE WE ARE IN TERMS OF THE TRIODION / PENTECOSTARION CYCLE
	my $directory;
	my $filename;
	if ($nday >= -70 && $nday < 0) {
		$directory = "triodion";
		$filename  = abs($nday);
	} elsif ($nday < -70) {
		$directory = "pentecostarion";
		$filename  = $ndayP + 1;
	} else {
		$directory = "pentecostarion";
		$filename  = $nday + 1;
	}

	my $filepath = "xml/" . $directory . "/" . ($filename >= 10 ? $filename . ".xml" : "0" . $filename . ".xml");

	### TODO: PARSE BIBLE HERE?
	#### BEGIN BY PARSING THE PENTECOSTARION / TRIODION FILE
	local $src = "pentecostarion";
	eval {
		$parser->parsefile( findBottomUp($language, $filepath) );
	};
	Carp::croak(__PACKAGE__ . "::init() - Parsing error $@ in file " . findBottomUp($language, $filepath)) if ($@);
	
	#### NEXT, PARSE THE MENAION FILE
	$filepath = "xml/";
	$filepath .= $date->getMonth() < 10 ? "0" . $date->getMonth() : $date->getMonth();
	$filepath .= $date->getDay() < 10 ? "/0" . $date->getDay() : "/" . $date->getDay();
	$filepath .= ".xml";

	local $src = "menaion";
	eval {
		$parser->parsefile( findBottomUp($language, $filepath) );
	};
	Carp::croak(__PACKAGE__ . "::init() - Parsing error $@ in file " . findBottomUp($language, $filepath)) if ($@);

	## SET THE DRANK OF THE DAY FOR FASTING PURPOSES
	local $dRank = max (  map { $_->getKey("Type") } @{ $self->{_saints} });
	local $src   = "";
	
	## READ IN THE FASTING INSTRUCTIONS
	foreach my $file (findTopDown($language, "xml/Commands/Fasting.xml")) {
		eval {
			$parser->parsefile( $file );
		};
		Carp::croak(__PACKAGE__ . "::init() - Parsing error $@ in file " . $file) if ($@);
	}

	return 1;
}

=item getSaints( [$src] )

Returns the Array of Saint objects associated with the Ponomar object Optional parameter C<$src> can take on one of two values: 'pentecostarion' or 'menaion' and conditions the returned array on the Source of the commemoration.

Note that for the purposes of Source, Triodion-based commemorations are called 'pentecostarion', i.e., 'pentecostarion' refers to everything based off the Paschal cycle. E.g.:

	$ponomar->getSaints( 'pentecostarion' );

Returns those saints who came from the pentecostarion.

=cut

sub getSaints {
	my $self = shift;
	my $condition = shift;

	return defined $condition ?
		grep { $_->getKey("Src") eq $condition } @{ $self->{_saints} } :
		@{ $self->{_saints} };
}

=item getTone()

Returns the Tone of the day as a String, based on the locale of the object. E.g.: In C<en> locale, returns 'Tone VIII' 

=cut

sub getTone {
	my $self = shift;

	return Ponomar::I18n::getLocaleKey('tone' . $self->{_tone}, $self->{_lang});
}

=item getFastingInstructions()

Returns the Fasting instructions of the day as a String, basd on the locale of the object. E.g. In C<en> locale, returns 'Xerophagy'

=cut

sub getFastingInstructions {
	my $self = shift;
	
	return Ponomar::I18n::getLocaleKey('fast_' . $self->{_fast}, $self->{_lang});
}

=item getFastingCode()

Returns the raw code of the fasting instruction, e.g., 000001. 

E.g., the following test if meat is allowed on a given day:
 
C<< split(//, $ponomar->getFastingCode())[0] == 1 >>

=cut

sub getFastingCode {
	my $self = shift;
	return $self->{_fast};
}

=item getReadings( [$type, $Src] )

Returns an array with references to all Readings objects associated with the date

Optional parameters C<$type> and C<$Src>

C<$Src> limits returned array to source of commemoration (e.g., pentecostarion, menaion); C<$type> limits returned array to type of service (e.g., vespers, liturgy)

=cut

sub getReadings {
	my $self = shift;
	my $type = shift;
	my $Src  = shift;

	my @out = ();
	foreach my $saint ($self->getSaints($Src)) {
		next unless $saint->hasServices($type);
		foreach my $service ($saint->getServices($type)) {
			next unless $service->hasReadings();
			push @out, $service->getReadings();
		}
	}
	return @out;
	#UGH! TODO: REWRITE THIS CODE, THIS COULD BE DONE WITH ONE LINE OF PERL
	## SOMETHING ALONG THE LINES OF map { grep {} map { ... }}
}

=item loadBible([$version])

Loads the Bible in this language. Optional parameter C<$version> specifies which version of the Bible to load
if multiple versions are available in a language. If C<$version> is not specified, the default version is used.

You can get a list of available versions by calling C<getBibleVersions()>.

Returns a reference to a new C<Ponomar::Bible> object.

=cut

sub loadBible {
	my $self = shift;
	my $version = shift;
	
	return Ponomar::Bible->new( Lang => $self->{_lang}, Version => $version );
}

=item Constant: NO_LUCAN_JUMP

Deprecated.

=cut

sub NO_LUCAN_JUMP {
	Carp::carp('Constant NO_LUCAN_JUMP is deprecated.');
	return 0;
}

=item Constant: LUCAN_JUMP

Deprecated.

=cut

sub LUCAN_JUMP {
	Carp::carp('Constant LUCAN_JUMP is deprecated.');
	return TRUE;
}

=item setLectionaryStyle ( $style )

Deprecated.

=cut

sub setLectionaryStyle {
	my $self = shift;

	Carp::carp('Sub setLectionaryStyle() is deprecated.');
	return 0;
}

=item getLectionaryStyle()

Deprecated.

=back

=cut

sub getLectionaryStyle {
	my $self = shift;
	Carp::carp('Sub getLectionaryStyle() is deprecated.');
	return $self->{_GS} == TRUE;
}

1;

__END__
