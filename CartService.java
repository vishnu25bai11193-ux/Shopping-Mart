import java.util.ArrayList;
import java.util.List;

/**
 * The shopping cart of the currently logged-in customer.
 * Cleared on logout and after a successful checkout.
 */
public class CartService {

    private List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return items;
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public void clear() {
        items.clear();
    }

    private CartItem findItem(int productId) {
        for (CartItem item : items) {
            if (item.getProduct().getId() == productId) {
                return item;
            }
        }
        return null;
    }

    /**
     * Adds a product, merging with an existing line if it is already in the cart.
     * Returns false when the requested quantity exceeds available stock.
     */
    public boolean add(Product product, int quantity) {
        CartItem existing = findItem(product.getId());
        int alreadyInCart = (existing == null) ? 0 : existing.getQuantity();
        if (alreadyInCart + quantity > product.getStock()) {
            return false;
        }
        if (existing == null) {
            items.add(new CartItem(product, quantity));
        } else {
            existing.addQuantity(quantity);
        }
        return true;
    }

    /** Sets a new quantity; a quantity of 0 removes the line. */
    public boolean updateQuantity(int productId, int quantity) {
        CartItem item = findItem(productId);
        if (item == null) {
            return false;
        }
        if (quantity == 0) {
            items.remove(item);
            return true;
        }
        if (quantity > item.getProduct().getStock()) {
            return false;
        }
        item.setQuantity(quantity);
        return true;
    }

    public boolean remove(int productId) {
        CartItem item = findItem(productId);
        if (item == null) {
            return false;
        }
        items.remove(item);
        return true;
    }

    public double getSubtotal() {
        double sum = 0;
        for (CartItem item : items) {
            sum += item.getSubtotal();
        }
        return sum;
    }

    public double getGstTotal() {
        double sum = 0;
        for (CartItem item : items) {
            sum += item.getGstAmount();
        }
        return sum;
    }

    public double getGrandTotal() {
        return getSubtotal() + getGstTotal();
    }

    /** Prints the cart as a plain-text bill preview. */
    public void print() {
        if (items.isEmpty()) {
            System.out.println("  Your cart is empty.");
            return;
        }
        System.out.printf("%-5s %-26s %5s %10s %9s %11s%n",
                "ID", "PRODUCT", "QTY", "RATE", "GST", "TOTAL");
        InputHelper.line();
        for (CartItem item : items) {
            System.out.printf("%-5d %-26s %5d %10s %9s %11s%n",
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    item.getQuantity(),
                    InputHelper.money(item.getProduct().getPrice()),
                    InputHelper.money(item.getGstAmount()),
                    InputHelper.money(item.getLineTotal()));
        }
        InputHelper.line();
        System.out.printf("%56s %11s%n", "Subtotal:", InputHelper.money(getSubtotal()));
        System.out.printf("%56s %11s%n", "GST:", InputHelper.money(getGstTotal()));
        System.out.printf("%56s %11s%n", "GRAND TOTAL:", InputHelper.money(getGrandTotal()));
    }
}
