# Ponomar

Ponomar is a fully-functional program suite for the Orthodox Church and provides the following features:

1. Calendar and liturgical information for any day of any year
2. Liturgical readings for the day
3. Lives of saints
4. Liturgical texts in a variety of languages
5. Liturgical service assembly for any day
6. Library of patristic text and scriptural commentary
7. Library of liturgical music in a variety of traditional chant

Copyright 2006-2018 Aleksandr Andreev and others.

Ponomar is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

Ponomar is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with Ponomar.  If not, see <http://www.gnu.org/licenses/>.

## Important information
Ponomar is ALPHA-PHASE software and is intended for SOFTWARE-TESTING PURPOSES ONLY.

## Building Ponomar

### Getting the right typefaces

Download and install the [Ponomar Unicode fonts](https://sci.ponomar.net/fonts.html).

Although this font is for primarily displaying Church Slavonic, it does contain special Typicon glyphs that are used by most of the other languages.

Note that Ponomar uses the TrueType version of this font (Ponomar Unicode TT) because Java has poor support of OpenType fonts.

If you are not on Windows, make sure you also have the Times New Roman font. On Debian and Ubuntu you will find this in the ttf-mscorefonts-installer package.

### Building from the command line

1) Install an SDK for Java. 

2) Create the .class files by typing 

`make`

4) Run the main class by typing

`make run`

The main Ponomar interface should appear.

An IDE such as Eclipse or IntelliJ will provide a smoother experience with building.

Alternatively, you can also install in and run from a separate folder using the `make install-separate-directory` and `run-separate-directory`.

## Perl API

A Perl API is available in Ponomar/APIs/Perl. See its documentation.

If you make any changes, be sure to run the regression tests:

`make test`
