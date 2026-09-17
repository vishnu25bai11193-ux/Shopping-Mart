Campus Shopping Mart — Java Console Application

A terminal-based retail billing system with role-based login, cart and GST billing, order history with receipts, and CSV file persistence.

Build and run
bash
cd ShoppingMart
javac -d out src/*.java
java -cp out Main

The data/ and receipts/ folders are created automatically on first run, relative to the directory you run the program from.

Default login
Role	Username	Password
Admin	admin	admin123
Customer	register a new one from the main menu	
Features

Admin

View, add, update and delete products
Search by keyword, filter by category / price range / stock
Low-stock alert (below 20 units)
View all orders and reprint any bill
Sales report: revenue, GST collected, average order value, top 5 sellers
View registered users

Customer

Register and log in
Browse, search and filter the catalogue
Add to cart, change quantity, remove items
Checkout with per-item GST, printed bill, and a saved receipt file
Personal order history with bill reprint
File layout
ShoppingMart/
├── src/
│   ├── Main.java              entry point
│   ├── ShoppingMartApp.java   all menu flows
│   ├── Product.java           model
│   ├── User.java              model
│   ├── CartItem.java          model
│   ├── Order.java             model + nested OrderLine
│   ├── ProductService.java    inventory logic
│   ├── AuthService.java       login / registration / SHA-256 hashing
│   ├── CartService.java       cart logic
│   ├── OrderService.java      checkout, receipts, sales report
│   ├── DataStore.java         all file I/O
│   └── InputHelper.java       validated Scanner input
├── data/       products.csv, users.csv, orders.csv  (auto-created)
└── receipts/   ORD0001.txt, ORD0002.txt, ...        (auto-created)
Design notes (useful for the viva)
Layered design. Models hold data, services hold logic, ShoppingMartApp holds the UI, DataStore holds all file I/O. No class does two of those jobs.
Per-product GST. Each product carries its own rate (5% grocery, 12% dairy, 18% general), so the bill matches how Indian retail invoices actually work.
Frozen order lines. An order stores a copy of the name, price and GST rate at the time of purchase, so an admin editing a price later cannot change an old bill.
Passwords are hashed with SHA-256 before being written to users.csv.
Input is never trusted. InputHelper re-prompts on bad input, and DataStore.sanitize() strips , : | so a product name cannot corrupt the CSV format.
Stock is checked twice — when adding to cart and again at checkout.
