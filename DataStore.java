import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * All file input/output lives here, so the service classes never touch
 * java.io directly. Data files are created automatically on first run.
 */
public class DataStore {

    public static final String DATA_DIR = "data";
    public static final String RECEIPT_DIR = "receipts";

    public static final String PRODUCTS_FILE = DATA_DIR + File.separator + "products.csv";
    public static final String USERS_FILE = DATA_DIR + File.separator + "users.csv";
    public static final String ORDERS_FILE = DATA_DIR + File.separator + "orders.csv";

    /** Makes sure the data and receipts folders exist. */
    public static void init() {
        new File(DATA_DIR).mkdirs();
        new File(RECEIPT_DIR).mkdirs();
    }

    /** Reads every non-empty, non-comment line of a file. Missing file = empty list. */
    public static List<String> readLines(String path) {
        List<String> lines = new ArrayList<>();
        File file = new File(path);
        if (!file.exists()) {
            return lines;
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    lines.add(line);
                }
            }
        } catch (IOException e) {
            System.out.println("  ! Could not read " + path + ": " + e.getMessage());
        }
        return lines;
    }

    /** Overwrites a file with the given lines. */
    public static void writeLines(String path, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("  ! Could not write " + path + ": " + e.getMessage());
        }
    }

    /** Saves a plain-text receipt into the receipts folder. */
    public static void writeReceipt(String orderId, String content) {
        String path = RECEIPT_DIR + File.separator + orderId + ".txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(content);
        } catch (IOException e) {
            System.out.println("  ! Could not save receipt: " + e.getMessage());
        }
    }

    /**
     * Removes the characters used as delimiters so a user cannot break the CSV
     * format by typing a comma or colon inside a product name.
     */
    public static String sanitize(String text) {
        if (text == null) {
            return "";
        }
        return text.replace(",", " ")
                   .replace(":", " ")
                   .replace("|", " ")
                   .trim();
    }
}
