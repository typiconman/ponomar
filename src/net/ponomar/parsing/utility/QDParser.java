package net.ponomar.parsing.utility;

import java.io.Reader;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;

/*
 * ORIGINALLY FROM: SIMPLENN (https://github.com/cth/simplenn/blob/master/QDParser.java)
 * 
 * Copyright (c) 2006 Christian Theil Have
 * SLIGHT MODIFICATIONS BY ALEKSANDR ANDREEV.
 * (C) 2006-2007 ALEKSANDR ANDREEV. ALL RIGHTS RESERVED.
 * 
 * PERMISSION IS HEREBY GRANTED TO USE, MODIFY, AND/OR REDISTRIBUTE THIS SOURCE
 * CODE PROVIDED THAT THIS NOTICE REMAINS IN ALL VERSION AND / OR DERIVATIVES
 * THEREOF.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN
 * ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
*/

/**
 * 
 * A Quick and Dirty xml parser, with (very) limited functionality. To be used
 * with classes implementing the {@link DocHandler} interface.
 * 
 * @author Christian Theil Have, with slight modifications by Aleksandr Andreev
 * 
 */
public class QDParser {
	private static int popMode(Deque<Integer> st) {
		if (!st.isEmpty())
			return st.pop();
		else
			return PRE;
	}

	private static final int TEXT = 1, ENTITY = 2, OPEN_TAG = 3, CLOSE_TAG = 4, START_TAG = 5, ATTRIBUTE_LVALUE = 6,
			ATTRIBUTE_EQUAL = 9, ATTRIBUTE_RVALUE = 10, QUOTE = 7, IN_TAG = 8, SINGLE_TAG = 12, COMMENT = 13, DONE = 11,
			DOCTYPE = 14, PRE = 15, CDATA = 16;

	public static void parse(DocHandler doc, Reader r) throws Exception {
		Deque<Integer> st;
		int depth = 0;
		int mode = PRE;
		int c = 0;
		int quotec = '"';
		StringBuilder sb = new StringBuilder();
		StringBuilder etag = new StringBuilder();
		String tagName = null;
		String lvalue = null;
		String rvalue = null;
		HashMap<String, String> attrs = null;
		st = new LinkedList<>();
		doc.startDocument();
		int line = 1, col = 0;
		boolean eol = false;
		while ((c = r.read()) != -1) {

			// We need to map \r, \r\n, and \n to \n
			// See XML spec section 2.11
			if (c == '\n' && eol) {
				eol = false;
				continue;
			} else if (eol) {
				eol = false;
			} else if (c == '\n') {
				line++;
				col = 0;
			} else if (c == '\r') {
				eol = true;
				c = '\n';
				line++;
				col = 0;
			} else {
				col++;
			}

			if (mode == DONE) {
				doc.endDocument();
				return;

				// We are between tags collecting text.
			} else if (mode == TEXT) {
				if (c == '<') {
					st.push(mode);
					mode = START_TAG;
					if (sb.length() > 0) {
						doc.text(sb.toString());
						sb.setLength(0);
					}
				} else if (c == '&') {
					st.push(mode);
					mode = ENTITY;
					etag.setLength(0);
				} else {
					sb.append((char) c);
				}
				// we are processing a closing tag: e.g. </foo>
			} else if (mode == CLOSE_TAG) {
				if (c == '>') {
					mode = popMode(st);
					tagName = sb.toString();
					sb.setLength(0);
					depth--;
					if (depth == 0)
						mode = DONE;
					doc.endElement(tagName);
				} else {
					sb.append((char) c);
				}

				// we are processing CDATA
			} else if (mode == CDATA) {
				if (c == '>' && sb.toString().endsWith("]]")) {
					sb.setLength(sb.length() - 2);
					doc.text(sb.toString());
					sb.setLength(0);
					mode = popMode(st);
				} else {
					sb.append((char) c);
				}
				// we are processing a comment. We are inside
				// the <!-- .... --> looking for the -->.
			} else if (mode == COMMENT) {
				if (c == '>' && sb.toString().endsWith("--")) {
					sb.setLength(0);
					mode = popMode(st);
				} else {
					sb.append((char) c);
				}
				// We are outside the root tag element
			} else if (mode == PRE) {
				if (c == '<') {
					mode = TEXT;
					st.push(mode);
					mode = START_TAG;
				}

				// We are inside one of these <? ... ?>
				// or one of these <!DOCTYPE ... >
			} else if (mode == DOCTYPE) {
				if (c == '>') {
					mode = popMode(st);
					if (mode == TEXT)
						mode = PRE;
				}

				// we have just seen a < and
				// are wondering what we are looking at
				// <foo>, </foo>, <!-- ... --->, etc.
			} else if (mode == START_TAG) {
				mode = popMode(st);
				if (c == '/') {
					st.push(mode);
					mode = CLOSE_TAG;
				} else if (c == '?') {
					mode = DOCTYPE;
				} else {
					st.push(mode);
					mode = OPEN_TAG;
					tagName = null;
					attrs = new HashMap<>();
					sb.append((char) c);
				}

				// we are processing an entity, e.g. &lt;, &#187;, etc.
			} else if (mode == ENTITY) {
				if (c == ';') {
					mode = popMode(st);
					String cent = etag.toString();
					etag.setLength(0);
					if (cent.equals("lt"))
						sb.append('<');
					else if (cent.equals("gt"))
						sb.append('>');
					else if (cent.equals("amp"))
						sb.append('&');
					else if (cent.equals("quot"))
						sb.append('"');
					else if (cent.equals("apos"))
						sb.append('\'');
					// Could parse hex entities if we wanted to
					// else if(cent.startsWith("#x"))
					// sb.append((char)Integer.parseInt(cent.substring(2),16));
					else if (cent.startsWith("#"))
						sb.append((char) Integer.parseInt(cent.substring(1)));
					// Insert custom entity definitions here
					else
						exc("Unknown entity: &" + cent + ";", line, col);
				} else {
					etag.append((char) c);
				}

				// we have just seen something like this:
				// <foo a="b"/
				// and are looking for the final >.
			} else if (mode == SINGLE_TAG) {
				if (tagName == null)
					tagName = sb.toString();
				if (c != '>')
					exc("Expected > for tag: <" + tagName + "/>", line, col);
				doc.startElement(tagName, attrs);
				doc.endElement(tagName);
				if (depth == 0) {
					doc.endDocument();
					return;
				}
				sb.setLength(0);
				attrs = new HashMap<>();
				tagName = null;
				mode = popMode(st);

				// we are processing something
				// like this <foo ... >. It could
				// still be a <!-- ... --> or something.
			} else if (mode == OPEN_TAG) {
				if (c == '>') {
					if (tagName == null)
						tagName = sb.toString();
					sb.setLength(0);
					depth++;
					doc.startElement(tagName, attrs);
					tagName = null;
					attrs = new HashMap<>();
					mode = popMode(st);
				} else if (c == '/') {
					mode = SINGLE_TAG;
				} else if (c == '-' && sb.toString().equals("!-")) {
					mode = COMMENT;
				} else if (c == '[' && sb.toString().equals("![CDATA")) {
					mode = CDATA;
					sb.setLength(0);
				} else if (c == 'E' && sb.toString().equals("!DOCTYP")) {
					sb.setLength(0);
					mode = DOCTYPE;
				} else if (Character.isWhitespace((char) c)) {
					tagName = sb.toString();
					sb.setLength(0);
					mode = IN_TAG;
				} else {
					sb.append((char) c);
				}

				// We are processing the quoted right-hand side
				// of an element's attribute.
			} else if (mode == QUOTE) {
				if (c == quotec) {
					rvalue = sb.toString();
					sb.setLength(0);
					attrs.put(lvalue, rvalue);
					mode = IN_TAG;
					// See section the XML spec, section 3.3.3
					// on normalization processing.
				} else if (" \r\n\u0009".indexOf(c) >= 0) {
					sb.append(' ');
					// TEMPORARILY REMOVED FOR DEBUG PURPOSES
				} else if (c == '&') {
					st.push(mode);
					mode = ENTITY;
					etag.setLength(0);
				} else {
					sb.append((char) c);
				}

			} else if (mode == ATTRIBUTE_RVALUE) {
				if (c == '"' || c == '\'') {
					quotec = c;
					mode = QUOTE;
				} else if (Character.isWhitespace((char) c)) {

				} else {
					exc("Error in attribute processing", line, col);
				}

			} else if (mode == ATTRIBUTE_LVALUE) {
				if (Character.isWhitespace((char) c)) {
					lvalue = sb.toString();
					sb.setLength(0);
					mode = ATTRIBUTE_EQUAL;
				} else if (c == '=') {
					lvalue = sb.toString();
					sb.setLength(0);
					mode = ATTRIBUTE_RVALUE;
				} else {
					sb.append((char) c);
				}

			} else if (mode == ATTRIBUTE_EQUAL) {
				if (c == '=') {
					mode = ATTRIBUTE_RVALUE;
				} else if (Character.isWhitespace((char) c)) {

				} else {
					exc("Error in attribute processing.", line, col);
				}

			} else if (mode == IN_TAG) {
				if (c == '>') {
					mode = popMode(st);
					doc.startElement(tagName, attrs);
					depth++;
					tagName = null;
					attrs = new HashMap<>();
				} else if (c == '/') {
					mode = SINGLE_TAG;
				} else if (Character.isWhitespace((char) c)) {

				} else {
					mode = ATTRIBUTE_LVALUE;
					sb.append((char) c);
				}
			}
		}
		if (mode == DONE)
			doc.endDocument();
		else
			exc("missing end tag", line, col);
	}

	private static void exc(String s, int line, int col) throws Exception {
		throw new Exception(s + " near line " + line + ", column " + col);
	}
}
