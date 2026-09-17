/**
 * Represents a single product in the mart.
 * Stored in data/products.csv as: id,name,category,price,stock,gstRate
 */
public class Product {

    private int id;
    private String name;
    private String category;
    private double price;
    private int stock;
    private double gstRate; // e.g. 0.05 = 5%

    public Product(int id, String name, String category, double price, int stock, double gstRate) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = price;
        this.stock = stock;
        this.gstRate = gstRate;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public double getGstRate() { return gstRate; }

    public void setName(String name) { this.name = name; }
    public void setCategory(String category) { this.category = category; }
    public void setPrice(double price) { this.price = price; }
    public void setStock(int stock) { this.stock = stock; }
    public void setGstRate(double gstRate) { this.gstRate = gstRate; }

    public boolean isInStock() { return stock > 0; }

    /** Converts this product into one CSV line. */
    public String toCsv() {
        return id + "," + name + "," + category + "," + price + "," + stock + "," + gstRate;
    }

    /** Rebuilds a product from one CSV line. Returns null if the line is malformed. */
    public static Product fromCsv(String line) {
        try {
            String[] parts = line.split(",", -1);
            if (parts.length < 6) {
                return null;
            }
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String category = parts[2].trim();
            double price = Double.parseDouble(parts[3].trim());
            int stock = Integer.parseInt(parts[4].trim());
            double gstRate = Double.parseDouble(parts[5].trim());
            return new Product(id, name, category, price, stock, gstRate);
        } catch (NumberFormatException e) {
            System.out.println("  ! Skipping corrupt product record: " + line);
            return null;
        }
    }

    @Override
    public String toString() {
        return String.format("#%d %s (%s) - Rs.%.2f", id, name, category, price);
    }
}
