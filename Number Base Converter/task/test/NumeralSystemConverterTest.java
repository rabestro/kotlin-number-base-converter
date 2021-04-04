import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;
import utils.Numbers;

import java.math.BigDecimal;
import java.math.MathContext;
import java.text.MessageFormat;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import static java.math.MathContext.DECIMAL32;
import static utils.Numbers.*;

public class NumeralSystemConverterTest extends StageTest {
    private static final int CONVERSION_TESTS_NUMBER = 10;
    private static final int BASE_TESTS_NUMBER = 30;
    private static final BigDecimal DELTA = new BigDecimal("0.00001", new MathContext(5));

    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "Conversion result:\\s*(?<number>.*?)\\s*", Pattern.CASE_INSENSITIVE);

    static void require(final boolean condition, final String error, final Object... args) {
        if (!condition) {
            final var feedback = MessageFormat.format(error, args);
            throw new WrongAnswer(feedback);
        }
    }

    @DynamicTest()
    CheckResult testOutputFormat() {
        final var main = new TestedProgram();
        final var output = main.start().toLowerCase();
        require(output.contains("source base")
                && output.contains("target base") && output.contains("/exit"), "" +
                "Your program should output the message \"Enter two numbers in format: " +
                "{source base} {target base} (To quit type /exit)\" when it starts");

        IntStream.range(0, BASE_TESTS_NUMBER).forEach(i -> testConversion(main));

        main.execute("/exit");
        require(main.isFinished(), "Your program should terminate when the user enters \"/exit\"");
        return CheckResult.correct();
    }

    private void testConversion(final TestedProgram main) {
        final int sourceBase = getRandomBase();
        final int targetBase = getRandomBase();
        var output = main.execute(sourceBase + " " + targetBase).toLowerCase();
        int testCount = CONVERSION_TESTS_NUMBER;
        while (testCount-- > 0) {
            require(output.contains("base " + sourceBase)
                    && output.contains("convert to base " + targetBase), "" +
                    "Your program should prompt the user for the number to be " +
                    "converted with the message \"Enter number in base " +
                    "{0} to convert to base {1}" +
                    " (To go back type /back)\" after accepting the " +
                    "source and target base", sourceBase, targetBase);

            require(output.contains("/back"), "" +
                    "Your program should provide the user with an option to go " +
                    "back to the top-level menu with the message \"Enter number in base " +
                    "{0} to convert to base {1} " +
                    "(To go back type /back)\"", sourceBase, targetBase);

            final var sourceNumber = Numbers.generateNumber(sourceBase);
            output = main.execute(sourceNumber);
            final var lines = output.lines().toArray(String[]::new);

            require(lines.length > 1, "" +
                    "Your program should print at least two lines: " +
                    "the conversion result and the prompt.");

            final var result = RESULT_PATTERN.matcher(lines[0]);

            require(result.matches(), "" +
                    "Your program should print the conversion result in the " +
                    "format \"Conversion result: CONVERTED_NUMBER\"");

            final var expectedResult = convert(sourceNumber, sourceBase, targetBase);
            final var actualResult = result.group("number");

            require(!actualResult.isBlank(), "You program didn't print the converted number");

            requireEqual(expectedResult, actualResult, targetBase);

            require(!main.isFinished(), "" +
                    "Your program should not terminate until the user enter " +
                    "\"/exit\" in the top-level menu");

            require(!output.contains("/exit"), "" +
                    "Your program should remember the user's source and target " +
                    "base and should not return to the top-level menu " +
                    "until the user enters \"/back\"");
        }

        require(main.execute("/back").toLowerCase().contains("/exit"), "" +
                "Your program should take the user back to the top-level " +
                "menu when they enter \"/back\"");
    }

    private void requireEqual(final String expectedNumber, final String actualNumber, final int radix) {
        final var expected = DOT_PATTERN.split(expectedNumber);
        final var actual = DOT_PATTERN.split(actualNumber);

        require(actual.length > 0, "" +
                        "The conversion result of your program is wrong. " +
                        "the expected result is {0}. Your actual result is {1}.",
                expectedNumber, actualNumber);

        final var expectedWholeNumber = expected[0];
        final var actualWholeNumber = actual[0];

        require(expectedWholeNumber.equalsIgnoreCase(actualWholeNumber), "" +
                        "The conversion result of your program is wrong. " +
                        "the expected whole part is {0}. Your actual whole part is {1}.",
                expectedWholeNumber, actualWholeNumber);

        if (expected.length < 2) {
            require(actual.length == 1, "" +
                            "When converting whole numbers the fractional part is not expected in the result. " +
                            "Expected result is {0} but your actual result is {1}.",
                    expectedNumber, actualNumber);
            return;
        }

        final var expectedFraction = expected[1];
        final var actualFraction = actual[1];

        require(actualFraction.length() == 5, "" +
                        "The fractional part of your conversion should only " +
                        "be 5 digits in length. Expected fractional part: {0} Actual fractional part: {1}",
                expectedFraction, actualFraction);

        if (actualFraction.equalsIgnoreCase(expectedFraction)) {
            return;
        }
        final var actualDecimalFraction = convertFractionToDecimal(actualFraction, radix);
        final var expectedDecimalFraction = convertFractionToDecimal(expectedFraction, radix);
        final var difference = expectedDecimalFraction.subtract(actualDecimalFraction, DECIMAL32).abs();

        require(difference.compareTo(DELTA) < 0, "" +
                        "The actual result {1} is not equal to expected {0}. " +
                        "A whole part of the numbers is equal, but the fractional part is different. " +
                        "The decimal difference of fractional parts is {2} and this is bigger then delta {3}. " +
                        "Expected decimal fraction: {4} actual: {5}. ",
                expectedNumber, actualNumber,
                difference.toString(), DELTA.toString(),
                expectedDecimalFraction.toString(),
                actualDecimalFraction.toString());
    }
}