#!/usr/bin/perl

use warnings;
use strict;
use utf8;

## A REGRESSION SCRIPT USING THE PERL API
## THIS SCRIPT CONSTRUCTS A PASCHALION TABLE FOR 532 YEARS BEGINNING IN 1941 AD
## THE PURPOSE OF THIS TEST IS TO ENSURE THAT JDATE OBJECTS AND UNICODE ARE WORKING
## AND THAT THE ENVIRONMENT IS SANE

use lib "Ponomar/APIs/Perl/";
use Ponomar;
use Ponomar::Util qw(getPascha getIndiction getSolarCycle getConcurrent getLunarCycle getFoundation getEpacta);
use Ponomar::JDate;

my $outfile = "/tmp/paschalion.tsv";
my $basefile = "Ponomar/scripts/Perl/data/paschalion_baseline.tsv";

# do not edit below this line!
print "Test of paschalion for the 15th Indiction...";
my $start = 1941; # beginning of the 15th Indiction
my @letters = ("А", "Б", "В", "Г", "Д", "Е", "Ж", "Ѕ", "З", "И", "І", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "Ꙋ", "Ф", "Х", "Ѿ", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Ѣ", "Ю", "Ѫ", "Ѧ");
my @months = qw/January February March April May June July August September October November December/;

open (OUT, ">:encoding(UTF-8)", $outfile) || die "Cannot write to file: $!";
print OUT "Paschalion for 532 years beginning with $start\n";
# print OUT "Generated ", strftime( "%a %b %e %H:%M:%S %Y", localtime) , "\n";

for (my $year = $start; $year < $start + 532; $year++) {
	my $amundi = $year + 5508;
	my $indiction = getIndiction($year);
	my $solarCycle = getSolarCycle($year);
	my $concurrent = getConcurrent($year);
	my $lunarCycle = getLunarCycle($year);
	my $foundation = getFoundation($year);
	my $epacta = getEpacta($year);

	my $pascha = getPascha($year);

	# PERFORM SOME SANITY CHECKS. THESE ENSURE THAT OUR COMPUTUS IS CORRECT
	unless($epacta + $foundation == 21 || $epacta + $foundation == 51) {
		print "FATAL ERROR:\n\t Epacta and Foundation in $year incorrectly added up to " . ($epacta + $foundation);
		exit 1;
	}
	# THE CODE BELOW COMPUTES PASCHA FROM THE CONCURRENT AND FOUNDATION
	# SEE HERE FOR EXPLANATION: http://www.magister.msk.ru/library/bible/comment/nkss/nkss22.htm
	my $first_Sunday_of_March = (new Ponomar::JDate(3, 1, $year))->addDays((10 - $concurrent) % 7);
	my $paschal_boundary = (new Ponomar::JDate(3, 1, $year))->addDays(44 - $foundation);
	# add the Nicene correction for Indiction 14 (3 days)
	$paschal_boundary = $paschal_boundary->addDays(3);
	# check that the full moon is after March 21; if it is not, add another 30 days.
	$paschal_boundary = $paschal_boundary->addDays(30) if ($paschal_boundary->getDaysSince(new Ponomar::JDate(3, 21, $year)) < 0);

	my $computed_pascha = $first_Sunday_of_March;
	while ($computed_pascha->getDaysSince($paschal_boundary) < 0) {
		$computed_pascha = $computed_pascha->addDays(7);
	}
	unless ($pascha == $computed_pascha) {
		print "FATAL ERROR:\n\t Computation of Pascha using formulae does not match the tabular computation for $year";
		exit 1;
	}
	
	my $keyb = $letters[$pascha->getDaysSince(new Ponomar::JDate(3, 22, $year))];

	print OUT join("\t", $year, $amundi, $indiction, $solarCycle, $concurrent, $lunarCycle, $foundation, $epacta);
	print OUT "\t";
	print OUT $months[$pascha->getMonth() - 1] . " " . $pascha->getDay() . "\t";
	print OUT "$keyb\n";
}

close OUT;

# now run the regression test
open (BASELINE, "<:encoding(UTF-8)", $basefile) || die "Cannot read from baseline file: $!";
my @baseline = <BASELINE>;
close (BASELINE);
open (GENED, "<:encoding(UTF-8)", $outfile) || die "Cannot read from generated file: $!";
my @gened = <GENED>;
close (GENED);
my @errors = ();
for (my $line = 0; $line < scalar (@baseline); $line++) {
    if ($baseline[$line] ne $gened[$line]) {
        push (@errors, "Error: line " . ($line + 1));
    }
}

unless (@errors) {
    print "PASSED", "\n";
    unlink "$outfile";
	exit 0;
} else {
    print "FAILED", "\n";
    open (ERROR, ">> /tmp/errors.txt") || die "Cannot write to error file: $!";
    print ERROR "In file paschalion.tsv:", "\n";
    foreach (@errors) {
        print ERROR $_, "\n";
    }
    close (ERROR);
	print "Failures printed to /tmp/errors.txt", "\n";
	exit 1;
}