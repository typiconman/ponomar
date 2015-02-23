#!/usr/bin/perl

use warnings;
use strict;
use utf8;
use POSIX qw(strftime);

## A REGRESSION SCRIPT USING THE PERL API
## THIS SCRIPT CONSTRUCTS A PASCHALION TABLE FOR 532 YEARS BEGINNING IN 2000 AD
## OUTPUT IS WRITTEN TO ../../regtests/paschalion.txt
## 
## INSTRUCTIONS FOR REGRESSION TESTING: RUN THIS SCRIPT, THEN COMPARE
## /Ponomar/regtests/paschalion.txt (the Script's output) with /Ponomar/regtests/paschalion_baseline.txt
## IF THE TWO FILES ARE DIFFERENT (EXCEPT FOR THE TIMESTAMP LINE), YOU HAVE A REGRESSION
##
## THE PURPOSE OF THIS TEST IS TO ENSURE THAT JDATE OBJECTS AND UNICODE ARE WORKING
## AND THAT THE ENVIRONMENT IS SANE
##

use lib "../../APIs/Perl/";
use Ponomar;
use Ponomar::Util qw(getPascha getIndiction getSolarCycle getConcurrent getLunarCycle getFoundation getEpacta);
use Ponomar::JDate;

my $start = 1941; # beginning of the 15th Indiction
my @letters = ("А", "Б", "В", "Г", "Д", "Е", "Ж", "Ѕ", "З", "И", "І", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "Ꙋ", "Ф", "Х", "Ѿ", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Ѣ", "Ю", "Ѫ", "Ѧ");
my @months = qw/January February March April May June July August September October November December/;
my $outfile = "../../regtests/paschalion.txt";
my $basefile = "../../regtests/paschalion_baseline.txt";

open (OUT, ">:encoding(UTF-8)", $outfile) || die "Cannot write to file: $!";
print OUT "Paschalion for 532 years beginning with $start\n";
print OUT "Generated ", strftime( "%a %b %e %H:%M:%S %Y", localtime) , "\n";

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
		print OUT "FATAL ERROR: Epact and Foundation incorrectly added up to " . ($epacta + $foundation);
		next;
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
		print OUT "FATAL: computation of Pascha using formulae does not match the tabular computation";
		next;
	}
	
	my $keyb = $letters[$pascha->getDaysSince(new Ponomar::JDate(3, 22, $year))];

	print OUT join("\t", $year, $amundi, $indiction, $solarCycle, $concurrent, $lunarCycle, $foundation, $epacta);
	print OUT "\t";
	print OUT $months[$pascha->getMonth() - 1] . " " . $pascha->getDay() . "\t";
	print OUT "$keyb\n";
}

close OUT;
print "Regression results have been written to $outfile\n";
print "Use diff $outfile $basefile to check changes\n";
print "See README for additional information\n";
exit;

