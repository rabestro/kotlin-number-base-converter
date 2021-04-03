import org.hyperskill.hstest.dynamic.DynamicTest;
import org.hyperskill.hstest.exception.outcomes.WrongAnswer;
import org.hyperskill.hstest.stage.StageTest;
import org.hyperskill.hstest.testcase.CheckResult;
import org.hyperskill.hstest.testing.TestedProgram;

import java.text.MessageFormat;

public class ConverterTest extends StageTest {

    @DynamicTest(order = 10)
    CheckResult testFirstMessageAndExit() {
        final var main = new TestedProgram();
        var output = main.start().toLowerCase();

        require(output.contains("source base") && output.contains("target base") && output.contains("/exit"),
                "Your program should output the message \"Enter two numbers in format:"
                        + " {source base} {target base} (To quit type /exit)\" when it starts");

        main.execute("/exit");
        require(main.isFinished(), "Your program should terminate when the user enters \"/exit\"");
        return CheckResult.correct();
    }

    @DynamicTest(order = 20)
    CheckResult testOutputFormat() {
        final var main = new TestedProgram();
        main.start();
        var output = main.execute("10 2").toLowerCase();

        require(output.contains("base 10") && output.contains("convert to base 2"),
                "Your program should prompt the user for the number to be " +
                        "converted with the message \"Enter number in base " +
                        "{user source base} to convert to base {user target base}" +
                        " (To go back type /back)\" after accepting the " +
                        "source and target base");

        require(output.contains("/back"),
                "Your program should provide the user with an option to go " +
                        "back to the top-level menu with the message \"Enter number in base " +
                        "{user source base} to convert to base {user target base} " +
                        "(To go back type /back)\"");

//        main.execute("/exit");
//        require(main.isFinished(), "Your program should terminate when the user enters \"/exit\"");
        return CheckResult.correct();
    }


    private static void require(final boolean condition, final String error, final Object... args) {
        if (!condition) {
            final var feedback = MessageFormat.format(error, args);
            throw new WrongAnswer(feedback);
        }
    }
}
