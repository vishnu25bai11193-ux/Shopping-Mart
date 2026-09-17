import java.util.Scanner;

/**
 * Wraps Scanner so that bad input never crashes the program.
 * One shared Scanner is used for the whole application.
 */
public class InputHelper {

    private static final Scanner SCANNER = new Scanner(System.in);

    private InputHelper() {
        // utility class
    }

    public static String readLine(String prompt) {
        System.out.print(prompt);
        return SCANNER.nextLine().trim();
    }

    /** Keeps asking until the user types something that is not blank. */
    public static String readNonEmpty(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("  ! This field cannot be empty.");
        }
    }

    /** Keeps asking until a whole number inside [min, max] is entered. */
    public static int readInt(String prompt, int min, int max) {
        while (true) {
            String raw = readLine(prompt);
            try {
                int value = Integer.parseInt(raw);
                if (value < min || value > max) {
                    System.out.println("  ! Enter a number between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  ! '" + raw + "' is not a valid number.");
            }
        }
    }

    public static double readDouble(String prompt, double min, double max) {
        while (true) {
            String raw = readLine(prompt);
            try {
                double value = Double.parseDouble(raw);
                if (value < min || value > max) {
                    System.out.println("  ! Enter a value between " + min + " and " + max + ".");
                    continue;
                }
                return value;
            } catch (NumberFormatException e) {
                System.out.println("  ! '" + raw + "' is not a valid amount.");
            }
        }
    }

    public static boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (y/n): ").toLowerCase();
            if (value.equals("y") || value.equals("yes")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            System.out.println("  ! Please answer y or n.");
        }
    }

    public static void pause() {
        System.out.print("\nPress Enter to continue...");
        SCANNER.nextLine();
    }

    /** Prints a simple plain-text banner. */
    public static void header(String title) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println("  " + title);
        System.out.println("==================================================");
    }

    public static void line() {
        System.out.println("--------------------------------------------------");
    }

    public static String money(double amount) {
        return String.format("Rs.%.2f", amount);
    }
}
