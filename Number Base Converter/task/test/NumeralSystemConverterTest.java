import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.text.MessageFormat;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class NumeralSystemConverterTest extends StageTest {
    private static final Random random = new Random();
    private static final int MAX_SOURCE_WHOLE_LENGTH = 20;
    private static final int MAX_SOURCE_FRACTION_LENGTH = 10;
    private static final int MAX_TARGET_FRACTION_LENGTH = 5;
    private static final int MAX_FRACTION_COMPARE = MAX_TARGET_FRACTION_LENGTH - 1;
    private static final int SCALE = 10;
    private static final String DIGITS = "0123456789abcdefghijklmnopqrstuvwxyz";
    private static final int MINIMUM_BASE = 2;
    private static final int MAXIMUM_BASE = DIGITS.length();
    private static final int MINIMUM_TESTS = 1;
    private static final int MAXIMUM_TESTS = 10;
    private static final Pattern DOT_PATTERN = Pattern.compile(".", Pattern.LITERAL);
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "Conversion result:\\s*(?<number>.*?)\\s*", Pattern.CASE_INSENSITIVE);

    private static int getRandomBase() {
        return MINIMUM_BASE + random.nextInt(MAXIMUM_BASE - MINIMUM_BASE);
    }

    private static String generateSourceNumber(final int radix) {
        final var sourceWhole = random
                .ints(1 + random.nextInt(MAX_SOURCE_WHOLE_LENGTH), 0, radix)
                .map(DIGITS::charAt)
                .mapToObj(Character::toString)
                .collect(Collectors.joining());

        if (random.nextBoolean()) {
            return sourceWhole;
        }

        final var sourceFraction = random
                .ints(1 + random.nextInt(MAX_SOURCE_FRACTION_LENGTH), 0, radix)
                .map(DIGITS::charAt)
                .mapToObj(Character::toString)
                .collect(Collectors.joining());

        return sourceWhole + '.' + sourceFraction;
    }

    private static String convert(String sourceNumber, int sourceBase, int targetBase) {
        if (!sourceNumber.contains(".")) {
            return sourceBase == targetBase
                    ? sourceNumber
                    : new BigInteger(sourceNumber, sourceBase).toString(targetBase);
        }
        final var numberParts = DOT_PATTERN.split(sourceNumber);
        if (sourceBase == targetBase) {
            return numberParts[0] + '.' + (numberParts[1] + "00000").substring(0, 5);
        }
        final var targetWhole = new BigInteger(numberParts[0], sourceBase).toString(targetBase);
        final var sourceFraction = numberParts[1].chars().map(DIGITS::indexOf).toArray();

        var fraction = BigDecimal.ZERO.setScale(SCALE, RoundingMode.HALF_EVEN);
        var divider = new BigDecimal(sourceBase).setScale(SCALE, RoundingMode.HALF_EVEN);
        var bigSourceBase = new BigDecimal(sourceBase).setScale(SCALE, RoundingMode.HALF_EVEN);
        var bigTargetBase = new BigDecimal(targetBase).setScale(SCALE, RoundingMode.HALF_EVEN);

        for (final int digit : sourceFraction) {
            var delta = new BigDecimal(digit)
                    .setScale(SCALE, RoundingMode.HALF_EVEN)
                    .divide(divider, RoundingMode.HALF_EVEN);
            fraction = fraction.add(delta);
            divider = divider.multiply(bigSourceBase);
        }
        var targetFraction = new StringBuilder();
        for (int i = 0; i < MAX_TARGET_FRACTION_LENGTH * 2; ++i) {
            fraction = fraction.multiply(bigTargetBase);
            final int index = fraction.intValue();
            targetFraction.append(DIGITS.charAt(index));
            fraction = fraction.subtract(new BigDecimal(index));
        }
        return targetWhole + '.' + targetFraction.substring(0, MAX_TARGET_FRACTION_LENGTH);
    }

    private static void require(final boolean condition, final String error, final Object... args) {
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

        int testCases = 20;
        while (testCases-- > 0) {
            testConversion(main, getRandomBase(), getRandomBase());
        }

        main.execute("/exit");
        require(main.isFinished(), "Your program should terminate when the user enters \"/exit\"");
        return CheckResult.correct();
    }

    private void testConversion(TestedProgram main, int sourceBase, int targetBase) {
        var output = main.execute(sourceBase + " " + targetBase).toLowerCase();
        int count = MINIMUM_TESTS + random.nextInt(MAXIMUM_TESTS - MINIMUM_TESTS + 1);
        while (count-- > 0) {
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

            final var sourceNumber = generateSourceNumber(sourceBase);
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

            requireEqual(expectedResult, actualResult);

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

    private void requireEqual(final String expectedNumber, final String actualNumber) {
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

        require(expectedFraction.substring(0, MAX_FRACTION_COMPARE).equalsIgnoreCase(
                actualFraction.substring(0, MAX_FRACTION_COMPARE)), "" +
                "The actual result {0} is not equal to expected {1}. " +
                "A whole part of the numbers is equal, but the fractional part is different." +
                "The first {2} digits of fractional part doesn''t equals to expected digits." +
                " The expected factional part is {3} but your actual fractional part is {4}.",
                expectedNumber, actualNumber, MAX_FRACTION_COMPARE, expectedFraction, actualFraction);
    }
}


