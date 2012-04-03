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
	$basepath = "/home/sasha/svn/ponomar/Ponomar/languages/";
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

=back

=cut

sub getMatinsGospel {
	my $reading = shift;

	return $matinsGospels{$reading};
}

1;

__END__

