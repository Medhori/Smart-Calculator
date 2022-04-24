package calculator;


import java.math.BigInteger;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Service {
    public static final String MINUS_REGEX = "[-]+";
    public static final String ADD_REGEX = "[+]+";
    public static final String DIGIT_REGEX = "[-+]?[0-9]+";
    public static final String HELP_COMMAND = "/help";
    public static final String EXIT_COMMAND = "/exit";
    public static final String COMMAND_REGEX = "^\\/.*";
    public static final String LATIN_LETTERS_REGEX = "[a-zA-Z]+";
    public static final String EQUAL = "=";
    public static Scanner scanner = new Scanner(System.in);
    public static Map<String, String> map = new HashMap<>();

    static String getResult(String input) {
        String result;

        try {
            if (input.contains("=")) {
                doAssignment(input);
                result = "";
            } else {

                String[] postfixExpr = infixToPostfix(input);
                BigInteger intResult = calculateWithPostfix(postfixExpr);
                result = intResult.toString();

            }
        } catch (AppException e) {
            result = e.getMessage();
        }

        return result;
    }

    private static BigInteger calculateWithPostfix(String[] postfixExpr) {
        Deque<BigInteger> stack = new ArrayDeque<>();
        for (String elem : postfixExpr) {
            if (elem.matches(DIGIT_REGEX)) {
                stack.offerLast(new BigInteger(elem));
            } else {
                BigInteger b = stack.pollLast();
                BigInteger a = stack.pollLast();

                if (a == null || b == null) {
                    throw new AppException("Invalid expression");
                }
                BigInteger result;
                switch (elem) {
                    case "+":
                        result = a.add(b);
                        break;
                    case "-":
                        result = a.subtract(b);
                        break;
                    case "*":
                        result = a.multiply(b);
                        break;
                    case "/":
                        result = a.divide(b);
                        break;
                    case "^":
                        result = BigInteger.valueOf((long) Math.pow(a.doubleValue(), b.doubleValue()));
                        break;
                    default:
                        throw new AppException("unknown operator");
                }
                stack.offerLast(result);
            }
        }

        if (stack.size() != 1) {
            throw new AppException("Invalid expression3");
        }

        return stack.pollLast();
    }

    private static String[] infixToPostfix(String infixExpr) {
        List<String> postfixList = new ArrayList<>();
        Deque<String> stack = new ArrayDeque<>();
        var infixList = getValuesExpression(infixExpr);
        String previousElem = "(";
        for (String elem : infixList) {
            if ("-".equals(elem) && "(".equals(previousElem)) {
                postfixList.add("0");
            }
            previousElem = elem;

            if (elem.matches(DIGIT_REGEX)) {
                postfixList.add(elem);

            } else if ("(".equals(elem)) {
                stack.offerLast(elem);
            } else if (")".equals(elem)) {
                while(true) {
                    String previousOperator = stack.pollLast();
                    if (previousOperator == null) {
                        throw new AppException("Invalid expression", " - unbalanced brackets, missing (");
                    } else if ("(".equals(previousOperator)) {
                        break;
                    }
                    postfixList.add(previousOperator);
                }
            } else {
                String previousOperator = stack.peekLast();
                if (previousOperator == null || "(".equals(previousOperator)) {
                    stack.offerLast(elem);
                } else if (precedence(elem) > precedence(previousOperator)) {
                    stack.offerLast(elem);
                } else {
                    do {
                        postfixList.add(stack.pollLast());
                        previousOperator = stack.peekLast();
                    } while (previousOperator != null &&
                            !"(".equals(previousOperator) &&
                            precedence(elem) <= precedence(previousOperator));
                    stack.offerLast(elem);
                }
            }
        }

        while (stack.peekLast() != null) {
            if ("(".equals(stack.peekLast())) {
                throw new AppException("Invalid expression", " - unbalanced brackets, missing )");
            }
            postfixList.add(stack.pollLast());
        }

        return postfixList.toArray(new String[0]);
    }
    private static int precedence(String elem) {
        switch (elem) {
            case "+":
            case "-":
                return 1;
            case "*":
            case "/":
                return 2;
            case "^":
                return 3;
            default:
                throw new AppException("Internal error", " - unknown precedence of (" + elem + ")");
        }
    }
    public static String[] getValuesExpression(String input) {
        var array = getExpression(input);
        for (int i = 0; i < array.length; i++) {

            if (array[i].matches(LATIN_LETTERS_REGEX)) {
                if (map.containsKey(array[i])) {
                    array[i] = map.get(array[i]);
                } else {
                    throw new AppException("Unknown variable");
                }

            } else if (array[i].matches(ADD_REGEX)) {
                array[i] = "+";
            } else if (array[i].matches(MINUS_REGEX)) {
                array[i] = array[i].length() % 2 == 0 ? "+" : "-";
            } else if (array[i].matches("[*]+")
                    || array[i].matches("[\\/]+")
                    || array[i].matches("[\\^]+")) {
                if (array[i].length() > 1) {
                    throw new AppException("Invalid expression");
                }
            }
        }
        return array;
    }
    public static String[] getExpression(String infixExpr) {
        final String regex = "^[-+]*[\\d]+|\\s[-+]\\d+|[a-zA-Z]|[*]+|\\d+|[+]+|[-]+|[\\^]+|[\\/]+|[(]|[)]";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(infixExpr);
        List<String> infixList = new ArrayList<>();

        while (matcher.find()) {
            infixList.add(matcher.group().trim());
        }
        return infixList.toArray(new String[0]);
    }

    private static void doAssignment(String input) {
        if (!isValidIdentifier(input)) {
            throw new AppException("Invalid identifier");
        }

        if (!isValidAssignment(input)) {
            throw new AppException("Invalid assignment");
        }

        if (!isValidVariables(input)) {
            throw new AppException("Unknown variable");
        }
        setValues(input);
    }
    private static void setValues(String input) {
        String key = input.split(EQUAL)[0].trim();
        String value = input.split(EQUAL)[1].trim();
        value = getValue(value);
        map.put(key, value);
    }
    private static String getValue(String value) {
        return isDigit(value) ? value : map.get(value);
    }
    private static boolean isValidVariables(String input) {
        String assignment = input.split(EQUAL, 2)[1].trim();
        return isDigit(assignment) || map.containsKey(assignment);
    }
    private static boolean isValidIdentifier(String input) {
        String identifier = input.split("=")[0].trim();
        return identifier.matches(LATIN_LETTERS_REGEX);
    }
    private static boolean isValidAssignment(String input) {
        String assignment = input.split(EQUAL, 2)[1].trim();
        return (isDigit(assignment) || assignment.matches(LATIN_LETTERS_REGEX))
                && !assignment.contains(EQUAL) && !assignment.contains(" ");
    }
    private static boolean isDigit(String number) {
        return number.matches(DIGIT_REGEX);
    }
    private static boolean isStatement(String input) {
        return input.contains("=");
    }

}
