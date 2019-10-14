package so;

import java.util.Stack;

public class SimpleCalculator {

    static int precedence(char c) {
        switch (c) {
            case '+':
            case '-':
                return 1;
            case '*':
            case '/':
                return 2;
            case '^':
                return 3;
        }
        return -1;
    }

    public static int evaluate(int s1, int s2, char operator) {
        switch (operator) {
            case '+':
                return (s1 + s2);
            case '-':
                return (s2 - s1);
            case '*':
                return (s1 * s2);
            case '/':
                if (s1 == 0)
                    throw new
                            UnsupportedOperationException("Cannot divide by zero");
                return (s2 / s1);
        }
        return 0;
    }

    static String[] infixToPostFix(String[] exp) {

        String[] result = {};
        String result1 = " ";
        Stack<Character> stack = new Stack<>();
        for (int i = 0; i < exp.length; i++) {
            String a = exp[i];
            char c = a.charAt(0);
            if (precedence(c) > 0) {
                while (stack.isEmpty() == false && precedence(stack.peek()) >= precedence(c)) {
                    result1 += stack.pop();
                }
                stack.push(c);
            } else if (c == ')') {
                c = stack.pop();
                while (c != '(') {
                    result1 += c;
                    c = stack.pop();
                }
            } else if (c == '(') {
                stack.push(c);
            } else {
                result1 += c;
            }
        }
        for (int i = 0; i <= stack.size(); i++) {
            result1 += stack.pop();
        }
        result = result1.split("");
        return result;
    }

    public static int postfixEvaluation(String[] exp) {

        Stack<Integer> stack = new Stack<>();
        for (int i = 0; i < exp.length; i++) {
            String a = exp[i];
            char c = a.charAt(0);
            if (c == '*' || c == '/' || c == '^' || c == '+' || c == '-') {
                int s1 = stack.peek();
                int s2 = stack.peek();
                int temp = evaluate(s1, s2, c);
                stack.push(temp);
            } else {
                stack.push((c - '0'));
            }
        }

        int result = stack.pop();
        return result;
    }

    public static void main(String[] args) {
        String[] exp = {"3", "*", "(", "5", "+", "8", ")"};
        System.out.print("Infix Expression: ");
        int j = exp.length + 1;
        for (int i = 0; i < exp.length; i++) {
            System.out.print(exp[i]);
        }
        System.out.println();
        System.out.print("Postfix Expression: ");
        for (int i = 0; i < j; i++) {
            System.out.print(infixToPostFix(exp)[i]);
        }
        System.out.println();
        System.out.println(" Postfix Evaluation: " + postfixEvaluation(exp));
    }
}