# Before `make install' is performed this script should be runnable with
# `make test'. After `make install' it should work as `perl Lingua-CU.t'

#########################

# change 'tests => 1' to 'tests => last_test_to_print';

use strict;
use warnings;
use utf8;

use Test::More tests => 5;
BEGIN { use_ok('Ponomar'); use_ok('Ponomar::Util', qw(getPascha)); };

# test if Paschalion works
my $pascha = getPascha(2021);
ok($pascha->getMonth() == 4 && $pascha->getDay() == 19 && $pascha->getYear() == 2021, "Paschalion works");
diag("Paschalion works");

my $ponomar = new Ponomar($pascha, 'en');
isa_ok($ponomar, 'Ponomar');
diag("Ponomar object can be created");

my @cids = qw(887 4781 4629 888 891 892 893 889 041900 041901);
my @ret = $ponomar->getSaints('menaion');
is_deeply (\@cids, \@ret, "Access of Ponomar data works");
diag("Ponomar data can be accessed");