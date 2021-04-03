import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class ConverterTest extends StageTest {
    private static final Pattern RESULT_PATTERN = Pattern.compile(
            "Conversion result:\\s*(?<number>.*?)\\s*", Pattern.CASE_INSENSITIVE);

    @DynamicTest(order = 10)
    CheckResult testFirstMessageAndExit() {
        final var main = new TestedProgram();
        var output = main.start().toLowerCase();

        require(output.contains("source base")
                && output.contains("target base") && output.contains("/exit"), "" +
                "Your program should output the message \"Enter two numbers in format: " +
                "{source base} {target base} (To quit type /exit)\" when it starts");

        main.execute("/exit");
        require(main.isFinished(), "Your program should terminate when the user enters \"/exit\"");
        return CheckResult.correct();
    }

    class TestCase {
        final int sourceBase;
        final int targetBase;
        final List<String[]> cases;

        TestCase(int from, int to, String[][] testCases) {
            sourceBase = from;
            targetBase = to;
            cases = Arrays.asList(testCases);
        }
    }

    private final TestCase[] testCases = new TestCase[]{
            new TestCase(10, 2, new String[][]{
                    {"12345", "11000000111001"}})

    };

    @DynamicTest(order = 20, data = "testCases")
    CheckResult testOutputFormat(final TestCase test) {
        final var sourceBase = test.sourceBase;
        final var targetBase = test.targetBase;

        final var main = new TestedProgram();
        main.start();
        var output = main.execute(sourceBase + " " + targetBase).toLowerCase();

        for (var data: test.cases) {
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

            final var sourceNumber = data[0];
            final var expectedResult = data[1];
            output = main.execute(sourceNumber);
            final var lines = output.lines().toArray(String[]::new);

            require(lines.length > 1, "" +
                    "Your program should print at least two lines: " +
                    "the conversion result and the prompt.");

            final var result = RESULT_PATTERN.matcher(lines[0]);

            require(result.matches(), "" +
                    "Your program should print the conversion result in the " +
                    "format \"Conversion result: CONVERTED_NUMBER\"");

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

        main.execute("/back");
        main.execute("/exit");
        require(main.isFinished(), "Your program should terminate when the user enters \"/exit\"");
        return CheckResult.correct();
    }


    private static void require(final boolean condition, final String error, final Object... args) {
        if (!condition) {
            final var feedback = MessageFormat.format(error, args);
            throw new WrongAnswer(feedback);
        }
    }
}
