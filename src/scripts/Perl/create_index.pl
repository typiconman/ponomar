#!/usr/bin/perl

use warnings;
use strict;
use utf8;
use lib "../../APIs/Perl/";
use Ponomar;
use Ponomar::JDate;
use Ponomar::Util qw(getToday);
use Tie::IxHash;

# create_index.pl :: This creates an index of saints
my @months = qw/January February March April May June July August September October November December/;
my $y = getToday()->getYear();
my $outpath = "../../regtests/index.html";
my %hash = ();
my $begin = new Ponomar::JDate(9, 1, $y);
use constant NUMBER_OF_DAYS => 365; # change this to go beyond September 1.

open (OUTFILE, ">:encoding(UTF-8)", $outpath) || die "Cannot write to file: $!";
print OUTFILE qq(
<HTML>
<HEAD>
<TITLE>Index of Saints</TITLE>
<META Http-equiv="content-type" Content="text/html; charset=utf-8">
</HEAD>
<BODY>
);

for (my $date = $begin; $date->getDaysSince($begin) < NUMBER_OF_DAYS; $date++) {
	my $ponomar = new Ponomar($date, 'en', 0);
	my @saints = $ponomar->getSaints('menaion');

	foreach my $saint (@saints) {
		next unless defined $saint->getKey('Name');

		if (defined $saint->getKey('SIds') && $saint->getKey('SIds') ne $saint->getKey('CId')) {
			# we are dealing with a commemoration, not a saint
			# temporarily ignore the speical cases
			next if ($saint->getKey('SIds') =~ /^\d+$/ && $saint->getKey('SIds') < 10);

			my @SIds = split(/,/, $saint->getKey('SIds'));
			foreach my $SId (@SIds) {
				push @saints, Ponomar::Saint->new( CId => $SId, Src => 'menaion', Date => $date, Lang => 'en', GS => 0, SIds => $SId, Menologion=> $saint->getKey('Menologion') );
			}
			next;
		}

		$hash{$saint->getKey('CId')}{'Name'} = $saint->getKey('Name')->{'Short'};
		$hash{$saint->getKey('CId')}{'FullName'} = $saint->getKey('Name')->{'Nominative'};
		$hash{$saint->getKey('CId')}{'Source'} = $saint->getKey('Menologion') if (defined $saint->getKey('Menologion'));
		push @{ $hash{$saint->getKey('CId')}{'Dates'} }, $date;
	}
}


# now sort the hash alphabetically
print OUTFILE "<TABLE>\n";

my %names = map { $hash{$_}{'Name'} => 1 } keys %hash;

foreach my $name (sort { $a cmp $b } keys %names) {
	print OUTFILE "<TR><TD ColSpan=\"2\">$name</TD></TR>\n";

	my @saints_with_name = grep { $hash{$_}{'Name'} eq $name } keys %hash;
	foreach (@saints_with_name) {
		print OUTFILE "<TR><TD>";
		print OUTFILE qq(<A Href="http://www.ponomar.net/cgi-bin/lives.cgi?id=$_">);
		print OUTFILE $hash{$_}{'FullName'} . "</A></TD><TD>";
		foreach my $date (@{ $hash{$_}{'Dates'} }) {
			my $m = $date->getMonth();
			my $d = $date->getDay();
			print OUTFILE qq(<A Href="http://www.ponomar.net/cgi-bin/menologion.cgi?month=$m&day=$d&year=$y">);
			print OUTFILE $months[$m - 1] . " " . $d . "</A> ";
		}
	}
	print OUTFILE "</TD></TR>\n";
}

print OUTFILE qq(
</TABLE>
</BODY>
</HTML>);
close OUTFILE;

exit;

