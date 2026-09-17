import java.util.ArrayList;
import java.util.List;

/**
 * A completed purchase. Line items are frozen snapshots: if the admin later
 * changes a product's price, old orders must not change.
 *
 * CSV layout (data/orders.csv):
 *   orderId,username,dateTime,subtotal,gstTotal,grandTotal,lineItems
 * where lineItems is "pid:name:qty:unitPrice:gstRate|pid:name:qty:..."
 */
public class Order {

    /** A frozen copy of one purchased product. */
    public static class OrderLine {
        private int productId;
        private String productName;
        private int quantity;
        private double unitPrice;
        private double gstRate;

        public OrderLine(int productId, String productName, int quantity, double unitPrice, double gstRate) {
            this.productId = productId;
            this.productName = productName;
            this.quantity = quantity;
            this.unitPrice = unitPrice;
            this.gstRate = gstRate;
        }

        public int getProductId() { return productId; }
        public String getProductName() { return productName; }
        public int getQuantity() { return quantity; }
        public double getUnitPrice() { return unitPrice; }
        public double getGstRate() { return gstRate; }

        public double getSubtotal() { return unitPrice * quantity; }
        public double getGstAmount() { return getSubtotal() * gstRate; }
        public double getLineTotal() { return getSubtotal() + getGstAmount(); }

        public String encode() {
            return productId + ":" + productName + ":" + quantity + ":" + unitPrice + ":" + gstRate;
        }

        public static OrderLine decode(String token) {
            String[] p = token.split(":", -1);
            if (p.length < 5) {
                return null;
            }
            return new OrderLine(
                    Integer.parseInt(p[0].trim()),
                    p[1].trim(),
                    Integer.parseInt(p[2].trim()),
                    Double.parseDouble(p[3].trim()),
                    Double.parseDouble(p[4].trim()));
        }
    }

    private String orderId;
    private String username;
    private String dateTime;
    private List<OrderLine> lines;

    public Order(String orderId, String username, String dateTime, List<OrderLine> lines) {
        this.orderId = orderId;
        this.username = username;
        this.dateTime = dateTime;
        this.lines = lines;
    }

    public String getOrderId() { return orderId; }
    public String getUsername() { return username; }
    public String getDateTime() { return dateTime; }
    public List<OrderLine> getLines() { return lines; }

    public double getSubtotal() {
        double sum = 0;
        for (OrderLine line : lines) {
            sum += line.getSubtotal();
        }
        return sum;
    }

    public double getGstTotal() {
        double sum = 0;
        for (OrderLine line : lines) {
            sum += line.getGstAmount();
        }
        return sum;
    }

    public double getGrandTotal() {
        return getSubtotal() + getGstTotal();
    }

    public int getTotalItems() {
        int count = 0;
        for (OrderLine line : lines) {
            count += line.getQuantity();
        }
        return count;
    }

    public String toCsv() {
        StringBuilder encoded = new StringBuilder();
        for (int i = 0; i < lines.size(); i++) {
            if (i > 0) {
                encoded.append("|");
            }
            encoded.append(lines.get(i).encode());
        }
        return orderId + "," + username + "," + dateTime + ","
                + String.format("%.2f", getSubtotal()) + ","
                + String.format("%.2f", getGstTotal()) + ","
                + String.format("%.2f", getGrandTotal()) + ","
                + encoded;
    }

    public static Order fromCsv(String line) {
        try {
            String[] parts = line.split(",", 7);
            if (parts.length < 7) {
                return null;
            }
            List<OrderLine> lines = new ArrayList<>();
            String[] tokens = parts[6].split("\\|");
            for (String token : tokens) {
                if (token.trim().isEmpty()) {
                    continue;
                }
                OrderLine ol = OrderLine.decode(token);
                if (ol != null) {
                    lines.add(ol);
                }
            }
            return new Order(parts[0].trim(), parts[1].trim(), parts[2].trim(), lines);
        } catch (NumberFormatException e) {
            System.out.println("  ! Skipping corrupt order record.");
            return null;
        }
    }
}
