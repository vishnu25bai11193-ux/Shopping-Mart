/**
 * One line inside a customer's shopping cart.
 * The cart lives only in memory during a session; it becomes an Order at checkout.
 */
public class CartItem {

    private Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
    public void addQuantity(int extra) { this.quantity += extra; }

    /** Price before tax. */
    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    /** GST amount for this line only. */
    public double getGstAmount() {
        return getSubtotal() * product.getGstRate();
    }

    public double getLineTotal() {
        return getSubtotal() + getGstAmount();
    }
}
