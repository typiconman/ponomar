#!/usr/bin/perl

use warnings;
use strict;
use utf8;
use lib "../../APIs/Perl/";
use Ponomar;
use Ponomar::JDate;
use Tie::IxHash;

# typicon_menologion.cgi :: THIS SCRIPT CREATES MENOLOGION DATA FOR THE ENGLISH TYPICON
# usage: typicon_menologion.pl month day
# This script is part of the Ponomar software and is distributed under the same license as Ponomar itself.
my $outpath = "../../regtests/menaion_entry.tex";

my $month = $ARGV[0];
my $day = $ARGV[1];
unless (defined $month && $month >= 1 && $month <= 12 && $month == int($month) ||
	defined $day && $day >= 1 && $day <= 31 && $day == int($day) ) {
	print "Usage: typicon_menologion.pl month day\n";
	exit;
}


# ok, we're dealing with several menologion sources
tie my %sources, "Tie::IxHash";
%sources = (
	"" => "General",
	"el" => "Greek sources",
	"ru" => "Russian sources",
	"la" => "Roman sources"
);
# define the Typicon symbols
my @typicon = ("", qw/⹇ 🕃 🕃 🕂 🕁 🕀 🕀 🕀/);

my $date = new Ponomar::JDate($month, $day, 2015);
my $ponomar = new Ponomar($date, 'en', 1);

open (OUTFILE, ">:encoding(UTF-8)", $outpath) || die "Cannot write to outfile: $!";
print OUTFILE <<HEADING;
\\documentclass[12pt]{article}
\\usepackage{xltxtra,xcolor}
\\newfontfamily{\\slv}[Scale=1.0]{Ponomar Unicode}
\\setmainfont[Mapping=tex-text]{Liberation Serif}
\\begin{document}

HEADING

my @saints = $ponomar->getSaints('menaion');
foreach my $src (keys %sources) {
	# get the saints in the menaion that come from this source
	print OUTFILE "\\textbf{" . $sources{$src} . "}: " if (length($src));
	my $idx = 0;
	foreach my $saint (@saints) {
		next unless ($saint->getKey('Menologion') eq $src);
		next unless (defined $saint->getKey('Name'));
		print OUTFILE "; " if ($idx);

		my $name = $saint->getKey('Name')->{Nominative};
		my $type = $saint->getKey('Type');
		my $century = "";
		my $place = "";
		my $note = "";
		if (defined $saint->getKey('Info')) {
			$century = defined $saint->getKey('Info')->{'ReposeDate'} ?
				$saint->getKey('Info')->{'ReposeDate'}	:
			defined $saint->getKey('Info')->{'ReposeCentury'} ?
				$saint->getKey('Info')->{'ReposeCentury'} : "";
			if (length $century > 1) {
				$century = "†" . $century;
			} else {
				$century = defined $saint->getKey('Info')->{'Year'} ?
					$saint->getKey('Info')->{'Year'} : "";
			}

		$place = defined $saint->getKey('Info')->{'ReposePlace'} ? 
			$saint->getKey('Info')->{'ReposePlace'}  : "";
		$note = defined $saint->getKey('Info')->{'Note'} ? 
			$saint->getKey('Info')->{'Note'} : "";
		}

		my $rank = "";
		if ($type) {
			$rank = $type == -2 ? "⧟" : $typicon[$type];
		}

		if (length($rank) > 0) {
			print OUTFILE $type >= 3 ? "\\textcolor{red}{\\slv $rank} " : "{\\slv $rank} ";
		}

		print OUTFILE (defined $type && $type >= 4) ? "\\textcolor{red}{$name}" : "$name";
		print OUTFILE (defined $note && length($note)) ? 
			"\\footnote{" . ucfirst($note) . "}" : "";
		print OUTFILE (defined $place && length $place > 0 && defined $century && length $century > 0) ?
			 " ($place, $century)" :
			(defined $century && length $century > 0) ? " ($century)" : "";
		$idx++;
	}
	print OUTFILE "\n\n";
}

print OUTFILE "\\end{document}\n";
exit;
