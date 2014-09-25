package Ponomar::Bible;

=head1 Ponomar::Bible

Ponomar::Bible - a Bible object for the Ponomar API

=cut

use strict;
require Carp;
require 5.004;
use vars qw( $VERSION $parser );
use XML::Parser;
use Ponomar::Util;

BEGIN {
	$VERSION = 0.01;
}

our ($language);

=head3 METHODS

=over 4

=item new( Lang => $language )

Creates a new instance of the Bible. Parameter C<Lang> specifies the language. Reads the appropriate bible.xml file
and loads the Bible information for that language into memory.

=cut

sub new {
	my $class = shift;
	my %pars  = @_;
	
	my $self = bless \%pars, $class;
	$self->init();
	return $self;
}

sub init {
	my $self = shift;
	
	$parser = XML::Parser->new(
		Handlers => {
			Start   => sub { $self->startElement(@_) },
			End     => sub { $self->endElement(@_) },
			Char    => sub { $self->text(@_) },
			Default => sub { $self->default(@_) } 
		});
	local $language = $self->{Lang};
	
	## parse the Bible.xml file!
	$parser->parsefile( findBottomUp($language, "xml/bible.xml") );
	undef $parser; # let the garbage collector kick in
}

=item getBookNameShort( $book )

Returns the short form of the name of the book C<$book> in the current Bible

=cut

sub getBookNameShort {
	my $self = shift;
	my $book = shift;
	
	return ${$self->{_bibleBookNames}}{$book}{Short};
}

=item getBookName ( $book )

Returns the full form of the name of the book C<$book> in the current Bible

=cut

sub getBookName {
	my $self = shift;
	my $book = shift;
	
	return ${$self->{_bibleBookNames}}{$book}{Full};
}

=item exists ( $book )

Returns true if the book C<$book> exists in the current Bible

=cut

sub exists {
	my $self = shift;
	my $book = shift;
	
	return exists ${$self->{_bibleBookNames}}{$book};
}

=item getPassage ($reading)

Returns a set of verse objects with the text of the Bible passage given by the Reading object C<$reading>

=back

=cut

sub getPassage {
	my $self = shift;
	my $reading = shift;
	
	### TODO: we should change the format to allow multiple books. Thus, we could get rid of "Composite"
	my ($book, $passage) = split (/_/, $reading->getReading());
	
	return undef unless (defined $self->getBookNameShort ($book));

	my @chapters = ();
	my @verses   = ();
	## CHECK TO SEE IF THIS ISN'T JUST A CHAPTER SPECIFICATION
	if ( index($passage, ":") == -1) {
		$chapters[0] = $passage;
		$verses[0] = 1;
		$chapters[1] = $passage + 1;
		$verses[1] = 0; ## 0 MEANS STOP BEFORE THE CHAPTER STARTS	
	} else {
		my @parts = split(", ", $passage);
		## EG 2:11-3:2 / 5 / 13-14 / 17-4:1
		my $dummy = "";
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

	# we now have a set of starting and stopping points in @chapters and @verses
	# we are ready to read the Bible!
	local $language = $self->{Lang};
	$book =~ s/ /_/; # UGH!
	my $file = findBottomUp($language, "bible/" . $self->{_version} . "/" . $book . ".text");
	my @output = ();
	
	# open up this book of the Bible
	open (BOOK, $file) || Carp::croak (__PACKAGE__ . "::getPassage(" . $reading->getReading() . ") - Error reading from text file.");
		my $curchap = 0;
		my $curverse = 0;
		my $readThis = 0;
		my $i = 0;
		
		while (<BOOK>) {
			my $text = "";
			if (index($_, "#") != -1) {
				$curchap = substr($_, 1);
				$curverse = 0;
				next;
			} else {
				($curverse, $text) = split (/\|/, $_);
			}
			if ($curchap == $chapters[$i] && $curverse == $verses[$i]) {
				$readThis = 1 - $readThis;
				$i++;
			}
			if ($readThis) {
				# read the passage
				$text =~ s/\r?\n//;
				# remove reading instructions
				# FIXME: THERE NEEDS TO BE A DIFFERENT WAY TO HANDLE THIS
				$text =~ s/\*\*([^\*]+)\*\*/<a href="#" title="$1">**<\/a>/g;
				$text =~ s/\*([^\*]+)\*/<a href="#" title="$1">*<\/a>/g; # /
				push @output, { Chapter => $curchap, Verse => $curverse, Text => $text };
			}
			if ($curchap == $chapters[$i] && $curverse == $verses[$i]) {
				$readThis = 1 - $readThis;
				$i++;
			}
		}	
			
	close (BOOK);

	return @output;
}

############ THE FOLLOWING METHODS SHOULD BE TREATED AS PRIVATE #####################################

sub default {
	return;
}

sub text {
	## FIXME: LIFE SHOULD BE READ HERE
	return;
}

sub startElement {
	my( $self, $parseinst, $element, %attrs ) = @_;
	
	if ($element eq "BIBLE") {
		# IDs are of the form lang/bible/version where lang is ISO lang code
		my ($first, $last) = split(/\/bible\//, $attrs{Id});
		my @id_parts = split (/\//, $first);
		
		my @lang_parts = split(/\//, $language);
	#	$self->{_readBible} = $id_parts[0] eq $lang_parts[0]; ## FIXED
		if ($id_parts[0] eq $lang_parts[0]) {
			if (defined $self->{_version}) {
				$self->{_readBible} = $id_parts[1] eq $self->{_version};
			} else {
				$self->{_readBible} = 1;
				$self->{_version} = $last;
			}
		}
	}
	if ($element eq "BOOK" && $self->{_readBible}) {
		my $tmpid = $attrs{Id};
		$tmpid =~ s/_/ /;
		${$self->{_bibleBookNames}}{$tmpid} = { "Short" => $attrs{Short}, 
							"Full"  => $attrs{Name},
							"Chapters" => $attrs{Chapters}
							};
	}
	return;
}

sub endElement {
	my( $self, $parseinst, $element, %attrs ) = @_;
	
	if ($element eq "BIBLE") {
		undef $self->{_readBible};
	}
	return;
}

1;

__END__

