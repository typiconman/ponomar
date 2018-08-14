#!/usr/bin/perl

use warnings;
use strict;
use utf8;

#######################################################################################
# KAHUNA :: AN ALGORITHM FOR COMPUTING EPISTLE AND GOSPEL LESSONS AT LITURGY         ##
# ACCORDING TO THE JULIAN CALENDAR IN PERPETUITY.                                    ##
# THIS ALGORITH READS A BUNCH OF DATA FILES FORMATTED IN THE FOLLOWING MANNER:       ##
# ;; COMMENTS                                                                        ##
# month day doy nday ndayF effWeekAp effWeekGosp                                     ##
# CREATES A GIGANTIC MATRIX                                                          ##
# THEN OUTPUTS XML FILES FOR EACH NDAY                                               ##
# WITH CMDs FOR EACH DOY AND NDAYF                                                   ##
#                                                                                    ##
# FOR INFORMATION ON HOW THE LUCAN JUMP IS IMPLEMENTED, SEE THE FOLLOWING PAPER:     ##
#                                                                                    ##
# Aндреев, А. А. О порядке чтения Евангелия за Божественной литургией в свете        ##
#   рекомендаций Поместного собора 1917-18 гг. // Вестник Екатеринбургской духовной  ##
#   семинарии. 2018.                                                                 ##
# doi:                                                                               ##
#######################################################################################

my $input_path = "data"; ## this is the path to the data TSV files
my $output_path = "../../languages/xml/lives"; ## this is the path to the XML data
use constant UPPER => 65; ## this is the maximal number of TSV files

# DO NOT EDIT BELOW THIS LINE!!

sub stringify {
    # takes an array and returns a string with a range that gives this array
    # e.g., stringify ( [1 2 3 5 6]) => (var >= 1 && var <= 3) || (var >= 5 && var <= 6)
    my @array = sort { $a <=> $b } @_;
    my @starts = ();
    my @stops = ();

    push @starts, $array[0];
    for (my $i = 0; $i < scalar(@array) - 1; $i++) {
        if ($array[$i] + 1 != $array[$i + 1]) {
            push @stops, $array[$i];
            push @starts, $array[$i + 1];
        }
    }
    push @stops, $array[-1];

    my @components;
    for (my $j = 0; $j < scalar(@starts); $j++) {
        if ($starts[$j] == $stops[$j]) {
            push @components, "doy == $starts[$j]";
        } else {
            push @components, "(doy >= $starts[$j] && doy <= $stops[$j])";
        }
    }
    return join(' || ', @components);
}

sub xmlEscape {
    my $data = shift;

    $data =~ s/&/&amp;/sg;
    $data =~ s/</&lt;/sg;
    $data =~ s/>/&gt;/sg;
    $data =~ s/"/&quot;/sg;
    return $data;
}

my %gigantic_matrix = ();

for (my $i = 1; $i <= UPPER; $i++) {
    open (INPUT, "<:encoding(UTF-8)", "$input_path/$i.tsv") || die "Cannot read from file: $!";
    while (<INPUT>) {
        s/\r?\n//g;
        next if (substr($_, 0, 2) eq ";;");
        my @vals = split(/\t/, $_);
        next unless (defined $vals[5] && defined $vals[6]);
        next if (length($vals[5]) < 1 || length($vals[6]) < 1); # skip empty slots
        # for some reason the nday values in the data files are off by one day
        $gigantic_matrix{$vals[3] + 1}{$vals[2]}{$vals[4]} = { Apostol => $vals[5], Gospel => $vals[6] };
    }
    close (INPUT);

}

# read in the lectionary file
my %lectionary = ();
open (INPUT, "<:encoding(UTF-8)", "$input_path/lectionary.tsv") || die "Cannot read from file: $!";
while (<INPUT>) {
    s/\r?\n//g;
    next if (substr($_, 0, 2) eq ";;");
    my @vals = split(/\t/, $_);
    # format is effweek dow epistleReading epistlePericope gospelReading gospelPericope
    next unless (defined $vals[2] && defined $vals[4]);
    next if (length($vals[2]) < 1 || length($vals[4]) < 1); # skip empty slots
    $lectionary{$vals[0]}{$vals[1]} = { epistleReading => $vals[2], epistlePericope => $vals[3], gospelReading => $vals[4], gospelPericope => $vals[5] };
}
close (INPUT);

# now flatten the gigantic matrix
foreach my $nday (keys %gigantic_matrix) {
    my %epistles = ();
    my %gospels = ();
    foreach my $doy (keys %{ $gigantic_matrix{$nday}}) {
        foreach my $ndayF (keys %{ $gigantic_matrix{$nday}{$doy}}) {
            push ( @{ $epistles{$gigantic_matrix{$nday}{$doy}{$ndayF}{Apostol}} }, { doy => $doy, ndayF => $ndayF });
            push ( @{ $gospels{$gigantic_matrix{$nday}{$doy}{$ndayF}{Gospel}} }, { doy => $doy, ndayF => $ndayF });
        }
    }

    # now we can print the commands
    my $outfile = '9' . $nday;
    my $dow = ($nday - 1) % 7;
    open (FORMER, "<:encoding(UTF-8)", "$output_path/$outfile.xml") || die "Cannot read from file: $!";
    open (OUTPUT, ">:encoding(UTF-8)", "$output_path/$outfile.bak") || die "Cannot write to file: $!";
    my $isLiturgy = 0;
    while (<FORMER>) {
        s/\r?\n//g;
        if (/<\/LITURGY>/) {
            $isLiturgy--;
            next;
        }
        if (/<LITURGY>/) {
            $isLiturgy++;
            print OUTPUT "<LITURGY>", "\n";
            foreach my $epistle (keys %epistles) {
                my %ndayFs = ();
                foreach my $cmd (@{$epistles{$epistle}}) {
                    $ndayFs{ ${ $cmd }{ndayF} } = 1;
                }
                foreach my $ndayF (keys %ndayFs) {
                    my @doys = ();
                    foreach my $cmd (@{$epistles{$epistle}}) {
                        push (@doys, ${ $cmd }{doy}) if (${ $cmd }{ndayF} == $ndayF && ${ $cmd }{doy} < 100);
                    }
                    if (scalar @doys) { 
                        my $nday = -1 * $ndayF;
                        my $command = xmlEscape("nday == $nday && (" . stringify(@doys) . ")");
                        my $reading = $lectionary{$epistle}{$dow}{epistleReading};
                        my $pericope = $lectionary{$epistle}{$dow}{epistlePericope};
                        print OUTPUT qq(<READING Type="apostol" Reading="$reading" Pericope="$pericope" EffWeek="$epistle" Cmd="$command"/>), "\n";
                    }
                    @doys = ();
                   
                    foreach my $cmd (@{$epistles{$epistle}}) {
                        push (@doys, ${ $cmd }{doy}) if (${ $cmd }{ndayF} == $ndayF && ${ $cmd }{doy} > 100);
                    }
                    if (scalar @doys) {
                        my $nday = -1 * $ndayF;
                        my $command = xmlEscape("ndayF == $nday && (" . stringify(@doys) . ")");
                        my $reading = $lectionary{$epistle}{$dow}{epistleReading};
                        my $pericope = $lectionary{$epistle}{$dow}{epistlePericope};
                        print OUTPUT qq(<READING Type="apostol" Reading="$reading" Pericope="$pericope" EffWeek="$epistle" Cmd="$command"/>), "\n";
                    }
                }
            }
            foreach my $gospel (keys %gospels) {
                my %ndayFs = ();
                foreach my $cmd (@{$gospels{$gospel}}) {
                    $ndayFs{ ${ $cmd }{ndayF} } = 1;
                }
                foreach my $ndayF (keys %ndayFs) {
                    my @doys = ();
                    foreach my $cmd (@{$gospels{$gospel}}) {
                        push (@doys, ${ $cmd }{doy}) if (${ $cmd }{ndayF} == $ndayF && ${ $cmd }{doy} < 100);
                    }
                    if (scalar @doys) { 
                        my $nday = -1 * $ndayF;
                        my $command = xmlEscape("nday == $nday && (" . stringify(@doys) . ")");
                        my $reading = $lectionary{$gospel}{$dow}{gospelReading};
                        my $pericope = $lectionary{$gospel}{$dow}{gospelPericope};
                        print OUTPUT qq(<READING Type="gospel" Reading="$reading" Pericope="$pericope" EffWeek="$gospel" Cmd="$command"/>), "\n";
                    }
                    @doys = ();
                   
                    foreach my $cmd (@{$gospels{$gospel}}) {
                        push (@doys, ${ $cmd }{doy}) if (${ $cmd }{ndayF} == $ndayF && ${ $cmd }{doy} > 100);
                    }
                    if (scalar @doys) {
                        my $nday = -1 * $ndayF;
                        my $command = xmlEscape("ndayF == $nday && (" . stringify(@doys) . ")");
                        my $reading = $lectionary{$gospel}{$dow}{gospelReading};
                        my $pericope = $lectionary{$gospel}{$dow}{gospelPericope};
                        print OUTPUT qq(<READING Type="apostol" Reading="$reading" Pericope="$pericope" EffWeek="$gospel" Cmd="$command"/>), "\n";
                    }

                }
            }
            print OUTPUT "</LITURGY>", "\n";
            next;
        }
        if ($isLiturgy) {
            next;
        }
        print OUTPUT $_, "\n";
    }
    close (OUTPUT);
    close (FORMER);
    unlink "$output_path/$outfile.xml";
    rename "$output_path/$outfile.bak", "$output_path/$outfile.xml";
}