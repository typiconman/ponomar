package Ponomar;

import static org.junit.jupiter.api.Assertions.*;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class StringOpTest {
    @Test void joinsAndCapitalizesText() {
        String[] words = {"one", "two", "three"};
        assertEquals("one two three", StringOp.join(words));
        assertEquals("one,two,three", StringOp.join(words, ','));
        assertEquals("one::two::three", StringOp.join(words, "::"));
        assertEquals("", StringOp.join(new String[0]));
        assertEquals("Ponomar", StringOp.capitalize("ponomar"));
        assertEquals("", StringOp.capitalize(""));
    }

    static Stream<Arguments> arithmetic() {
        return Stream.of(Arguments.of("2 + 3 * 4", 14.0), Arguments.of("(2 + 3) * 4", 20.0),
            Arguments.of("20 / 5 / 2", 2.0), Arguments.of("10 % 3", 1.0),
            Arguments.of("-(3 + 4)", -7.0), Arguments.of("--5", 5.0));
    }

    @ParameterizedTest @MethodSource("arithmetic")
    void evaluatesArithmeticPrecedence(String expression, double result) {
        assertEquals(result, new StringOp().eval(expression));
    }

    @Test void evaluatesVariablesAndBooleanExpressions() {
        StringOp evaluator = new StringOp();
        evaluator.dayInfo.put("Easter", "81"); evaluator.dayInfo.put("dow", "6");
        assertEquals(86.0, evaluator.eval("Easter + 5"));
        assertTrue(evaluator.evalbool("Easter > 80 && dow == 6"));
        assertFalse(evaluator.evalbool("Easter < 80 || dow != 6"));
        assertTrue(evaluator.evalbool("!(dow == 5)"));
        assertTrue(evaluator.evalbool("true"));
        assertFalse(evaluator.evalbool("false"));
        assertTrue(evaluator.evalbool("Easter >= 81 && Easter <= 81"));
    }

    @Test void rejectsMalformedAndUnknownExpressions() {
        StringOp evaluator = new StringOp();
        IllegalArgumentException missingVariable = assertThrows(
            IllegalArgumentException.class, () -> evaluator.eval("Missing"));
        assertTrue(missingVariable.getMessage().contains("Missing was not found"));
        IllegalArgumentException missingBracket = assertThrows(
            IllegalArgumentException.class, () -> evaluator.eval("(2 + 3"));
        assertTrue(missingBracket.getMessage().contains("Not Enough Brackets"));
    }

    @Test void cloneIsolatesItsVariableTable() {
        StringOp original = new StringOp(); original.dayInfo.put("value", "10");
        StringOp clone = original.clone(); clone.dayInfo.put("value", "20"); clone.dayInfo.put("extra", "5");
        assertEquals(10.0, original.eval("value"));
        assertEquals(20.0, clone.eval("value"));
        assertFalse(original.dayInfo.containsKey("extra"));
    }
}
