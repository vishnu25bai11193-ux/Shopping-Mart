import java.util.List;

/**
 * Controls the screen flow: welcome menu -> login -> admin or customer menu.
 */
public class ShoppingMartApp {

    private AuthService authService;
    private ProductService productService;
    private OrderService orderService;
    private CartService cart;
    private User currentUser;

    public ShoppingMartApp() {
        DataStore.init();
        this.authService = new AuthService();
        this.productService = new ProductService();
        this.orderService = new OrderService(productService);
        this.cart = new CartService();
    }

    /* ------------------------------------------------------------------ */
    /*  ENTRY POINT                                                        */
    /* ------------------------------------------------------------------ */

    public void run() {
        System.out.println();
        System.out.println("**************************************************");
        System.out.println("*          CAMPUS SHOPPING MART v1.0             *");
        System.out.println("*        Console Based Retail Billing System     *");
        System.out.println("**************************************************");

        boolean running = true;
        while (running) {
            InputHelper.header("MAIN MENU");
            System.out.println("  1. Login");
            System.out.println("  2. Register as new customer");
            System.out.println("  3. About this system");
            System.out.println("  0. Exit");
            int choice = InputHelper.readInt("\nChoose an option: ", 0, 3);

            switch (choice) {
                case 1:
                    doLogin();
                    break;
                case 2:
                    doRegister();
                    break;
                case 3:
                    showAbout();
                    break;
                case 0:
                    running = false;
                    System.out.println("\n  Goodbye! All data has been saved.\n");
                    break;
                default:
                    break;
            }
        }
    }

    private void showAbout() {
        InputHelper.header("ABOUT");
        System.out.println("  A terminal based shopping mart with role based access,");
        System.out.println("  cart and GST billing, order history and CSV persistence.");
        System.out.println("  Default admin login -> username: admin  password: admin123");
        InputHelper.pause();
    }

    /* ------------------------------------------------------------------ */
    /*  AUTHENTICATION                                                     */
    /* ------------------------------------------------------------------ */

    private void doLogin() {
        InputHelper.header("LOGIN");
        String username = InputHelper.readNonEmpty("Username: ");
        String password = InputHelper.readNonEmpty("Password: ");

        User user = authService.login(username, password);
        if (user == null) {
            System.out.println("\n  ! Invalid username or password.");
            InputHelper.pause();
            return;
        }

        currentUser = user;
        cart.clear();
        System.out.println("\n  Welcome, " + user.getFullName() + "!");

        if (user.isAdmin()) {
            adminMenu();
        } else {
            customerMenu();
        }
    }

    private void doRegister() {
        InputHelper.header("NEW CUSTOMER REGISTRATION");
        String fullName = InputHelper.readNonEmpty("Full name: ");
        String username = InputHelper.readNonEmpty("Choose a username: ");
        String password = InputHelper.readNonEmpty("Choose a password: ");

        User user = authService.register(username, password, fullName);
        if (user == null) {
            System.out.println("\n  ! That username is already taken.");
        } else {
            System.out.println("\n  Account created. You can log in now.");
        }
        InputHelper.pause();
    }

    private void logout() {
        cart.clear();
        currentUser = null;
        System.out.println("\n  Logged out successfully.");
    }

    /* ------------------------------------------------------------------ */
    /*  ADMIN SIDE                                                         */
    /* ------------------------------------------------------------------ */

    private void adminMenu() {
        boolean active = true;
        while (active) {
            InputHelper.header("ADMIN PANEL - " + currentUser.getUsername());
            System.out.println("  1. View all products");
            System.out.println("  2. Add a new product");
            System.out.println("  3. Update a product");
            System.out.println("  4. Delete a product");
            System.out.println("  5. Search / filter products");
            System.out.println("  6. Low stock alert");
            System.out.println("  7. View all orders");
            System.out.println("  8. Sales report");
            System.out.println("  9. View registered users");
            System.out.println("  0. Logout");
            int choice = InputHelper.readInt("\nChoose an option: ", 0, 9);

            switch (choice) {
                case 1:
                    InputHelper.header("PRODUCT INVENTORY");
                    ProductService.printTable(productService.getAll());
                    InputHelper.pause();
                    break;
                case 2:
                    addProduct();
                    break;
                case 3:
                    updateProduct();
                    break;
                case 4:
                    deleteProduct();
                    break;
                case 5:
                    searchMenu();
                    break;
                case 6:
                    lowStockReport();
                    break;
                case 7:
                    InputHelper.header("ALL ORDERS");
                    OrderService.printOrderTable(orderService.getAll());
                    viewOrderDetail();
                    break;
                case 8:
                    orderService.printSalesReport();
                    InputHelper.pause();
                    break;
                case 9:
                    listUsers();
                    break;
                case 0:
                    logout();
                    active = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void addProduct() {
        InputHelper.header("ADD PRODUCT");
        String name = DataStore.sanitize(InputHelper.readNonEmpty("Product name: "));
        String category = DataStore.sanitize(InputHelper.readNonEmpty("Category: "));
        double price = InputHelper.readDouble("Price (Rs.): ", 0.01, 1000000);
        int stock = InputHelper.readInt("Opening stock: ", 0, 100000);
        double gstPercent = InputHelper.readDouble("GST rate in percent (0-28): ", 0, 28);

        Product product = new Product(productService.nextId(), name, category, price, stock, gstPercent / 100.0);
        productService.add(product);
        System.out.println("\n  Product added with ID " + product.getId() + ".");
        InputHelper.pause();
    }

    private void updateProduct() {
        InputHelper.header("UPDATE PRODUCT");
        ProductService.printTable(productService.getAll());
        int id = InputHelper.readInt("\nEnter product ID to update (0 to cancel): ", 0, 999999);
        if (id == 0) {
            return;
        }
        Product product = productService.findById(id);
        if (product == null) {
            System.out.println("  ! No product with that ID.");
            InputHelper.pause();
            return;
        }

        System.out.println("\n  Leave a field blank to keep the current value.");
        String name = InputHelper.readLine("Name [" + product.getName() + "]: ");
        if (!name.isEmpty()) {
            product.setName(DataStore.sanitize(name));
        }
        String category = InputHelper.readLine("Category [" + product.getCategory() + "]: ");
        if (!category.isEmpty()) {
            product.setCategory(DataStore.sanitize(category));
        }
        String price = InputHelper.readLine("Price [" + product.getPrice() + "]: ");
        if (!price.isEmpty()) {
            try {
                product.setPrice(Double.parseDouble(price));
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid price, keeping old value.");
            }
        }
        String stock = InputHelper.readLine("Stock [" + product.getStock() + "]: ");
        if (!stock.isEmpty()) {
            try {
                product.setStock(Integer.parseInt(stock));
            } catch (NumberFormatException e) {
                System.out.println("  ! Invalid stock, keeping old value.");
            }
        }

        productService.save();
        System.out.println("\n  Product updated.");
        InputHelper.pause();
    }

    private void deleteProduct() {
        InputHelper.header("DELETE PRODUCT");
        ProductService.printTable(productService.getAll());
        int id = InputHelper.readInt("\nEnter product ID to delete (0 to cancel): ", 0, 999999);
        if (id == 0) {
            return;
        }
        Product product = productService.findById(id);
        if (product == null) {
            System.out.println("  ! No product with that ID.");
            InputHelper.pause();
            return;
        }
        if (InputHelper.confirm("Really delete '" + product.getName() + "'?")) {
            productService.delete(id);
            System.out.println("  Product deleted.");
        } else {
            System.out.println("  Cancelled.");
        }
        InputHelper.pause();
    }

    private void lowStockReport() {
        InputHelper.header("LOW STOCK (below 20 units)");
        java.util.List<Product> low = new java.util.ArrayList<>();
        for (Product product : productService.getAll()) {
            if (product.getStock() < 20) {
                low.add(product);
            }
        }
        ProductService.printTable(low);
        InputHelper.pause();
    }

    private void listUsers() {
        InputHelper.header("REGISTERED USERS");
        System.out.printf("%-16s %-22s %-10s%n", "USERNAME", "FULL NAME", "ROLE");
        InputHelper.line();
        for (User user : authService.getAllUsers()) {
            System.out.printf("%-16s %-22s %-10s%n",
                    user.getUsername(), user.getFullName(), user.getRole());
        }
        InputHelper.line();
        InputHelper.pause();
    }

    /* ------------------------------------------------------------------ */
    /*  CUSTOMER SIDE                                                      */
    /* ------------------------------------------------------------------ */

    private void customerMenu() {
        boolean active = true;
        while (active) {
            InputHelper.header("CUSTOMER MENU - " + currentUser.getFullName()
                    + "  |  Cart: " + cart.getItems().size() + " item(s)");
            System.out.println("  1. Browse all products");
            System.out.println("  2. Search / filter products");
            System.out.println("  3. Add item to cart");
            System.out.println("  4. View cart");
            System.out.println("  5. Update / remove cart item");
            System.out.println("  6. Checkout and generate bill");
            System.out.println("  7. My order history");
            System.out.println("  0. Logout");
            int choice = InputHelper.readInt("\nChoose an option: ", 0, 7);

            switch (choice) {
                case 1:
                    InputHelper.header("AVAILABLE PRODUCTS");
                    ProductService.printTable(productService.getAll());
                    InputHelper.pause();
                    break;
                case 2:
                    searchMenu();
                    break;
                case 3:
                    addToCart();
                    break;
                case 4:
                    InputHelper.header("YOUR CART");
                    cart.print();
                    InputHelper.pause();
                    break;
                case 5:
                    modifyCart();
                    break;
                case 6:
                    checkout();
                    break;
                case 7:
                    myOrders();
                    break;
                case 0:
                    logout();
                    active = false;
                    break;
                default:
                    break;
            }
        }
    }

    private void addToCart() {
        InputHelper.header("ADD TO CART");
        ProductService.printTable(productService.getAll());
        int id = InputHelper.readInt("\nEnter product ID (0 to cancel): ", 0, 999999);
        if (id == 0) {
            return;
        }
        Product product = productService.findById(id);
        if (product == null) {
            System.out.println("  ! No product with that ID.");
            InputHelper.pause();
            return;
        }
        if (!product.isInStock()) {
            System.out.println("  ! '" + product.getName() + "' is out of stock.");
            InputHelper.pause();
            return;
        }
        int quantity = InputHelper.readInt("Quantity (available " + product.getStock() + "): ",
                1, product.getStock());

        if (cart.add(product, quantity)) {
            System.out.println("\n  Added " + quantity + " x " + product.getName() + " to cart.");
        } else {
            System.out.println("\n  ! That would exceed available stock for this product.");
        }
        InputHelper.pause();
    }

    private void modifyCart() {
        InputHelper.header("UPDATE CART");
        cart.print();
        if (cart.isEmpty()) {
            InputHelper.pause();
            return;
        }
        int id = InputHelper.readInt("\nEnter product ID to change (0 to cancel): ", 0, 999999);
        if (id == 0) {
            return;
        }
        int quantity = InputHelper.readInt("New quantity (0 removes the item): ", 0, 100000);
        if (cart.updateQuantity(id, quantity)) {
            System.out.println("\n  Cart updated.");
        } else {
            System.out.println("\n  ! Item not in cart, or quantity above available stock.");
        }
        InputHelper.pause();
    }

    private void checkout() {
        InputHelper.header("CHECKOUT");
        if (cart.isEmpty()) {
            System.out.println("  Your cart is empty. Add something first.");
            InputHelper.pause();
            return;
        }
        cart.print();
        if (!InputHelper.confirm("\nConfirm this purchase?")) {
            System.out.println("  Checkout cancelled.");
            InputHelper.pause();
            return;
        }
        Order order = orderService.placeOrder(currentUser, cart);
        if (order != null) {
            cart.clear();
        }
        InputHelper.pause();
    }

    private void myOrders() {
        InputHelper.header("MY ORDERS");
        List<Order> mine = orderService.getOrdersOf(currentUser.getUsername());
        OrderService.printOrderTable(mine);
        if (!mine.isEmpty()) {
            viewOrderDetail();
        } else {
            InputHelper.pause();
        }
    }

    /** Lets the user reprint a stored receipt by order ID. */
    private void viewOrderDetail() {
        String id = InputHelper.readLine("\nEnter an Order ID to view its bill (Enter to skip): ");
        if (id.isEmpty()) {
            return;
        }
        Order order = orderService.findById(id);
        if (order == null) {
            System.out.println("  ! No order with that ID.");
        } else {
            User owner = authService.findByUsername(order.getUsername());
            System.out.println(orderService.buildReceipt(order, owner));
        }
        InputHelper.pause();
    }

    /* ------------------------------------------------------------------ */
    /*  SHARED: SEARCH AND FILTER                                          */
    /* ------------------------------------------------------------------ */

    private void searchMenu() {
        InputHelper.header("SEARCH AND FILTER");
        System.out.println("  1. Search by name or category keyword");
        System.out.println("  2. Filter by category");
        System.out.println("  3. Filter by price range");
        System.out.println("  4. Show only in-stock items");
        System.out.println("  0. Back");
        int choice = InputHelper.readInt("\nChoose an option: ", 0, 4);

        switch (choice) {
            case 1: {
                String keyword = InputHelper.readNonEmpty("Keyword: ");
                System.out.println();
                ProductService.printTable(productService.searchByKeyword(keyword));
                break;
            }
            case 2: {
                List<String> categories = productService.getCategories();
                System.out.println("\n  Categories:");
                for (int i = 0; i < categories.size(); i++) {
                    System.out.println("   " + (i + 1) + ". " + categories.get(i));
                }
                if (categories.isEmpty()) {
                    break;
                }
                int pick = InputHelper.readInt("Choose a category: ", 1, categories.size());
                System.out.println();
                ProductService.printTable(productService.filterByCategory(categories.get(pick - 1)));
                break;
            }
            case 3: {
                double min = InputHelper.readDouble("Minimum price: ", 0, 1000000);
                double max = InputHelper.readDouble("Maximum price: ", min, 1000000);
                System.out.println();
                ProductService.printTable(productService.filterByPrice(min, max));
                break;
            }
            case 4: {
                java.util.List<Product> inStock = new java.util.ArrayList<>();
                for (Product product : productService.getAll()) {
                    if (product.isInStock()) {
                        inStock.add(product);
                    }
                }
                System.out.println();
                ProductService.printTable(inStock);
                break;
            }
            case 0:
            default:
                return;
        }
        InputHelper.pause();
    }
}
