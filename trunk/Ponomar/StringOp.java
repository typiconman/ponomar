package Ponomar;
import java.util.Hashtable;

/*****************************************************************************
 StringOp.java :: A CLASS TO HANDLE ADDITIONAL STRING OPERATIONS 
 FOR THE PONOMAR PROGRAM(ME)
	
 (C) 2007 ALEKSANDR ANDREEV. ALL RIGHTS RESERVED.
 SIGNIFICANT PORTIONS (C) 2007 YURI SHARDT. ALL RIGHTS RESERVED.

 IMPORTANT: ****do not copy, modify, or distribute this class****
 THIS MODULE IS STILL **IN DEVELOPMENT**, AND MAY NOT BE REPRODUCED,
 MODIFIED, OR REDISTRIBUTED, IN WHOLE OR IN PART, FOR ANY REASON 
 WITHOUT THE EXPRESS CONSENT AND WRITTEN PERMISSION OF THE AUTHORS. 
 THIS CODE WILL BE MADE PUBLIC ONCE IT HAS REACHED A STABLE LEVEL.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL
 THE AUTHOR BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT
 OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.

 SUMMARY OF FUNCTIONS:
 	capitalize(String) <- CAPITALIZES A STRING
	join(String[])	   <- JOINS THE MEMBERS OF THE ARRAY INTO ONE STRING, SEPARATED BY " "
	join(String[], char) <- DITTO, SEPARATED BY char
	join(String[], String) <- DITTO, SEPARATED BY String
	Hashtable dayInfo <- STORES VARIABLE VALUES FOR eval(String), SET BY Main.write()
	eval(String) <- PARSES String RETURNING A DOUBLE *
	evalbool(String) <- PARSES String RETURNING A BOOLEAN *
	main(String[]) <- TESTS eval()
	* PLEASE SEE COMMENTS BELOW FOR FULL DOCUMENTATION OF FEATURES
*********************************************************************************/

final class StringOp
{
	//private static LanguagePack Phrases = new LanguagePack();
	//private static String [] Errors=Phrases.obtainValues((String)Phrases.Phrases.get("Errors"));

// JOINS THE MEMBERS OF AN ARRAY, ANALAGOUS TO PERL'S join FUNCTION
protected static String join(String[] pieces)
{
	return join(pieces, " ");
}

protected static String join(String[] pieces, char sep)
{
	return join(pieces, String.valueOf(sep));
}

protected static String join(String[] pieces, String sep)
{
	if(pieces.length == 0) return "";

	StringBuffer buf = new StringBuffer();
	buf.append(pieces[0]);
	for(int i=1,n=pieces.length; i<n; i++)
	{
		buf.append(sep).append(pieces[i]);
	}
	return buf.toString();
}

// CAPITALIZES THE FIRST LETTER OF THE STRING
protected static String capitalize(String str)
{
	return (str.length() > 0) ? Character.toUpperCase(str.charAt(0)) + str.substring(1) : str;
}

// STORES VARIABLE VALUES FOR eval(String) AS String variable -> int value
// Hashtable MUST BE INITIALIZED AND VALUES MUST BE ENTERED BEFORE CALLING eval(String) [in a constructor, probably]
protected static Hashtable dayInfo;

/****************************************************************************************
	eval 2.0 :: AN ALL PURPOSE EXPRESSION PARSER THAT FULLY OBEYS CORRECT ORDER OF OPERATIONS
	IDEA:	RECEIVE AN EXPRESSION CONTAINING VARS, NUMBERS AND OPERATORS
	OF THE FORM nday < 4 && (dow == 5 || dow == 6)
	PARSE THIS EXPRESSION PRESERVING THE ORDER OF OPERATIONS
	AND LOOKING UP THE VARIABLES IN A HASHTABLE
	RETURNING true OR false IF THE EXPRESSION IS TRUE OR FALSE
	OR THE EXPRESSION'S NUMERICAL VALUE IF IT'S A MATHEMATICAL OPERATION
	e.g.
	(80 < Easter) && Easter > 80 || Easter - 49 < 50 && Easter + 49 > 120 || (Easter + 49) % 8 <= 5

	(C) 2007 YURI SHARDT. ALL RIGHTS RESERVED.

	HISTORY:
		April 2007 - eval 1.0 by Aleksandr Andreev written to handle basic logical operators
		June 2007 - rewritten by Yuri Shardt and Aleksandr Andreev with added functionality
		July 2007 - eval 2.0, a complete rewrite of the eval function to handle
		mathematical operations, unary and binary logic operators and correct OOO.
******************************************************************************/

protected static double eval(String expression) throws IllegalArgumentException
{
	double result = Double.NaN;
	int i = -1;

	do
	{
		// STARTING AT THE END OF STRING, FIND LAST OPEN PARENTHESIS
		i = expression.lastIndexOf("(");

		if (i != -1)
		{
			// IF FOUND, LOOK FOR THE FIRST CLOSED PARENTHESIS
			int j = expression.indexOf(")", i);
			// ONLY PRECEED IF THERE IS A CLOSING BRACKET!
			if (j != -1)
			{
				// RECURSION: CALL EVAL ON THE TEXT BETWEEN PARENTHESES
                        	String expression2 = expression.substring(i + 1, j).trim();
				result = eval(expression2);
                    		expression = expression.substring(0, i).trim() + result + expression.substring(j + 1);
                    	}
                    	else
                    	{
                    		throw (new IllegalArgumentException("Malformed expression: Not Enough Brackets") );
                    	}
		}
		// DO THIS UNTIL NO PARENTHESES REMAIN
	}
	while (i != -1);
	
	// WHAT FOLLOWS SHOULD BE THE ORDER IN INCREASING ORDER OF PRECEDENCE, THAT IS,
	// THE FIRST THINGS TO LOOK FOR SHOULD HAVE THE LOWEST PRECEDENCE.
	// ACCORDING TO WIKIPEDIA (ORDER OF OPERATION) THIS IS:
	// 1	 	() [] -> . :: 						Grouping, scope, array/member access
	// 2 		 ! ~ ++ -- - + * & 					(most) unary operations
	// 3 		* / % 							Multiplication, division, modulus
	// 4 		+ - 							Addition and subtraction
	// 5 		<< >> 							Bitwise shift left and right
	// 6 		< <= > >= 						Comparisons: less-than, ...
	// 7 		==  != 							Comparisons: equal and not equal
	// 8 		& 							Bitwise AND
	// 9 		^ 							Bitwise exclusive OR
	// 10 		| 							Bitwise inclusive (normal) OR
	// 11 		&&							Logical AND
	// 12 		||							Logical OR
	// 13 		= += -= *= /= %= &= |= ^= <<= >>=		 	Assignment operators
	// FOR THIS FUNCTION ONLY CASES 2 (SOME AND NOT ALL FUNCTIONS), 3, 4, 6, 7, 11, AND 12 ARE IMPLEMENTED OR EVEN REQUIRED
	// THUS, THE ORDER OF IMPLEMENTATION SHOULD BE 12, 11, 7, 6, 4, 3, AND FINALLY 2
	
	// A COMPLETE REWRITE OF THE EVAL FUNCTION
	// THE GENERAL TEMPLATE IS AS FOLLOWS:
	// FOR ANY COMBINATION OF SYMBOLS WITH THE SAME PRECEDENCE WITH LEFT TO RIGHT EQUAL PRECEDENCE
	// GOING RIGHT TO LEFT, DETERMINE THE FIRST OCCURANCE OF THE SYMBOL
	// SPLIT AT THIS OCCURRANCE OF THE SYMBOL INTO 2 PARTS: LHS AND RHS.
	// CHECK, IF NECESSARY, FOR ANY UNARY OPERATORS (- (AS IN -100), !), WHICH DO NOT TAKE A SPACE BETWEEN THEM AND THE COMPONENT
	//		E.G. -100, !TRUE, !(Easter < 100)
	// EVALUATE THE INDIVIDUAL PARTS (RHS AND LHS)
	// RETURN THE RESULT
		
	// BASED ON ABOVE: LOWEST PRECENDENCE GOES TO ||: THUS, DO IT FIRST
	i = expression.lastIndexOf("||");
	if (i != -1)
	{
		// SPLIT AT THE OPERATOR
		String LHS;
		String RHS;
		LHS = expression.substring(0, i - 1).trim();
		RHS = expression.substring(i + 2).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS
		result = bool2double(evalbool(LHS) || evalbool(RHS));
		return result;
	}

	// NOW CHECK FOR ANY &&
	i = expression.lastIndexOf("&&");
	if (i != -1)
	{
		// SPLIT AT THE OPERATOR
		String LHS;
		String RHS;
		LHS = expression.substring(0, i - 1).trim();
		RHS = expression.substring(i + 2).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS
		result = bool2double(evalbool(LHS) && evalbool(RHS));
		return result;
	}
	
	// NOW DEAL WITH EQUAL AND NOT EQUAL
	int n = -1;
	
	i = expression.lastIndexOf("==");
	int j = expression.lastIndexOf("!=");
	n = i > j ? i : j;
	if (n != -1)
	{
		// SPLIT AT THE OPERATOR
		String LHS;
		String RHS;
		LHS = expression.substring(0, n - 1).trim();
		RHS = expression.substring(n + 2).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS, COMPARE BY OPERATOR
		if (expression.charAt(n) == '=')
		{
			result = bool2double(eval(LHS) == eval(RHS));
		}
		else
		{
			result = bool2double(eval(LHS) != eval(RHS));
		}
		return result;
	}
	
	// NOW DEAL WITH ALL INEQUALITIES (STRICT AND NOT STRICT)
		
	i = expression.lastIndexOf("<");
	j = expression.lastIndexOf(">");
	int k = expression.lastIndexOf("<=");
	int l = expression.lastIndexOf(">=");
	
	// DETERMINE THE LARGEST (CLOSEST TO END OF EXPRESSION) VALUE
	n = i > j ? i : j;
	n = n > k ? n : k;
	n = n > l ? n : l; 	
	
	if (n != -1)
	{
		// SPLIT AT THE OPERATOR
		String LHS;
		String RHS;
		LHS = expression.substring(0, n - 1).trim();
		RHS = expression.substring(n + 2).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS, COMPARE BY OPERATOR
		if (expression.charAt(n) == '<')
		{
			if (expression.charAt(n+1) == '=')
			{
				result = bool2double(eval(LHS) <= eval(RHS));
			}
			else
			{
				result = bool2double(eval(LHS) < eval(RHS));
			}
		}
		else
		{
			if (expression.charAt(n+1) == '=')
			{
				result = bool2double(eval(LHS) >= eval(RHS));
			}
			else
			{
				result = bool2double(eval(LHS) > eval(RHS));
			}
		}
		return result;
	}
	
	// NOW DEAL WITH ALL ADDITIONS AND SUBTRACTIONS
		
	i = expression.lastIndexOf("+");
	
	// CHECK IF THERE IS A UNARY OPERATOR "-" PRESENT HERE
	boolean test = true;
	int len = expression.length();
	while (test)
	{
		j = expression.lastIndexOf("-", len);
		// SINCE UNARY OPERATORS ARE NOT FOLLOWED BY A SPACE: TEST FOR THE PRESENCE OF A FOLLOWING SPACE
               if (expression.charAt(j+1) == ' ')
		{
			test = false;
		}
		else
		{
			// A UNARY OPERATOR WAS FOUND; TRY AGAIN STARTING AT j-1: THERE COULD BE ROW OF UNARY "-"
			len = j-1;
		}
		// TEST IF THE END OF THE STRING HAS BEEN REACHED OR A SINGLE INITIAL UNARY OPERATOR HAS BEEN FOUND ("-900")
		if (j < 1)
		{
			j = -1;
			test = false;
		}
	}
	// DETERMINE THE LARGEST (CLOSEST TO END OF EXPRESSION) VALUE
	n = i > j ? i : j;
	
	if (n != -1)
	{
		// SPLIT AT THE OPERATOR
		String LHS;
		String RHS;
		LHS = expression.substring(0, n - 1).trim();
		RHS = expression.substring(n + 2).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS, COMPARE BY OPERATOR
		if (expression.charAt(n) == '+')
		{
			result = eval(LHS) + eval(RHS);
		}
		else
		{
			result = eval(LHS) - eval(RHS);
		}
		return result;
	}
	
	// NOW DEAL WITH MULTIPLICATION, DIVISION, AND MODULUS OPERATOR
		
	i = expression.lastIndexOf("*");
	j = expression.lastIndexOf("/");
	k = expression.lastIndexOf("%");
	
	// DETERMINE THE LARGEST (CLOSEST TO END OF EXPRESSION) VALUE
	n = i > j ? i : j;
	n = n > k ? n : k;
	
	if (n != -1)
	{
		// SPLIT AT THE OPERATOR
		String LHS;
		String RHS;
		LHS = expression.substring(0, n - 1).trim();
		RHS = expression.substring(n + 2).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS, COMPARE BY OPERATOR
		if (expression.charAt(n) == '*')
		{
			result=eval(LHS) * eval(RHS);
		}
		else if (expression.charAt(n) == '/')
		{
			result=eval(LHS) / eval(RHS);
		}
		else
		{
			result=eval(LHS) % eval(RHS);
		}
		return result;
	}
	
	// NOW DEAL WITH ANY UNARY OPERATORS THAT MAY EXIST.
	// AT THIS POINT THERE IS NO ISSUE WITH MULTIPLE IDENTITIES.
	// THE ONLY POSSIBLE INTERPRETATION AT THIS POINT IS AS A UNARY OPERATOR
	// UNLIKE FOR THE OTHER OPERATORS ABOVE, WHICH HAVE LEFT TO RIGHT PRECEDENCE AT EQUAL LEVEL
	// UNARY OPERATORS TEND TO HAVE RIGHT TO LEFT PRECEDENCE AT EQUAL LEVEL
	// THUS, THE FIRST ONE MUST BE FOUND
	i = expression.indexOf("!");
	j = expression.indexOf("-");
	
	n = i > j ? i : j;
	
	if (n != -1)
	{
		// REMOVE THE FIRST INSTANCE OF THE OPERATOR
		String RHS;
		RHS = expression.substring(n + 1).trim();

		// RECURSION: CALL EVAL ON THE TWO PARTS, COMPARE BY OPERATOR
		if (expression.charAt(n) == '!')
		{
			result = bool2double(!evalbool(RHS));
		}
		else
		{
			result=-eval(RHS);
		}
		return result;
	}

	// IF WE HAVE REACHED THIS POINT, THEN THERE ARE NO MORE OPERATORS LEFT
	// THERE COULD BE A VARIABLE TO BE READ FROM A HASHTABLE;
	// A BOOLEAN STATEMENT (TRUE or FALSE)
	// OR A NUMBER

	if (expression.indexOf("true") != -1)
	{
		result = bool2double(true);
		return result; // <----------------- ADDED BY A. ANDREEV 8/1/07 N.S. TO FIX HANDLING OF !TRUE
	}
	else if (expression.indexOf("false") != -1)
	{
		result = bool2double(false);
		return result; // <----------------- ADDED BY A. ANDREEV 8/1/07 N.S. TO FIX HANDLING OF !TRUE
	}
	try
	{
		// IT'S A NUMBER -> JUST GET THE NUMBER
		result = Double.parseDouble(expression);
	}
	catch (NumberFormatException nfe)
	{
		// IT'S A VAR -> GET ITS VALUE FROM A HASH AND CHECK IF IT EXISTS
		try
		{
			result = Integer.parseInt(dayInfo.get(expression).toString());
		}
		catch (NullPointerException npe)
		{
			throw (new IllegalArgumentException("Malformed expression: " + expression + " was not found"));
		}

	}

	return result;
}

protected static boolean evalbool(String expression)
{
	// THIS FUNCTION SIMPLY CONVERTS A DOUBLE INTO A BOOLEAN EXPRESSION, WHERE 0 = FALSE
	// EVERYTHING ELSE EQUALS TRUE! THIS FUNCTION IS REQUIRED FOR &&, ||, and ! OPERATORS
	
	Double result = eval(expression);
	if (result == Double.NaN)
	{
		System.out.println("Error Reading the values");
	}
	// CONVERT TO BOOLEAN
	boolean resultboolean = true;
	if (result == 0)
	{
		resultboolean = false;
	}
		
	return resultboolean;
}

// INTERNAL FUNCTION TO CAST BOOLEAN AS DOUBLE
private static double bool2double(boolean expression)
{
	double result = 1.0;
	if (expression == false)
	{
		result = 0.0;
	}
	return result;
}

// CAN BE USED TO TEST eval(String) AGAINST OUTPUT FROM KNOWN SOURCE, E.G. MATLAB
public static void main(String[] argz)
{
	//String arg = join(argz);
	dayInfo = new Hashtable();
        dayInfo.put("nday", 3);
	dayInfo.put("wday", 6);
	dayInfo.put("Easter",81);
	
	String arg = 
	//"(Easter + 5) < (100 +5";								//should fail with an error message stating no closing brackets.
	//"Today + 5 > 100";						 			//should fail and return an error message with non-existant variable
	//"(Easter + 50 - 100 - 1000) < 500";							//true according to MATLAB
	//"(80 < Easter < 100)  || Easter > 80 && Easter - 49 < 50 || Easter + 49 > 120"; 	//true according to MATLAB
	//"(80 < Easter < 100)  && Easter > 80 || Easter - 49 < 50 && Easter + 49 > 120"; 	//true according to MATLAB
	//"80 < Easter < 100"; 	<-------- ACTUALLY, THIS IS NOT VALID, PLEASE SEE BELOW ------------------------
	//"(Easter + 48) * (Easter - 48) > 45"; 						//true		|
	//"(Easter) < 48"; 									//false		|
	//"(Easter - 49) % 8 < 1 && !(Easter + 45 > wday == 6)";  				// TRUE		|
	//"!((nday > 4 && wday == 6) || (wday  == 5 || wday == 6))";				//false 	|
	//"((nday > 4 && wday == 6) || (wday  == 5 || wday == 6))";				//true		|
	 // "Easter - 49  < 48";								//true		|
	// "(Easter - 49) > 45";								//false		|
	// "!(nday > 4)";									//true		|
	//"!!!!!(nday > 4)";									//true		|
	//"---------Easter < 0";								// true		|
	//"-Easter < 0 && nday > -3";								// true		|
	// TESTING THE NUMERICAL PORTION OF THE PROGRAMME							|
	//"2 + -----Easter";									// -79		|
	//"(Easter + 49) % 8";									// 2		|
	// THE FOLLOWING TESTS WERE ADDED BY A. ANDREEV 8/1/07 N.S. TO CHECK BOOLEAN FUNCTIONALITY		|
	//"!true";										// false	|
	//"!false";										// true		|
	//"true || false";									// true		|
	"true && false || false && true";							// false	|
	// "60 < Easter < 80";									// true!!!!!!  \|/
	/**													v
		80 < Easter < 100 is "true" since the function first evaluates 80 < Easter to be 1, which is < 100.
		Analogously, 60 < Easter < 80 is evaluated true, when in fact it should be false.

		In reality x < y < z is nice functionality, but it does not appear to be supported natively
		by e.g. Perl's eval() function.  That is too bad, but there isn't much we can do about it.
		I suggest reverting back to 80 < Easter && Easter < 100 for the sake of cross-language compatibility.

		A. ANDREEV, 8/1/2007 N.S.
	*****/
	System.out.println("Evaluating " + arg);
	System.out.println(evalbool(arg));
}

}
