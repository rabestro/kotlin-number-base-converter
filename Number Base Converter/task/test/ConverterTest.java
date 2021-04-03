import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import java.math.BigInteger;
import java.text.MessageFormat;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ConverterTest extends StageTest {
    private static final Random random = new Random();
    private static final int MAX_SOURCE_WHOLE_LENGTH = 20;
    private static final int MAX_SOURCE_FRACTION_LENGTH = 10;
    private static final int MAX_TARGET_FRACTION_LENGTH = 5;
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
            return new BigInteger(sourceNumber, sourceBase).toString(targetBase);
        }
        final var numberParts = DOT_PATTERN.split(sourceNumber);
        final var targetWhole = new BigInteger(numberParts[0], sourceBase).toString(targetBase);
        final var sourceFraction = numberParts[1].chars().map(DIGITS::indexOf).toArray();

        var fraction = 0.0;
        var divider = (double) sourceBase;
        for (final int digit : sourceFraction) {
            fraction += digit / divider;
            divider *= sourceBase;
        }
        var targetFraction = new StringBuilder();
        for (int i = 0; i < MAX_TARGET_FRACTION_LENGTH; ++i) {
            fraction *= targetBase;
            final int index = (int) fraction;
            targetFraction.append(DIGITS.charAt(index));
            fraction -= index;
        }
        return targetWhole + '.' + targetFraction.toString();
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

    CheckResult testConversion(TestedProgram main, int sourceBase, int targetBase) {
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

            require(expectedResult.equalsIgnoreCase(actualResult), "" +
                            "The conversion result of your program is wrong.\n" +
                            "When converting number {0} from base {1} to base {2} " +
                            "the expected result is {3}. Your actual result is {4}.",
                    sourceNumber, sourceBase, targetBase, expectedResult, actualResult);

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

        return CheckResult.correct();
    }
}
