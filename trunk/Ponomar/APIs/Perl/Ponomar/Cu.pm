package Ponomar::Cu;

=head1 Ponomar::Cu

Ponomar::Cu -- Church Slavonic support in the Ponomar API

=head3 DESCRIPTION

This is not an Object Oriented class, but rather a set of utility functions for working with Church Slavonic texts.

=cut

use strict;
# UTF Support is required for Church Slavonic support!
sub HAVE_UTF8 () { $] >= 5.007003 }

require 5.004;

BEGIN {
	if ( HAVE_UTF8 ) {
		# The string eval helps hide this from Test::MinimumVersion
		eval "require utf8;";
		die "Failed to load UTF-8 support" if $@;
	}
}

require Exporter;
our $VERSION = 0.01;
our @ISA = qw( Exporter );
our @EXPORT = ();
our @EXPORT_OK = qw( hip2unicode ucs2unicode resolveTitli cu2arabic arabic2cu );

require Carp;

=head3 METHODS

=over 4

=item hip2unicode ( $string )

Takes C<$string>, a string in HIP (Hyperinvariant Presentation), and converts it to Unicode

=cut

sub hip2unicode {
	my $string = shift;
	
	return;
}

=item ucs2unicode ( $string )

Takes C<$string>, a string in UCS (Universal Church Slavonic), and converts it to Unicode

=cut

sub ucs2unicode {
	my $string = shift;

	my %dictionary = (
		"!" => chr(0x0021),
		"#" => chr(0x0486),
		"\$" => chr(0x0486) . chr(0x0301),
		"%" => chr(0x0486) . chr(0x0300),
		"&" => chr(0x0483),
		"(" => chr(0x0028),
		")" => chr(0x0029),
		"*" => chr(0x002A),
		"+" => chr(0x2DE1), # combining BE
		"," => chr(0x002C),
		"-" => chr(0x002D),
		"." => chr(0x002E),
		"/" => chr(0x002F),
		"0" => chr(0x043E) . chr(0x0301),
		"1" => chr(0x0301),
		"2" => chr(0x0300),
		"3" => chr(0x0486),
		"4" => chr(0x0486) . chr(0x0301),
		"5" => chr(0x0486) . chr(0x0300),
		"6" => chr(0x0311), # combining inverted breve
		"7" => chr(0x0483), # titlo
		"8" => chr(0x033E), # combining vertical tilde
		"9" => chr(0x0436) . chr(0x0483), # zhe with titlo above
		":" => chr(0x003A),
		";" => chr(0x003B),
		"<" => chr(0x2DEF), # combining HA
		"=" => chr(0x2DE9), # combining EN
		">" => chr(0x2DEC), # combining ER
		"?" => chr(0x2DF1), # combining CHE
		"@" => chr(0x0300),
		"A" => chr(0x0430) . chr(0x0300), # latin A maps to AZ with grave accent
		"B" => chr(0x0463) . chr(0x0311), # latin B maps to Yat' with inverted breve
		"C" => chr(0x2DED), # combining ES
		"D" => chr(0x0434) . chr(0x2DED),
		"E" => chr(0x0435) . chr(0x0300), # latin E maps to e with grave accent
		"F" => chr(0x0472), # F maps to THETA
		"G" => chr(0x0433) . chr(0x0483), # G maps to ge with TITLO
		"H" => chr(0x0461) . chr(0x0301), # latin H maps to omega with acute accent
		"I" => chr(0x0406),
		"J" => chr(0x0456) . chr(0x0300), 
		"K" => chr(0xA656) . chr(0x0486), # YA with psili
		"L" => chr(0x043B) . chr(0x2DE3), # el with cobining de
		"M" => chr(0x0476), # capital IZHITSA with izhe titlo
		"N" => chr(0x047A) . chr(0x0486), # capital WIDE ON with psili
		"O" => chr(0x047A), # just capital WIDE ON
		"P" => chr(0x0470), # capital PSI
		"Q" => chr(0x047C), # capital omega with great apostrophe
		"R" => chr(0x0440) . chr(0x0483), # lowercase re with titlo
		"S" => chr(0x0467) . chr(0x0300), # lowercase small yus with grave
		"T" => chr(0x047E), # capital OT
		"U" => chr(0x041E) . chr(0x0443), # diagraph capital UK
		"V" => chr(0x0474), # capital IZHITSA
		"W" => chr(0x0460), # capital OMEGA
		"X" => chr(0x046E), # capital XI
		"Y" => chr(0xA64B) . chr(0x0300), # monograph uk with grave
		"Z" => chr(0x0466), # capital SMALL YUS
		"[" => chr(0x005B),
		"\\" => chr(0x0483), # yet another titlo 
		"]" => chr(0x005D),
		"^" => chr(0x0311), # combining inverted breve
		"_" => chr(0x033E), # yet another yerik
		"a" => chr(0x0430) . chr(0x0301), # latin A maps to AZ with acute accent
		"b" => chr(0x2DEA), # combining ON
		"c" => chr(0x2DED), # combining ES
		"d" => chr(0x2DE3), # combining DE
		"e" => chr(0x0435) . chr(0x0301), # latin E maps to e with acute accent
		"f" => chr(0x0473), # lowercase theta
		"g" => chr(0x2DE2), # combining ge
		"h" => chr(0x044B) . chr(0x0301), # ery with acute accent
		"i" => chr(0x0456),
		"j" => chr(0x0456) . chr(0x0301), # i with acute accent
		"k" => chr(0xA657) . chr(0x0486), # iotaed a with psili
		"l" => chr(0x043B) . chr(0x0483), # el with titlo
		"m" => chr(0x0477), # izhitsa with izhe titlo
		"n" => chr(0x047B) . chr(0x0486), # wide on with psili
		"o" => chr(0x047B), # wide on
		"p" => chr(0x0471), # lowercase psi
		"q" => chr(0x047D), # lowercase omega with great apostrophe
		"r" => chr(0x0440) . chr(0x2DED), # lowercase er with combining es
		"s" => chr(0x0467) . chr(0x0301), # lowercase small yus with acute accent
		"t" => chr(0x047F), # lowercase ot
		"u" => chr(0x043E) . chr(0x0443), # diagraph uk
		"v" => chr(0x0475), # lowercase izhitsa
		"w" => chr(0x0461), # lowercase omega
		"x" => chr(0x046F), # lowercase xi
		"y" => chr(0xA64B) . chr(0x0301), # monograph uk with acute accent
		"z" => chr(0x0467), # lowercase small yus
		"{" => chr(0xA64B) . chr(0x0311), # monograph uk with inverted breve
		"|" => chr(0x0467) . chr(0x0486) . chr(0x0300), # lowercase small yus with apostroph
		"}" => chr(0x0438) . chr(0x0483), # the numeral eight
		"~" => chr(0x0301), # yet another acute accent
		### SECOND HALF IS THE CYRILLIC BLOCK
		"Ђ" => chr(0x0475) . chr(0x0301), # lowercase izhitsa with acute
		"Ѓ" => chr(0x0410) . chr(0x0486) . chr(0x0301), # uppercase A with psili and acute
		"‚" => chr(0x201A),
		"ѓ" => chr(0x0430) . chr(0x0486) . chr(0x0301), # lowercase A with psili and acute
		"„" => chr(0x201E),
		"…" => chr(0x046F) . chr(0x0483), # the numberal sixty
		"†" => chr(0x0430) . chr(0x0311), # lowercase a with inverted breve
		"‡" => chr(0x0456) . chr(0x0311), # lowercase i with inverted breve
		"€" => chr(0x2DE5), # combining ze
		"‰" => chr(0x0467) . chr(0x0311), # lowercase small yus with inverted breve
		"Љ" => chr(0x0466) . chr(0x0486), # upercase small yus with psili
		"‹" => chr(0x0456) . chr(0x0483), # the numeral ten
		"Њ" => chr(0x0460) . chr(0x0483), # capital OMEGA with psili
		"Ќ" => chr(0x041E) . chr(0x0443) . chr(0x0486) . chr(0x0301), # diagraph uk with apostroph
		"Ћ" => chr(0xA656) . chr(0x0486) . chr(0x0301), # uppercase Iotated A with apostroph
		"Џ" => chr(0x047A) . chr(0x0486) . chr(0x0301), # uppercase Round O with apostroph
		"ђ" => chr(0x0475) . chr(0x2DE2), # lowercase izhitsa with combining ge
		"‘" => chr(0x2018),
		"’" => chr(0x2019),
		"“" => chr(0x201C),
	 	"”" => chr(0x201D),
	 	"•" => chr(0x2DE4), # combining zhe
		"–" => chr(0x2013),
	 	"—" => chr(0x2014),
	 	"™" => chr(0x0442) . chr(0x0483),
		"љ" => chr(0x0467) . chr(0x0486), # lowercase small yus with psili
		"›" => chr(0x0475) . chr(0x0311), # izhitsa with inverted breve
		"њ" => chr(0x0461) . chr(0x0483), # lowercase omega with psili
		"ќ" => chr(0x043E) . chr(0x0443) . chr(0x0486) . chr(0x0301), # diagraph uk with apostroph
	 	"ћ" => chr(0xA657) . chr(0x0486) . chr(0x0301), # lowercase iotaed a with apostroph
	 	"џ" => chr(0x047B) . chr(0x0486) . chr(0x0301), # lowercase Round O with apostroph
		"Ў" => chr(0x041E) . chr(0x0443) . chr(0x0486), # Capital Diagraph Uk with psili
		"ў" => chr(0x043E) . chr(0x0443) . chr(0x0486), # lowercase of the above
		"Ј" => chr(0x0406) . chr(0x0486) . chr(0x0301), # Uppercase I with apostroph
		"¤" => chr(0x0482), # cyrillic thousands sign
		"Ґ" => chr(0x0410) . chr(0x0486), # capital A with psili
		"¦" => chr(0x0445) . chr(0x0483), # lowercase kha with titlo
		"§" => chr(0x0447) . chr(0x0483), # the numeral ninety
		"Ё" => chr(0x0463) . chr(0x0300), # lowecase yat with grave accent
		"©" => chr(0x0441) . chr(0x0483), # the numeral two hundred
		"«" => chr(0x00AB), 
		"¬" => chr(0x00AC),
		"®" => chr(0x0440) . chr(0x2DE3), # lowercase er with dobro titlo
		"Ї" => chr(0x0406) . chr(0x0486),
		"°" => chr(0xA67E), # kavyka
		"±" => chr(0xA657) . chr(0x0486) . chr(0x0300),
		"І" => chr(0x0406),
		"і" => chr(0x0456) . chr(0x0308),
		"ґ" => chr(0x0430) . chr(0x0486),
		"µ" => chr(0x0443), # small letter u (why encoded at the micro sign?!)
		"¶" => chr(0x00B6), # paragraph sign
		"·" => chr(0x00B7), # center dot
		"ё" => chr(0x0463) . chr(0x0301), # lowercase yat with acute accent
		"№" => chr(0x0430) . chr(0x0483), # the numeral one
		"є" => chr(0x0454), # wide E
		"»" => chr(0x00BB),
		"ј" => chr(0x0456) . chr(0x0486) . chr(0x0301), # lowercase i with apostroph
		"Ѕ" => chr(0x0405),
		"ѕ" => chr(0x0455),
		"ї" => chr(0x0456) . chr(0x0486), # lowercase i with psili
		"А" => chr(0x0410),
		"Б" => chr(0x0411),
	 	"В" => chr(0x0412),
		"Г" => chr(0x0413),
		"Д" => chr(0x0414),
		"Е" => chr(0x0415),
		"Ж" => chr(0x0416),
		"З" => chr(0x0417),
		"И" => chr(0x0418),
		"Й" => chr(0x0419),
		"К" => chr(0x041A),
		"Л" => chr(0x041B),
		"М" => chr(0x041C),
		"Н" => chr(0x041D),
		"О" => chr(0x041E),
		"П" => chr(0x041F),
	 	"Р" => chr(0x0420),
		"С" => chr(0x0421),
		"Т" => chr(0x0422),
		"У" => chr(0x0423),
		"Ф" => chr(0x0424),
		"Х" => chr(0x0425),
		"Ц" => chr(0x0426),
		"Ч" => chr(0x0427),
		"Ш" => chr(0x0428),
		"Щ" => chr(0x0429),
		"Ъ" => chr(0x042A),
		"Ы" => chr(0x042B),
		"Ь" => chr(0x042C),
		"Э" => chr(0x0462), # capital yat
		"Ю" => chr(0x042E),
		"Я" => chr(0xA656), # capital Iotified A
		"а" => chr(0x0430),
		"б" => chr(0x0431),
		"в" => chr(0x0432),
		"г" => chr(0x0433),
		"д" => chr(0x0434),
		"е" => chr(0x0435),
		"ж" => chr(0x0436),
		"з" => chr(0x0437),
		"и" => chr(0x0438),
		"й" => chr(0x0439),
		"к" => chr(0x043A),
		"л" => chr(0x043B),
		"м" => chr(0x043C),
		"н" => chr(0x043D),
		"о" => chr(0x043E),
		"п" => chr(0x043F),
	  	"р" => chr(0x0440),
		"с" => chr(0x0441),
		"т" => chr(0x0442),
		"у" => chr(0xA64B), # monograph Uk (why?!)
		"ф" => chr(0x0444),
		"х" => chr(0x0445),
		"ц" => chr(0x0446),
		"ч" => chr(0x0447),
		"ш" => chr(0x0448),
		"щ" => chr(0x0449),
		"ъ" => chr(0x044A),
		"ы" => chr(0x044B),
		"ь" => chr(0x044C),
		"э" => chr(0x0463), # lowercase yat
		"ю" => chr(0x044E),
		"я" => chr(0xA657) # iotaed a
	);
	
	my $what = join("|", map(quotemeta, keys %dictionary));
	$string =~ s/($what)/$dictionary{$1}/g;
	
	return $string;
}

=item resolveTitli ( $string )

Takes C<$string>, a string of Church Slavonic text in Unicode, and resolves any Titli present,
including numerals.

=cut

sub resolveTitli {
	my $string = shift;
	
	return;
}

=item cu2arabic ( $string )

Takes C<$string>, a numeral in Church Slavonic, and converts it to a numeral in Arabic numerals.

=cut

sub cu2arabic {
	my $numeral = shift;
	
	return;
}

=item arabic2cu ( $number )

Takes C<$number>, a number, and returns its representation in Slavonic numerals.

=cut

sub arabic2cu {
	my $number = shift;
	
	return;
}

=back

=cut

1;

__END__
