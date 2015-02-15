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

my $start = 2000;
my @letters = ("А", "Б", "В", "Г", "Д", "Е", "Ж", "Ѕ", "З", "И", "І", "К", "Л", "М", "Н", "О", "П", "Р", "С", "Т", "Ꙋ", "Ф", "Х", "Ѿ", "Ц", "Ч", "Ш", "Щ", "Ъ", "Ы", "Ь", "Ѣ", "Ю", "Ѫ", "Ѧ");
my @months = qw/January February March April May June July August September October November December/;
my $outfile = "../../regtests/paschalion.txt";
my $basefile = "../../regtests/paschalion_baseline.txt";

open (OUT, ">:encoding(UTF-8)", $outfile) || die "Cannot write to file: $!";
print OUT "Paschalion for 532 years beginning with $start\n";
print OUT "Generated ", strftime( "%a %b %e %H:%M:%S %Y", localtime) , "\n";

for (my $year = $start; $year < $start + 533; $year++) {
	my $amundi = $year + 5508;
	my $indiction = getIndiction($year);
	my $solarCycle = getSolarCycle($year);
	my $concurrent = getConcurrent($year);
	my $lunarCycle = getLunarCycle($year);
	my $foundation = getFoundation($year);
	my $epacta = getEpacta($year);

	my $pascha = getPascha($year);

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

