/**
 * Campus Shopping Mart - console based retail billing system.
 *
 * Compile : javac -d out src/*.java
 * Run     : java -cp out Main
 */
public class Main {

    public static void main(String[] args) {
        try {
            new ShoppingMartApp().run();
        } catch (Exception e) {
            System.out.println("\n  ! Unexpected error: " + e.getMessage());
            System.out.println("  The program will now close.");
        }
    }
}
