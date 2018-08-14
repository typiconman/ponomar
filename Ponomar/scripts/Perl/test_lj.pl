#!/usr/bin/perl

use warnings;
use strict;
use utf8;

######################################################################################################
## test_lj.pl :: THIS CODE TESTS THE LUCAN JUMP IMPLEMENTATION AGAINST THE DATA FILES               ##
## THE RESULTING FILES SHOULD BE THE SAME AS THE INPUT DATA FILES USED TO PRODUCE THE CMDs          ##
## BY KAHUNA.PL. THIS CAN BE USED FOR REGRESSION TESTING                                            ##
##                                                                                                  ##
######################################################################################################

use lib "Ponomar/APIs/Perl/";
use Ponomar;
use Ponomar::Util;

my $inpath = "Ponomar/scripts/Perl/data";
my $outpath = "/tmp";

# DO NOT EDIT BELOW THIS LINE!

my @years = qw/2010 2037 2143 2075 2018 2034 2162 2061 2094 2083 2099 2064 2031 2338 2058 2085 2096 2028 2118 2023 2123 
            2039 2055 2088 2077 2104 2071 2020 2267 2036 2025 2047 2041 2074 2063 2090 2079 2033 2022 2060 2038 2155 2065
            2098 2087 2030 2103 2057 2035 2084 2062 2179 2089 2027 2021 2054 2043 2070 2059 2024 2097 2040 2287 2051 2078/;

my $n = 1;
my $failures = 0;

foreach my $year (@years) {
    # compute stuff
    $| = 1;
    print "Now testing data for $year ($n.tsv)...";
    my $key_of_boundaries = Ponomar::Util::getKeyOfBoundaries($year);
    my $key_of_boundaries_next = Ponomar::Util::getKeyOfBoundaries($year + 1);

    my $pascha = Ponomar::Util::getPascha($year);
    my $pascha_next = Ponomar::Util::getPascha($year + 1);
    # begin 13th Monday after Pascha
    my $start = $pascha->addDays(134);
    # end on the Sunday of the Publican and the Pharisee
    my $end = $pascha_next->subtractDays(70); # check the maths here
    open (OUTFILE, ">:encoding(UTF-8)", "$outpath/$n.tsv") || die "Unable to write to file: $!";

    print OUTFILE ";; This is a kahuna data file for the year $year\n";
    print OUTFILE ";; Comments are indicated by the two semicolons\n";
    print OUTFILE ";; The key of boundaries is $key_of_boundaries (next year will be $key_of_boundaries_next)\n";
    print OUTFILE ";; The columns below are:\n";
    print OUTFILE ";; ";
    print OUTFILE join("\t", qw/month day doy nday ndayF apostol gospel/), "\n";
    print OUTFILE ";; The first two columns are useless for the kahuna; they are only for debugging\n";
    print OUTFILE ";; Since you have to be a kahuna youself to use these data, there is no documentation\n";

    for (my $day = $start; $day->before($end); $day = $day->addDays(1)) {
        my $ponomar = new Ponomar($day, 'en');
        print OUTFILE $day->getMonth(), "\t";
        print OUTFILE $day->getDay(), "\t";
        print OUTFILE $day->getDoy(), "\t";
        print OUTFILE $day->getDaysSince($pascha), "\t";
        print OUTFILE $day->getDaysUntil($pascha_next), "\t";

        foreach ($ponomar->getSaints('pentecostarion')) {
            next unless ($_->getKey('CId') >= 9000 && $_->getKey('CId') <= 9315);
            my @services = $_->getServices('liturgy');
            foreach my $service (@services) {
            #    $service->execCommands(1); # check this number
                next unless ($service->hasReadings());
                my @readings = $service->getReadings('apostol');
                foreach my $reading (@readings) { 
                    next unless defined $reading;
                    next unless $reading->getType() eq "apostol";
                    print OUTFILE $reading->getEffWeek(), "\t";
                    last;
                }
                @readings = $service->getReadings('gospel');
                foreach my $reading (@readings) {
                    next unless defined $reading;
                    next unless $reading->getType() eq "gospel";
                    print OUTFILE $reading->getEffWeek();
                    last;
                }
            }
        }
        print OUTFILE "\n";
    }
    close (OUTFILE);

    ## now test the two files
    open (BASELINE, "<:encoding(UTF-8)", "$inpath/$n.tsv") || die "Cannot read from baseline file: $!";
    my @baseline = <BASELINE>;
    close (BASELINE);
    open (GENED, "<:encoding(UTF-8)", "$outpath/$n.tsv") || die "Cannot read from generated file: $!";
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
        unlink "$outpath/$n.tsv";
    } else {
        print "FAILED", "\n";
        open (ERROR, ">> $outpath/errors.txt") || die "Cannot write to error file: $!";
        print ERROR "In file $n.tsv:", "\n";
        foreach (@errors) {
            print ERROR $_, "\n";
        }
        close (ERROR);
        $failures++;
    }
    $n++;
}

unless ($failures) {
    print "Regression testing PASSED", "\n";
    exit 0;
} else {
    print "Regression testing FAILED with $failures mismatched file(s)", "\n";
    print "Mismatched lines printed to $outpath/errors.txt", "\n";
    exit 1;
}
