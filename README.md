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

IMPORTANT INFORMATION
Ponomar is ALPHA-PHASE software and is intended for SOFTWARE-TESTING PURPOSES ONLY.

In order to fully use the Ponomar Java program, the following steps can be followed:
1) Install the Ponomar Unicode fonts located at https://sci.ponomar.net/fonts.html. 
Although this font is for primarily displaying Church Slavonic, 
it does contain special Typicon glyphs that are used by most of the other languages.
Note that Ponomar uses the TrueType version of this font (Ponomar Unicode TT)
because Java has poor support of OpenType fonts.

2) Install Java 17 or newer.

3) Build the project with the included Maven Wrapper:

`./mvnw -DskipTests package`

The wrapper pins Maven 3.9.16. If Maven 3.9 or newer is installed,
`mvn -DskipTests package` is equivalent. The legacy `make` target delegates to the wrapper.

4) From the root of this project, type

`java -cp target/classes Ponomar.Main`

and the main Ponomar interface should appear.

5) A Perl API is available in Ponomar/APIs/Perl. See its documentation.

6) If you make any changes, run the JUnit and golden-data regression tests:

`./mvnw test`

The full verification build also writes JaCoCo HTML and XML reports beneath
`target/site/jacoco`:

`./mvnw -Dmaven.test.failure.ignore=true verify`

Verification enforces 75% line coverage over the headless production logic, as
well as the stricter line and branch gate for the five core classes. UI classes
are not part of the headless gate. It also omits the printer/dialog helper,
external-database adapter, destructive data-migration utility, and obsolete day
reader whose legacy source tree is not present. The generated report still shows
coverage for every compiled class.

The 2026-09-29 modernization baseline covers 2,201 of 2,908 headless lines
(75.69%). The five-class core covers 769 of 853 lines (90.15%) and 407 of 450
branches (90.44%). These figures provide a reproducible baseline rather than a
promise that future production additions will be covered automatically.

The 532-year Paschalion baseline and all 65 Lucan-jump golden files remain in
`Ponomar/scripts/Perl/data` as Maven test resources. The regressions themselves
are pure Java/JUnit tests. For every date represented by the Lucan fixtures, the
Lucan regression also compares numeric effective weeks selected through the
headless production `Day` and `Commemoration1` path. Named Saturday and Sunday
fixed-feast overrides for Elevation, Nativity, and Theophany are validated but
excluded from that equality check because the Kahuna TSV model does not contain
those richer production rules.

`DivineLiturgy1` and `Matins` are excluded from the headless coverage denominator
because JaCoCo's gate is class-granular and their public `Readings()` paths
unconditionally end in `format()`, which constructs the JFrame-based `Bible`.
Their package-visible classifiers and underlying XML selection path are tested
directly without invoking UI formatting. `Service` remains in the gate because
its headless entry points can be called independently of its UI-bound branches.
Other excluded classes are UI components, printer/dialog helpers, an
external-database adapter, a destructive data-migration utility, and `Days`,
whose legacy `Ponomar/xml` input tree is absent.

The converted `Kahuna` entry point requires explicit input-data and output-lives
directories so an accidental no-argument invocation cannot rewrite tracked XML.

### Known failing tests

The suite intentionally keeps correct expectations for known production defects.
Consequently, `./mvnw test` currently reports the twelve failures below; use
`./mvnw -Dmaven.test.failure.ignore=true verify` when generating the coverage
report while retaining those failures in the report.

| Failing test | Production defect and reason for failure |
| --- | --- |
| `JDateTest.rejectsDayZero` | `JDate` accepts day zero instead of throwing `IllegalArgumentException`. |
| `JDateTest.calculatesLeapYearDayOfYear` | `getDoy()` returns 366 for 29 February 2024 instead of the zero-based value 59. |
| `JDateTest.crossesMonthAndYearBoundaries` | `subtactMonths()` turns 31 January 2024 into 1 January 2024 instead of 31 December 2023. |
| `OrderedHashtableTest.cloneShouldPreserveLongValues` | `clone()` casts a `Long` value to `String`, producing `ClassCastException`. |
| `OrderedHashtableTest.iteratorRejectsUnsupportedRemoval` | The key iterator silently accepts `remove()` although removal is unsupported. |
| `OrderedHashtableTest.supportsBulkOperationsAndViewRemoval` | Removing from `keySet()` does not remove the corresponding map entry, so the returned view violates the map-view contract. |
| `QDParserTest.rejectsMismatchedClosingTags` | The parser accepts mismatched closing tags instead of reporting malformed XML. |
| `NonUiServiceTest.legacyCommemorationShouldAcceptNonNumericIds` | Legacy commemoration lookup parses identifier `B_163` as an integer and throws `NumberFormatException`. |
| `NonUiServiceTest.usualBeginningConvenienceApiShouldReturnTheServiceText` | `UsualBeginning` uses an uninitialized language helper, catches the resulting `NullPointerException`, and returns empty output. |
| `NonUiServiceTest.matinsLowRankSundaySuppressionShouldKeepVectorsAligned` | The low-rank Sunday branch stores reading text in the suppressed rank and tag vectors, then clears the reading vector three times while leaving the rank and tag vectors populated. |
| `NonUiCoverageTest.appliesChineseConditionalAndRecursiveNumberRules` | Simplified-Chinese number rules fall back to English or emit incorrect conditional/recursive forms. |
| `NonUiCoverageTest.formatsJulianAndGregorianDatesInLocalizedText` | Ideographic Gregorian day formatting does not produce the expected Chinese day text. |

The language and calendar data under `Ponomar/languages` remain external to the
JAR because the application reads them through repository-relative paths.
