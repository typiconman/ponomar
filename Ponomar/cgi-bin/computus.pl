#!/usr/bin/perl

use strict;
use Tie::IxHash;
use XML::Parser;
use lib "/home/sasha/Design/cgi-bin";
use JDate;

############
## THIS SCRIPT CREATES A COMPUTUS -- A LISTING OF READINGS FOR THE ENTIRE GREAT INDICTION CYCLE
my @GLOBALS = qw /dow doy nday Year GS LS Tone dRank/; # ndayP and ndayF will automatically get recoded!!
my $dow;	# today's day of week
my $doy;	# Day-of-year
my $nday;	# Number of days before or after this year's Pascha
my $ndayP;	# Number of days after last year's Pascha
my $ndayF;	# Number of days before next year's Pascha
my $Year;	# This year
my $dRank = 0;	# the Rank of the service -- see below:
my $language = "en";
my $GS = 1;

## THIS IS THE MAIN FILE PATH
my $basepath = "/home/sasha/svn/ponomar/Ponomar/languages/";

### THIS STORES ALL OF THE DATA FROM THE XML FILES
tie my %SAINTS, "Tie::IxHash";
tie my %READINGS, "Tie::IxHash";
my %COMMANDS = ();
my @DEBUG = ();
my $whichService;
my $src = ""; # THIS IS THE SOURCE OF THE DATA

sub max {
	my $max = shift;
	for ( @_ ) { $max = $_ if $max < $_; }
	return $max;
}

sub default {
	return;
}

sub text {
	return;
}

sub startElement {
	my( $parseinst, $element, %attrs ) = @_;
	
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
		if ($element eq "SAINT") {
			# INITIAL COMMEMORATION INFORMATION
			# CREATE NEW ENTRY IN SAINTS HASH WITH THE KEY OF CID
			# PUSH ALL OTHER RELEVANT KEYS TO THIS HASH
			my $CId = $attrs{CId};
			delete $attrs{CId};
			%{ $SAINTS{$CId}} = (%attrs, "Reason" => $src);
			last SWITCH;
		} 
		if ($element eq "NAME") {
			# src is a CID or SID
			# PUSH ALL INFORMATION INTO THIS HASH
			last SWITCH;
		}
		if ($element eq "SCRIPTURE") {
			# again src is a CId or SId
			# PUSH ALL INFORMATION INTO READINGS HASH
			die "Invalid identifier $src " unless exists $SAINTS{$src};
			# CREATE (OR REPLACE) AN ENTRY IN READINGS SUBHASH
			# WHERE CId (SId) is the Key
			# E.G., $READINGS{123}{apostol}{Reading}
			# and $READINGS{123}{apostol}{Pericope}
			my $type = $attrs{Type};
			delete @attrs{ qw(Cmd Type) }; # don't need this if we've gotten here
			unless ($whichService) {
				## orphan?
				last SWITCH;
			}
			@{ $READINGS{$src}{$whichService}{$type} }{keys %attrs} = values %attrs;
			# (XXX) THE ABOVE SHOULD ALLOW FOR LOCALE-LEVEL INHERITANCE,
			# EVEN OF THINGS LIKE PERICOPE NUMBERS, IF NECESSARY, BUT NOT TESTED
			last SWITCH;
		}
		if ($element eq "SERVICE") {
			die "Invalid identifier $src " unless exists $SAINTS{$src};
			$SAINTS{$src}{Type} = $attrs{Type}; 
			last SWITCH;
		}
		if ($element eq "PRIMES") {
			$whichService = "1st hour";
			last SWITCH;
		}
		if ($element eq "TERCE") {
			$whichService = "3rd hour";
			last SWITCH;
		}
		if ($element eq "SEXTE") {
			$whichService = "6th hour";
			last SWITCH;
		}
		if ($element eq "NONE") {
			$whichService = "9th hour";
			last SWITCH;
		}
		if ($element eq "VESPERS") {
			$whichService = "vespers";
			last SWITCH;
		}
		if ($element eq "MATINS") {
			$whichService = "matins";
			last SWITCH;
		}
		if ($element eq "LITURGY") {
			$whichService = "liturgy";
			last SWITCH;
		}
		if ($element eq "COMMAND") {
			my $i = max keys %COMMANDS;
			@{ $COMMANDS{++$i} }{keys %attrs} = values %attrs;
			last SWITCH;
		}
	};
}

sub endElement {
	my ($parseinst, $element, %attrs) = @_;
	
	if ($element eq "VESPERS" || $element eq "MATINS" || $element eq "LITURGY" || $element eq "SERVICE") {
		$whichService = "";
	} 
	return;
}

sub findBottomUp {
	my ($language, $file) = @_;
	
	###########################
	### THIS ALGORITHM IMPLEMENTS BOTTOM-UP READING OF FILES IN THE XML PATH
	### THE FULL IMPLEMENTATION IS DESCRIBED BY YURI IN "A Description", p. 27
	###
	### Basically, we begin with basepath/<language>/<script>/<locale>/file
	### and go until basepath/file
	### Stopping at the first occurance of file, which is then read
	###
	### PARAMETERS PASSED TO HERE: $language is e.g. cu/ru or zh/Hans
	### $file is e.g., xml/01/01.xml or xml/pentecostarion/01.xml
	###########################
	
	# we have a path like language: cu/ru or zh/Hans
	# file: xml/01/01.xml
	my @parts = split (/\//, $language);
	for (my $j = $#parts; $j >= 0; $j--) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/$file";
		return $path if (-e $path);
	}

	return $basepath . $file if (-e $basepath . $file);
	die "Unable to find file $file in the bottom-up path for language $language";
}

sub findTopDown {
	my ($language, $file) = @_;
	
	###########################
	### THIS ALGORITHM IMPLEMENTS THE TOP-DOWN APPROACH FOR READING FILES
	### DESCRIBED BY YURI IN op. cit., p. 28
	###
	### WE CREATE AN ARRAY OF ALL EXTANT FILES NAMED $file IN ALL PATHS
	### BEGINNING WITH BASEPATH
	### AND UP TO $basepath/<language>/<script>/<locale>/file
	### PARAMETERS: SAME AS ABOVE
	############################
	
	my @paths = ();
	push @paths, $basepath . $file if (-e $basepath . $file);
	my @parts = split(/\//, $language);
	for (my $j = 0; $j < @parts; $j++) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/" . $file;
		push @paths, $path if (-e $path);
	}
	warn "Unable to find any instances of $file in the path for $language" unless (@paths);
	return @paths;
}

sub getPascha # returns the date of Pascha in any year
{
	my $inyear = shift;
	#Use the Gaussian formulae to calculate the Alexandria Paschallion
	my $a = $inyear % 4;
	my $b = $inyear % 7;
	my $c = $inyear % 19;
	my $d = (19 * $c + 15) % 30;
	my $e = (2 * $a + 4 * $b - $d + 34) % 7;
	my $f = int(($d + $e + 114) / 31); #Month of pascha e.g. march=3
	my $g = (($d + $e + 114) % 31) + 1; #Day of pascha in the month
	return JDate->new($f, $g, $inyear);
}

my $start = 1901;
my $end   = 2433;

my $PARSER = new XML::Parser(ErrorContext => 2);
$PARSER->setHandlers(	Start   => \&startElement,
			End     => \&endElement,
			Char    => \&text,
			Default => \&default);

open (OUTFILE, ">readings_dump.tsv") || die "Cannot write to file: $!";
print OUTFILE "year\tpascha\t";
print OUTFILE join("\t", 1..37);
print OUTFILE "\n";

for (my $year = $start; $year <= $end; $year++) {
	my $pascha = getPascha($year);
	
	my $lastpascha = getPascha($year - 1);
	my $nextpascha = getPascha($year + 1);
	
	print OUTFILE $year . "\t";
	print OUTFILE $pascha->toString() . "\t";
	
	for (my $i = 56; $i < 315; $i += 7) {
		my $today = $pascha->addDays($i);

		untie %SAINTS;
		foreach (keys %SAINTS) {
			delete $SAINTS{$_};
		}
		tie %SAINTS, "Tie::IxHash";
		
		untie %READINGS;
		foreach (keys %READINGS) {
			delete $READINGS{$_};
		}
		tie %READINGS, "Tie::IxHash";
		
		foreach (keys %COMMANDS) {
			delete $COMMANDS{$_};
		}
		
		## COMPUTE THE GLOBALs
		$dow = $today->getDayOfWeek();
		die "Fatal error; will not implode" unless $dow == 0;
		
		$doy = $today->getDoy();
		$nday = JDate->difference($today, $pascha);
		$ndayP = JDate->difference($today, $lastpascha);
		$ndayF = JDate->difference($today, $nextpascha);
		$Year  = $today->getYear();
		
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
		
		#### BEGIN BY PARSING THE PENTECOSTARION / TRIODION FILE
		$src = "pentecostarion";
		push @DEBUG, findBottomUp($language, $filepath);
		$PARSER->parsefile( findBottomUp($language, $filepath) );
		
		#### WE NOW HAVE A SET OF CIDs, which we are ready to Parse
		$src = "";
		foreach my $CId (keys %SAINTS) {
			$src = $CId;
			foreach my $file (findTopDown($language, "xml/lives/$CId.xml")) {
				push @DEBUG, $file;
				$PARSER->parsefile( $file);
			}
		}

#		## IMPLEMENT SUPRESSION AND MOVEMENT OF READINGS
#		# 1. LOAD SUPPRESSION INSTRUCTIONS INTO A HASH
#		foreach ( findTopDown($language, "xml/Commands/DivineLiturgy.xml") ) {
#			$PARSER->parsefile( $_ );
#		}
#		# 2. DECIDE IF TODAY'S READINGS ARE SUPPRESSED
#		foreach ( keys %COMMANDS ) {
#			## RECALL THAT IN PERL, VARS MUST START WITH $
#			my $cmd = $COMMANDS{$_}{Value};
#			foreach (@GLOBALS) {
#				$cmd =~ s/$_/\$$_/g;
#			}
#			next unless eval $cmd;
#			## IF WE GOT HERE, THEN THE READING IS TO BE SUPPRESSED
#			## REMEMBER, THE SUPPRESSION REFERS ONLY TO PENTECOSTARION READINGS
#			## AND MAY BE CAUSED BY THE PRESENCE OF A MENAION-BASED COMMEMORATION
#			## XXX: FOR EXAMPLE, THE dRank OF ASCENSION IS 7, BUT THE READING IS NOT SUPPRESSED
#			## WE HANDLE THIS BY ENSURING THAT THE SOURCE OF THE READING IS AN UNRANKED COMMEMORATION
#			## ALSO, NOTE THAT WE MAY HAVE MULTIPLE-SOURCE RANKED COMMEMORATIONS ON A GIVEN DAY
#			## E.G., ASCENSION + STS CYRIL AND METHODIUS, OR MID-PENTECOST + ST JOHN
#			## BUT IN THESE CASES, WE DO **NOT** SUPPRESS THE ``DAILY'' READINGS
#			foreach my $source (keys %READINGS) {
#				next unless $READINGS{$source}{liturgy};
#				next unless $SAINTS{$source}{Reason} eq "pentecostarion";
#				delete $READINGS{$source}{liturgy} unless ($SAINTS{$source}{Type} >= 1);
#			}
#		}

		## GET THE EFFWEEK PARAMETER OF THE GOSPEL READING (IF ANY)
		## AND PRINT IT
		my $type = "liturgy";
		my $src  = "pentecostarion";
		foreach my $CID (keys %SAINTS) {
			#next unless $SAINTS{$CID}{Reason} eq $src;
			
			foreach (keys %{ $READINGS{$CID}{$type} }) {
				next unless $_ eq "gospel";
				print OUTFILE $READINGS{$CID}{$type}{$_}{EffWeek};
			#	print OUTFILE $today->toString();
			#	print OUTFILE $doy;
			}
		}
		print OUTFILE "\t";
		
	}
	print OUTFILE "\n";
}

close OUTFILE;

