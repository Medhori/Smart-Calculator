package calculator;

import static calculator.Service.*;

public class Application {
    public void run() {
        while (true) {
            String input = scanner.nextLine();

            if (input.isEmpty()) {
                continue;
            }
            if (input.equals(EXIT_COMMAND)) {
                System.out.println("Bye!");
                break;
            } else if (input.equals(HELP_COMMAND)) {
                System.out.println("This is a smart calculator");
            } else if (input.matches(COMMAND_REGEX)) {
                System.out.println("Unknown command");
            } else {
                String result = getResult(input);
                if (!result.isEmpty()) {
                    System.out.println(result);
                }
            }
        }
    }
}
