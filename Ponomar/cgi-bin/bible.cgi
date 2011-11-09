#!/usr/bin/perl -wT

use warnings;
use strict;
use utf8;

############################################################################################
#### bible.cgi :: THIS IS THE BIBLE READER FOR ONLINE MENOLOGION, COMPATIBLE W/ V. 3
####
############################################################################################

use CGI;
use CGI::Carp qw( fatalsToBrowser );
use CGI::Cookie;
use Tie::IxHash;
use XML::Parser;
use lib "./";
use General;

use constant false => 0;

BEGIN {
	$ENV{PATH} = "/bin:/usr/bin";
	delete @ENV{ qw( IFS CDPATH ENV BASH_ENV ) };
}

## GLOBALS
my $basepath = "/home/ponomar0/svn/Ponomar/languages/";
my @instructions; # Global array containing reading instructions
my @chapters; ## THE CHAPTERS PART OF THE READINGS
my @verses;   ## THE VERSES PART OF THE READINGS
my $dummy; ## A DUMMY TO KEEP THE PREVIOUS CHAPTER

### DATA GLOBALS
my $GS = 1; # gospel selector: exJordanville = 0; Lucan Jump = 1
my $language = "en"; # English is the default language
my $Lat = 60.0; # THE DEFAULT LATITUDE
my $Lon = 30.3; # THE DEFAULT LONGITUDE
my $TZ  = 3;	# THE DEFAULT TIMEZONE (FROM GMT)
my $City = "Saint Petersburg, Russia"; # THE NAME OF THE DEFAULT LOCATION
my $dow;	# today's day of week
my $doy;	# Day-of-year
my $nday;	# Number of days before or after this year's Pascha
my $ndayP;	# Number of days after last year's Pascha
my $ndayF;	# Number of days before next year's Pascha
my $Tone;
my $Year;
my $dRank;
my @GLOBALS = qw /dow doy nday Year GS Tone dRank/;

#################################### BEGIN SUBROUTINES ###########################
sub findBottomUp {
	my ($language, $file) = @_;
	
	###########################
	### THIS ALGORITHM IMPLEMENTS BOTTOM-UP READING OF FILES IN THE XML PATH
	### THE FULL IMPLEMENTATION IS DESCRIBED BY YURI IN "A Description", p. 27
	###
	### Basically, we begin with basepath/<language>/<script>/<locale>/file
	### and go until basepath/file
	### Stopping at the first occurance of file, which is then read
	###
	### PARAMETERS PASSED TO HERE: $language is e.g. cu/ru or zh/Hans
	### $file is e.g., xml/01/01.xml or xml/pentecostarion/01.xml
	###########################
	
	# we have a path like language: cu/ru or zh/Hans
	# file: xml/01/01.xml
	my @parts = split (/\//, $language);
	for (my $j = $#parts; $j >= 0; $j--) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/$file";
		return $path if (-e $path);
	}

	return $basepath . $file if (-e $basepath . $file);
	die "Unable to find file $file in the bottom-up path for language $language";
}

sub findTopDown {
	my ($language, $file) = @_;
	
	###########################
	### THIS ALGORITHM IMPLEMENTS THE TOP-DOWN APPROACH FOR READING FILES
	### DESCRIBED BY YURI IN op. cit., p. 28
	###
	### WE CREATE AN ARRAY OF ALL EXTANT FILES NAMED $file IN ALL PATHS
	### BEGINNING WITH BASEPATH
	### AND UP TO $basepath/<language>/<script>/<locale>/file
	### PARAMETERS: SAME AS ABOVE
	############################
	
	my @paths = ();
	push @paths, $basepath . $file if (-e $basepath . $file);
	my @parts = split(/\//, $language);
	for (my $j = 0; $j < @parts; $j++) {
		my $path = $basepath . join ("/", @parts[0..$j]) . "/" . $file;
		push @paths, $path if (-e $path);
	}
	warn "Unable to find any instances of $file in the path for $language" unless (@paths);
	return @paths;
}

sub isNumeric {
	shift;
	return /^(\d+\.?\d*|\.\d+)$/;
}

sub max {
	my $max = shift;
	for ( @_ ) { $max = $_ if $max < $_; }
	return $max;
}

sub output {
	my $lineout = shift; ## line to be written
	my $mood    = shift; ## the tone
	my $k = index( $lineout, "|" );
	my $out = "";
	if ( index( $lineout, "\*\*") != -1) {
		## A line with reading instructions

		my @tmp = split (/\*\*/, $lineout); # Get the parts
		$lineout = $mood eq "rtf" ?  join("{\\cf2 **}", ($tmp[0], $tmp[2])) :
			join ("<A Href=\"\#instructions\">**</A>", ($tmp[0], $tmp[2]) ); ## Join the parts outside **
		push @instructions, "**" . $tmp[1]; ## Move the part within ** to an array
	}

	if ( $lineout =~ /\#\d/ ) {
		## A line with the chapter number
		## Write the chapter number
		my $k = substr( $lineout, 1 );
		return $mood eq "rtf" ? "{\\b\\fs28 Chapter $k}\\par\\par\n": 
			$hideversenum == 1 ? "" : "<BIG><B>Chapter $k </B></BIG><BR><BR>\n";
	} elsif ($k != -1) { 
		my $l = substr( $lineout, $k + 1); # Output
		my $v = substr( $lineout, 0, $k); # Verse number
		$out = $mood eq "rtf" ? "{\\b $v} $l\\par\n" : 
			$hideversenum == 1 ? "$l &nbsp;\n" : "<B>$v</B> $l<BR>\n";
	}

	if ($mood eq "html") {
		$out =~ s/\*\(/\<FONT Color=red\>\(/;
		$out =~ s/\)\*/\)\<\/FONT\>/;
	} elsif ($mood eq "rtf") {
		$out =~ s/\*\(/{\\cf2 \(/;
		$out =~ s/\)\*/\)}/;
	}
	return $out;
}

sub readBible {
	my $mood = shift;
	
	my $filename = $basepath . "$version/" . $book . ".text";
	open (PASSAGE, $filename) || die ("Unable to read from $filename : $!");
		my $dummy   = 0;	# Dummy variable; 1 if we print current line, 0 otherwise
		my $curline = "";	# Contents of the current line of the book file
		my $curchap = 0; 	# Current chapter number
		my $curverse = 0;	# Current verse number

		my $text = ($mood eq "html" && index($version, "cu/") != -1) ? qq( <SPAN Style="font-family: Hirmos Ponomar; font-size: 16px;">) : 
			   ($mood eq "html") ? qq(<SPAN Style="font-size: 14px">) : "";

		## LOOP THROUGH ALL THE READINGS
		for (my $i = 0; $i < @chapters; $i++) {
			while ($curchap != $chapters[$i]) {
				$curline = <PASSAGE>;
				$curline =~ s/\r?\n//g;
				## FORMAT #<#> WHERE <#> IS THE CHAPTER NUMBER
				if (index($curline, "\#") != -1) {
					$curchap = substr( $curline, 1 );
					$curverse = 0;
				}
				$text .= output($curline, $mood) if $dummy;
				last if eof;
			}
			while ($curverse != $verses[$i]) {
				last if eof;
				$curline = <PASSAGE>;
				my $n = index($curline, "|");
				$curverse = substr( $curline, 0, $n) if $n != -1;
				$text .= output($curline, $mood) if $dummy || ($curverse == $verses[$i]);
			}
			$dummy = 1 - $dummy;
		}
	close (PASSAGE);
	$text .= ($mood eq "html") ? "</SPAN>" : "";
	return $text;
}

################################ PARSING SUBROUTINES #######################################
tie my %versions, "Tie::IxHash";
tie my %books, "Tie::IxHash";
tie my %chaps, "Tie::IxHash";
my $readingVersion = 0;

sub default {
	return;
}

sub text {
	return;
}

sub startElement {
	my( $parseinst, $element, %attrs ) = @_;
	SWITCH: {
		if ($element eq "BIBLE") {
			$versions{$attrs{Id}} = $attrs{Name};
			$readingVersion = $attrs{Id};
			my @lang_parts = split(/\//, $attrs{Id});
			# one of the components of lang parts must be bible
			my $tmp_lang = "";
			foreach (@lang_parts) {
				last if index($_, "bible") != -1;
				$tmp_lang .= $_ . "/";
			}
			# strip trailing slash
			chop $tmp_lang;
			$tmp_lang = "cu/ru" if ($tmp_lang eq "cu");
			if ($language eq $tmp_lang) {
				$version = $readingVersion;
			}
			last SWITCH;
		}
		if ($element eq "BOOK" && $version eq $readingVersion) {
			$books{$attrs{Id}} = $attrs{Name};
			$chaps{$attrs{Id}} = $attrs{Chapters};
			last SWITCH;
		}
		if ($element eq "INFO" && $version eq $readingVersion) {
			$orient = "rtl" if $attrs{Orient} eq "rtl";
			last SWITCH;
		}
	}
}

sub endElement {
	my ($parseinst, $elem) = @_;
	if ($elem eq "BIBLE") {
		$readingVersion = 0;
	}
}

############################# END OF SUBROUTINES ################################

##################################### BEGIN CODE #######################################
my $q = new CGI;

#### GET THE USER'S COOKIE
my %cookies = fetch CGI::Cookie;
if ($cookies{"menologion"}) {
	my @temp = split(/\|/, $cookies{"menologion"}->value());
	if (@temp == 6) {
		($City, $Lat, $Lon, $TZ, $language, $GS) = @temp;
	}
}

##### SET UP THE ARGUMENTS
my $version = $q->param("version");
my $book    = $q->param("book")    || "Gen";
my $reading = $q->param("reading") || "1:1-13"; ## EG: 2:11-3:2, 5, 13-14, 17-4:1
my $mode    = $q->param("mode");
my $menu    = $q->param("menu");
my $language = $q->param("lang") || "en";
my $hideversenum = $q->param("hideversenum") || 0;
my $orient  = "ltr";

if ($version) {
	$language = (split(/\//, $version))[0];
}

# PREVENT HACKING
if ($language =~ /^([a-zA-Z\/]+)$/) {
	$language = $1;
} else {
	$language = "en";
}

## GET THE XML BIBLE DATA.
my $parser = new XML::Parser(ErrorContext => 2);
$parser->setHandlers(	Start   => \&startElement,
			End     => \&endElement,
			Char    => \&text,
			Default => \&default);

$parser->parsefile( findBottomUp($language, "xml/bible.xml") );

# CHECK VALIDITY
unless ( exists( $versions{$version} ) ) {
	die ("The version $version does not exist in the Lectionary");
}
unless ( exists( $books{$book} ) ) {
	die ("The book $book does not exist in the Scriptures");
}

####### FIGURE OUT WHICH SECTIONS OF THE BIBLE NEED TO BE READ #####

if ( index($reading, ":") == -1) {
	$chapters[0] = $reading;
	$verses[0] = 1;
	$chapters[1] = $reading + 1;
	$verses[1] = 0; ## 0 MEANS STOP BEFORE THE CHAPTER STARTS	
} else {
	my @parts = split(", ", $reading);
	## EG 2:11-3:2 / 5 / 13-14 / 17-4:1

	foreach my $part (@parts) {
		my @sections = split("-", $part);
		## EG 2:11 / 3:2, or just 5
		if (@sections == 1) {
			## only one part of the reading; replicate for the second part
			## (This happens in the case of 5
			$sections[1] = $sections[0];
		}
	
		foreach my $section (@sections) {
			my ($chapter, $verse) = split(":", $section);
			## PROBLEM IN 13-14 example and 5 example
			if ( index($section, ":") == -1) {
				## USE PREVIOUS CHAPTER NUMBER AND THIS AS A VERSE NUMBER
				$verse   = $section;
				$chapter = $dummy; ## USE PREVIOUS CHAPTER
			}
			$dummy = $chapter; ## STORE CURRENT CHAPTER JUST IN CASE WE NEED IT AGAIN
			
			## ADD THE CHAPTER VERSE TO THE READINGs arrays
			push @chapters, $chapter;
			push @verses, $verse;
		}
	}
}

############################# OUTPUT CONSTRUCTION ############################3
if ($mode eq "print") {
	# Create a printable version
	print "Content-type: text/html; charset=utf-8\n\n";
	print qq(<HTML><HEAD><TITLE>$book $reading</TITLE>
<LINK Rel="stylesheet" Href="http://www.ponomar.net/main.css" Type="text/css" />
</HEAD>
<BODY OnLoad="JavaScript:window.print();">
<DIV Class="mainframe">
<CENTER><BIG><B>$books{$book} $reading [$versions{$version}]</B></BIG></CENTER><BR><BR>);
	print readBible("html");
	if (@instructions) {
		# PROCESS THE READING INSTRUCTIONS
		print "<DIV Class=\"instructions\"><A Name=\"instructions\">\n";
		print join "<BR>", @instructions;
		print "</A></DIV>\n";
	}
	print qq(</DIV><BR>
<DIV Class="copyright">Printed using Ponomar Lectionary 1.2 at <A Href="http://www.ponomar.net/">http://www.ponomar.net/</A></DIV>
</BODY>
</HTML>);
} elsif ($mode eq "save") {
	## TODO: USE THE EXAMPLE OF xml2stuff TO ALLOW FOR OTHER FORMATS (E.G., TEX, OOO)
	print "Content-type: text/html; charset=utf-8\n";
	print "Content-disposition: attachment; filename=scripture.html\n\n";
		
	print "<HTML><HEAD><TITLE>$books{$book} $reading [$versions{$version}]</TITLE>\n";
	print qq(<META Http-equiv="content-type" Content="text/html; charset=utf-8">\n);
	print qq(</HEAD><BODY>\n);
	print qq(<CENTER><BIG><B>$books{$book} $reading [$versions{$version}]</B></BIG></CENTER><BR><BR>);
	print readBible("html");
	if (@instructions) {
		# PROCESS THE READING INSTRUCTIONS
		print "<DIV Class=\"instructions\"><A Name=\"instructions\">\n";
		print join "<BR>", @instructions;
		print "</A></DIV>\n";
	}
	print qq(</DIV><BR>
<DIV Class="copyright">Saved using Ponomar Lectionary 1.2 at <A Href="http://www.ponomar.net/">http://www.ponomar.net/</A></DIV>
</BODY>
</HTML>);
} else {
	print "Content-type: text/html; charset=utf-8\n\n";

	print <<END_OF_TOP;
<HTML>
<HEAD><TITLE>Ponomar Project :: Online Menologion</TITLE>
<!---
	UNIFIED TEMPLATE FOR ONLINE SERVICES OF THE PONOMAR PROJECT
	MENOLOGION / LECTIONARY / PASCHALION / PARISH DIRECTORY
	Copyright (C) 2008 ALEKSANDR ANDREEV
	This code is free software and is distributed under the terms of the
	GNU General Public License, either version 3 or later.
---->
<LINK Rel="stylesheet" Type="text/css" Href="http://www.ponomar.net/new.css">
<SCRIPT Language="JavaScript" Src="http://www.ponomar.net/new.js"></SCRIPT>
<script type="text/javascript">
var gaJsHost = (("https:" == document.location.protocol) ? "https://ssl." : "http://www.");
document.write(unescape("%3Cscript src='" + gaJsHost + "google-analytics.com/ga.js' type='text/javascript'%3E%3C/script%3E"));
</script>
<script type="text/javascript">
try {
var pageTracker = _gat._getTracker("UA-12187460-2");
pageTracker._setDomainName("none");
pageTracker._setAllowLinker(true);
pageTracker._trackPageview();
} catch(err) {}</script>
</HEAD>
<BODY MarginWidth=0 MarginHeight=0 LeftMargin=0 TopMargin=0 BgColor="#FFFFFF">
END_OF_TOP

	unless ($menu eq "no") {
		print <<END_OF_TOP2;
<CENTER><UL Id="tablist">
<LI><A Href="http://www.ponomar.net/ponomar/index.html">Ponomar Project</A></LI>
<LI><A Href="http://www.ponomar.net/cgi-bin/index.cgi">Online Menologion</A></LI>
<LI><A Href="http://www.ponomar.net/cgi-bin/paschalion.cgi">Paschalion</A></LI>
<LI><A Class="current" Href="#">Lectionary</A></LI>
<LI><A Href="http://www.ponomar.net/cgi-bin/search.cgi">Eureka! Search</A></LI>
</UL></center>
<TABLE CellPadding=0 CellSpacing=0 Width="100%" BgColor="#F6F6F6">
	<TR>
		<TD Align="Center" ColSpan=2>
			<IMG Src="http://www.ponomar.net/images/menologion.gif">
		</TD>
	</TR>
END_OF_TOP2
	} else {
		print "<TABLE CellPadding=0 CellSpacing=0 Width=\"100%\" BgColor=\"#F6F6F6\">";
	}
	
	print <<END_OF_TOP3;
	<TR>
		<TD Class="borderbar" Width=22%>
			<A Href="http://www.ponomar.net/ponomar/"><B>Ponomar Project</B></A>&nbsp;
		</TD>
		<TD Class="borderbar" Width=78%>
			Ponomar -> Lectionary : Daily Scripture readings of the Orthodox Church&nbsp;
		</TD>
	</TR>
	<TR>
		<TD Class="calframe" VAlign="top" Width=22%>
		<!-- Navigation -->
		<FORM Name="bible" Action="http://www.ponomar.net/cgi-bin/bible.cgi" Method="Get" Name="navigate" Id="navigate">
		<INPUT Type="submit" Name="submitbtn" Value="Submit" Style="display: none;">
		<INPUT Type="hidden" Name="mode" Value="specify">
END_OF_TOP3

	## PRINT THE DIFFERENT VERSIONS
	print "<SELECT Name=\"version\" OnChange=\"JavaScript:changeMe();\">\n";

	while ( my ($ver1, $version1) = each(%versions) ) {
		my $selected = ($ver1 eq $version) ? "Selected" : "";
		print "<OPTION Value=\"$ver1\" $selected > $version1 </OPTION>\n";
	}

	print "</SELECT><BR><BR>\n";

	## PRINT THE DIFFERENT BOOKS
	print "<SELECT Name=\"book\" Size=\"15\" OnChange=\"JavaScript:changeMe2();\">\n";
	while ( my ($bk1, $book1) = each(%books) ) {
		my $selected = ($bk1 eq $book) ? "Selected" : "";
		print "<OPTION Value=\"$bk1\" $selected > $book1 </OPTION>\n";
	}

	print "</SELECT> &nbsp;\n";

	## PRINT THE RELEVANT NUMBER OF CHAPTERS
	print "<INPUT Type=\"hidden\" Id=\"reading\" Name=\"reading\" Value=\"$reading\">\n";
	print "<SELECT Name=\"chapter\" Id=\"chapter\" Size=15 OnChange=\"JavaScript:changeMe2();\">\n";

	for (my $j = 1; $j <= $chaps{$book}; $j++) {
		my $selected = ($j == $chapters[0]) ? "Selected" : "";
		print "<OPTION Value=$j $selected > $j &nbsp;&nbsp; </OPTION>\n";
	}

	print "</SELECT></FORM><BR><BR>\n";
	print "<UL Class=\"commentary\"><LI>\n";
	print join "</LI><LI>", qq(<A Href="JavaScript:openWindow('http://www.ponomar.net/slvsupport.html');">How to display Church Slavonic characters</A>);
	print "</LI></UL>\n";
	
	## NOW PRINT THE MAIN FRAME
	my $align = $orient eq "ltr" ? "left" : "right";
	print "</TD><TD Class=\"mainframe\" Id=\"contents\" VAlign=\"top\" Align=\"$align\" Dir=\"$orient\">\n";
	
	## FIGURE OUT THE PREVIOUS SCRIPTURE PASSAGE
	my $prevchap = $chapters[0] - 1;
	my $prevbook = $book;

	if ($prevchap < 1) {
		## FIND THE PREVIOUS BOOK
		my @keys = keys(%books);
		for ( my $j = 0; $j <= @keys; $j++ ) {
			$prevbook = $keys[$j-1] if $keys[$j] eq $book;
		}
		$prevchap = $chaps{$prevbook};
	}

	## FIGURE OUT THE NEXT SCRIPTURE PASSAGE
	my $nextchap = $chapters[0] + 1;
	my $nextbook = $book;

	if ($nextchap > $chaps{$book} ) {
		## FIND THE NEXT BOOK ( minus one because we don't want the book after the last )
		my @keys = keys(%books);
		for ( my $j = 0; $j <= @keys - 1; $j++ ) {
			$nextbook = $keys[$j+1] if $keys[$j] eq $book;
		}
		$nextchap = 1;
	}
	
	## BUILD A NAVIGATION TOOLBAR
	print "<A Href=\"bible.cgi?version=$version&book=$prevbook&reading=$prevchap&hideversenum=$hideversenum\" Title=\"Go to $books{$prevbook} $prevchap\">";
	print "<IMG Src=\"../images/previous.gif\" Border=0 Alt=\"Go to $books{$prevbook} $prevchap\"></A>\n";
	print "<A Href=\"bible.cgi?version=$version&book=$book&reading=$chapters[0]&hideversenum=$hideversenum\" Title=\"View entire chapter of $books{$book} $chapters[0]\">";
	print "<IMG Src=\"../images/current.gif\" Border=0 Alt=\"View entire chapter of $books{$book} $chapters[0]\"></A>\n";
	print "<A Href=\"bible.cgi?version=$version&book=$nextbook&reading=$nextchap&hideversenum=$hideversenum\" Title=\"Go to $books{$nextbook} $nextchap\">";
	print "<IMG Src=\"../images/next.gif\" Border=0 Alt=\"Go to $books{$nextbook} $nextchap\"></A>\n";
	print "<A Href=\"bible.cgi?version=$version&book=$book&reading=$reading&hideversenum=" . (!$hideversenum) . "\">";
	print "<IMG Src=\"../images/versenums.gif\" Border=0 Alt=\"Turn verse numbers off / on\"></A>\n";
	print "<A Href=\"Javascript:decreaseFontSize();\">\n";
	print "<IMG Src=\"../images/font-dec.gif\" Border=0 Alt=\"-\"></A>\n";
	print "<A Href=\"JavaScript:increaseFontSize();\">\n";
	print "<IMG Src=\"../images/font-inc.gif\" Border=0 Alt=\"+\"></A>\n";
	print "<A Href=\"JavaScript:doClipboard();\" Title=\"Copy $books{$book} $reading to Lectionary's Clipboard\">";
	print "<IMG Src=\"../images/copy.gif\" Border=0 Alt=\"Copy $books{$book} $reading to Lectionary's Clipboard\"></A>\n";
	print "<A Href=\"bible.cgi?version=$version&book=$book&reading=$reading&mode=save&hideversenum=$hideversenum\" Title=\"Save $books{$book} $reading as a text file\">";
	print "<IMG Src=\"../images/save.gif\" Border=0 Alt=\"Save $books{$book} $reading as a text file\"></A>\n";
	print "<A Href=\"bible.cgi?version=$version&book=$book&reading=$reading&mode=print&hideversenum=$hideversenum\" Title=\"Print $books{$book} $reading\">";
	print "<IMG Src=\"../images/print.gif\" Border=0 Alt=\"Print $books{$book} $reading\"></A>\n";
	print "<A Href=\"JavaScript:doMail();\" Title=\"E-mail $books{$book} $reading\">";
	print "<IMG Src=\"../images/email.gif\" Border=0 Alt=\"E-mail $books{$book} $reading\"></A><BR><BR>\n";
	print "<CENTER><BIG><B>$books{$book} $reading</BIG></B></CENTER>\n";
	
	print readBible("html");
	if (@instructions) {
		# PROCESS THE READING INSTRUCTIONS
		print "<DIV Class=\"instructions\"><A Name=\"instructions\">\n";
		print join "<BR>", @instructions;
		print "</A></DIV>\n";
	}

	print <<END_OF_FOOTER;
		<br></TD>
	</TR>
	<TR>
		<TD Align="Center" Width="22%" Class="borderbar">
			<A Href="JavaScript:openWindow('http://www.ponomar.net/mailme.html');">Contact Me</A>
		</TD>
		<TD Align="Center" Width="78%" Class="copyright">
			Lectionary 1.3 &copy; 2006-2009 Aleksandr Andreev.
		</TD>
	</TR>
</TABLE>
</BODY>
</HTML>
END_OF_FOOTER

} 

print "\n";
