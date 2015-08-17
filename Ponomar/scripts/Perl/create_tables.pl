#!/usr/bin/perl

use warnings;
use strict;
use utf8;
use lib "../../APIs/Perl/";
use Ponomar;
use Ponomar::JDate;
use Tie::IxHash;

# create_tables.pl :: This creates a table (in TSV -- tab-separated values) of the menaion data for the meanion
# this can be used for simple localization.
my $outpath = "../../regtests/saintdata.tsv";
my $begin = new Ponomar::JDate(9, 1, 2015);
use constant NUMBER_OF_DAYS => 1; # change this to go beyond September 1.

open (OUTFILE, ">:encoding(UTF-8)", $outpath) || die "Cannot write to output file: $!";
print OUTFILE "ID\tTag\tKey\tValue\n";

for (my $date = $begin; $date->getDaysSince($begin) < NUMBER_OF_DAYS; $date++) {
	my $ponomar = new Ponomar($date, 'en', 0);
	my @saints = $ponomar->getSaints('menaion');

	foreach my $saint (@saints) {
		next unless (defined $saint->getKey('Name'));
		if (defined $saint->getKey('SIds') && $saint->getKey('SIds') ne $saint->getKey('CId')) {
			# ignore reserved SIds (see documentation)
			goto LABEL if ($saint->getKey('SIds') =~ /^\d+$/ && $saint->getKey('SIds') < 10);
			# this commemoration consists of multiple saints.
			my @SIds = split(/,/, $saint->getKey('SIds'));
			foreach my $SId (@SIds) {
				push @saints, Ponomar::Saint->new( CId => $SId, Src => 'menaion', Date => $date, Lang => 'en', GS => 0, SIds => $SId, Menologion=> undef );
				# UGH. Crazy code with a for loop on a changing array. ...
			}
		}
LABEL:
		foreach (sort keys %{ $saint->getKey('Name') }) {
			print OUTFILE $saint->getKey('CId') . "\t" . "NAME\t" . $_ . "\t" . $saint->getKey('Name')->{$_} . "\n";
		}

		next unless (defined $saint->getKey('Info'));
		foreach (sort keys %{ $saint->getKey('Info') }) {
			print OUTFILE $saint->getKey('CId') . "\t" . "INFO\t" . $_ . "\t" . $saint->getKey('Info')->{$_} . "\n";
		}

	}
}

close OUTFILE;

exit;

