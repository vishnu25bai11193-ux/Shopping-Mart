import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Turns a cart into a saved Order, writes the receipt file and produces the
 * admin sales report.
 */
public class OrderService {

    private List<Order> orders = new ArrayList<>();
    private ProductService productService;

    public OrderService(ProductService productService) {
        this.productService = productService;
        load();
    }

    private void load() {
        orders.clear();
        for (String line : DataStore.readLines(DataStore.ORDERS_FILE)) {
            Order order = Order.fromCsv(line);
            if (order != null) {
                orders.add(order);
            }
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (Order order : orders) {
            lines.add(order.toCsv());
        }
        DataStore.writeLines(DataStore.ORDERS_FILE, lines);
    }

    private String nextOrderId() {
        return String.format("ORD%04d", orders.size() + 1);
    }

    private String timestamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

    /**
     * Checkout. Verifies stock again (it may have changed), reduces inventory,
     * stores the order and writes a receipt. Returns null if stock ran out.
     */
    public Order placeOrder(User user, CartService cart) {
        for (CartItem item : cart.getItems()) {
            Product live = productService.findById(item.getProduct().getId());
            if (live == null || live.getStock() < item.getQuantity()) {
                System.out.println("  ! Not enough stock for: " + item.getProduct().getName());
                return null;
            }
        }

        List<Order.OrderLine> lines = new ArrayList<>();
        for (CartItem item : cart.getItems()) {
            lines.add(new Order.OrderLine(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getProduct().getGstRate()));
            productService.reduceStock(item.getProduct().getId(), item.getQuantity());
        }
        productService.save();

        Order order = new Order(nextOrderId(), user.getUsername(), timestamp(), lines);
        orders.add(order);
        save();

        String receipt = buildReceipt(order, user);
        DataStore.writeReceipt(order.getOrderId(), receipt);
        System.out.println(receipt);
        System.out.println("  Receipt saved to receipts/" + order.getOrderId() + ".txt");

        return order;
    }

    /** Builds the printable bill text. */
    public String buildReceipt(Order order, User user) {
        StringBuilder sb = new StringBuilder();
        sb.append("\n==================================================\n");
        sb.append("             CAMPUS SHOPPING MART\n");
        sb.append("             TAX INVOICE / RECEIPT\n");
        sb.append("==================================================\n");
        sb.append("Order ID : ").append(order.getOrderId()).append("\n");
        sb.append("Customer : ").append(user == null ? order.getUsername() : user.getFullName())
          .append(" (").append(order.getUsername()).append(")\n");
        sb.append("Date     : ").append(order.getDateTime()).append("\n");
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%-22s %4s %9s %11s%n", "ITEM", "QTY", "RATE", "AMOUNT"));
        sb.append("--------------------------------------------------\n");
        for (Order.OrderLine line : order.getLines()) {
            String name = line.getProductName();
            if (name.length() > 22) {
                name = name.substring(0, 21) + ".";
            }
            sb.append(String.format("%-22s %4d %9s %11s%n",
                    name,
                    line.getQuantity(),
                    InputHelper.money(line.getUnitPrice()),
                    InputHelper.money(line.getSubtotal())));
        }
        sb.append("--------------------------------------------------\n");
        sb.append(String.format("%37s %11s%n", "Subtotal:", InputHelper.money(order.getSubtotal())));
        sb.append(String.format("%37s %11s%n", "GST:", InputHelper.money(order.getGstTotal())));
        sb.append(String.format("%37s %11s%n", "GRAND TOTAL:", InputHelper.money(order.getGrandTotal())));
        sb.append("==================================================\n");
        sb.append("        Thank you for shopping with us!\n");
        sb.append("==================================================\n");
        return sb.toString();
    }

    public List<Order> getAll() {
        return orders;
    }

    public List<Order> getOrdersOf(String username) {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getUsername().equalsIgnoreCase(username)) {
                result.add(order);
            }
        }
        return result;
    }

    public Order findById(String orderId) {
        for (Order order : orders) {
            if (order.getOrderId().equalsIgnoreCase(orderId)) {
                return order;
            }
        }
        return null;
    }

    public static void printOrderTable(List<Order> list) {
        if (list.isEmpty()) {
            System.out.println("  No orders found.");
            return;
        }
        System.out.printf("%-10s %-14s %-21s %6s %12s%n",
                "ORDER ID", "CUSTOMER", "DATE", "ITEMS", "TOTAL");
        InputHelper.line();
        for (Order order : list) {
            System.out.printf("%-10s %-14s %-21s %6d %12s%n",
                    order.getOrderId(),
                    order.getUsername(),
                    order.getDateTime(),
                    order.getTotalItems(),
                    InputHelper.money(order.getGrandTotal()));
        }
        InputHelper.line();
    }

    /** Admin report: revenue, tax collected and best-selling products. */
    public void printSalesReport() {
        InputHelper.header("SALES REPORT");
        if (orders.isEmpty()) {
            System.out.println("  No sales recorded yet.");
            return;
        }
        double revenue = 0;
        double tax = 0;
        int itemCount = 0;
        List<String> names = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();

        for (Order order : orders) {
            revenue += order.getGrandTotal();
            tax += order.getGstTotal();
            itemCount += order.getTotalItems();
            for (Order.OrderLine line : order.getLines()) {
                int index = names.indexOf(line.getProductName());
                if (index == -1) {
                    names.add(line.getProductName());
                    quantities.add(line.getQuantity());
                } else {
                    quantities.set(index, quantities.get(index) + line.getQuantity());
                }
            }
        }

        System.out.println("  Total orders     : " + orders.size());
        System.out.println("  Items sold       : " + itemCount);
        System.out.println("  GST collected    : " + InputHelper.money(tax));
        System.out.println("  Total revenue    : " + InputHelper.money(revenue));
        System.out.println("  Average order    : " + InputHelper.money(revenue / orders.size()));
        InputHelper.line();
        System.out.println("  BEST SELLERS");

        // Simple selection sort on the parallel lists (top 5).
        int limit = Math.min(5, names.size());
        for (int i = 0; i < limit; i++) {
            int best = i;
            for (int j = i + 1; j < names.size(); j++) {
                if (quantities.get(j) > quantities.get(best)) {
                    best = j;
                }
            }
            String tempName = names.get(i);
            names.set(i, names.get(best));
            names.set(best, tempName);
            int tempQty = quantities.get(i);
            quantities.set(i, quantities.get(best));
            quantities.set(best, tempQty);

            System.out.printf("   %d. %-28s %4d sold%n", i + 1, names.get(i), quantities.get(i));
        }
    }
}
