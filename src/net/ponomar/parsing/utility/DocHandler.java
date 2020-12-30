package net.ponomar.parsing.utility;

import java.util.HashMap;

/*
 * ORIGINALLY FROM: SIMPLENN (https://github.com/cth/simplenn/blob/master/DocHandler.java)
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
 * DocHandler interface for XML parsing. To be used with {@link QDParser}.
 * 
 * @author Christian Theil Have, with slight modifications by Aleksandr Andreev
 */
public interface DocHandler {
	void startElement(String tag, HashMap<String, String> h) throws Exception;

	void endElement(String tag) throws Exception;

	void startDocument() throws Exception;

	void endDocument() throws Exception;

	void text(String str) throws Exception;
}
