#!/usr/bin/perl

use warnings;
use strict;
use utf8;
use POSIX qw(strftime);

## A REGRESSION SCRIPT USING THE PERL API
## THIS SCRIPT CONSTRUCTS A SIMPLE LECTIONARY WITH SEQUENTIAL LITURGY READINGS FOR EVERY DAY OF THE YEAR
## WITHOUT TAKING INTO CONSIDERATION THE LUCAN JUMP OR ANY OTHER TRANSFERS
## OUTPUT IS WRITTEN TO ../../regtests/simple_lectionary.txt
## 
## INSTRUCTIONS FOR REGRESSION TESTING: RUN THIS SCRIPT, THEN COMPARE
## /Ponomar/regtests/simple_lectionary.txt (the Script's output) with /Ponomar/regtests/simple_lectionary_baseline.txt
## IF THE TWO FILES ARE DIFFERENT (EXCEPT FOR THE TIMESTAMP LINE), YOU HAVE A REGRESSION
##
## YOU MUST ENSURE THAT THE REGRESSION WAS AN INTENDED CHANGE BEFORE COMITTING YOUR CODE TO SVN
##

use lib "../../APIs/Perl/";
use Ponomar;
use Ponomar::Util;

sub formatReading {
	my $reading = shift;

	my $passage = $reading->getReading();
	$passage =~ s/_/ /g;
	my $pericope = $reading->getPericope();
	return defined $pericope ? $passage . " (§$pericope)" : $passage;
}

my $start = getPascha(2013);
my $end = getPascha($start->getYear() + 1);
my $output = "../../regtests/simple_lectionary.txt";
my $baseline = "../../regtests/simple_lectionary_baseline.txt";
my @weekdays = qw/Sunday Monday Tuesday Wednesday Thursday Friday Saturday/;

open (OUTFILE, ">:encoding(UTF-8)", $output) || die "Cannot write to output file: $!";

print OUTFILE "Lectionary of sequential readings\n";
print OUTFILE "Generated ", strftime( "%a %b %e %H:%M:%S %Y", localtime) , "\n";


for (my $today = $start; $today->getDaysUntil($end) > 0; $today++) {
	my $ponomar = new Ponomar($today, 'en', 0);

	my $object = ($ponomar->getSaints('pentecostarion'))[0];
	next unless ($object->hasServices('liturgy'));
	# execute the liturgical commands for this day
	# this should be commented out if we are running regtests
	my $dRank = max (map { $_->getKey("Type") } $ponomar->getSaints('menaion'));
	foreach my $service ($object->getServices('liturgy')) {
		$service->execCommands($dRank);
	}

	if ($today->getDayOfWeek() == 0) {
		print OUTFILE "\n\n";
		print OUTFILE ($object->getKey('Name')->{Nominative} . "\n");
		print OUTFILE join("\t", map {  formatReading($_) } $ponomar->getReadings('liturgy', 'pentecostarion'));
		print OUTFILE "\n";
		next;
	} 
	if ($today->getDayOfWeek() == 1) {
		my $weekname = $object->getKey('Name')->{Nominative};
		$weekname =~ s/Monday of the //gi;
		print OUTFILE $weekname, "\n";
	}
	print OUTFILE $weekdays[$today->getDayOfWeek()] . ":\t";	
	print OUTFILE join("\t", map {  formatReading($_) } $ponomar->getReadings('liturgy', 'pentecostarion'));
	print OUTFILE "\n";
}

close OUTFILE;
print "Regression results have been written to $output\n";
print "Use diff $output $baseline to check changes\n";
print "See README for additional information\n";
exit;

