import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

/**
 * Registration and login. Passwords are stored as SHA-256 hashes, never as
 * plain text, so users.csv does not leak credentials.
 */
public class AuthService {

    private List<User> users = new ArrayList<>();

    public AuthService() {
        load();
        seedDefaultAdmin();
    }

    private void load() {
        users.clear();
        for (String line : DataStore.readLines(DataStore.USERS_FILE)) {
            User user = User.fromCsv(line);
            if (user != null) {
                users.add(user);
            }
        }
    }

    private void save() {
        List<String> lines = new ArrayList<>();
        for (User user : users) {
            lines.add(user.toCsv());
        }
        DataStore.writeLines(DataStore.USERS_FILE, lines);
    }

    /** On a fresh install there must be one admin, otherwise nobody can log in. */
    private void seedDefaultAdmin() {
        if (findByUsername("admin") == null) {
            users.add(new User("admin", hash("admin123"), User.ROLE_ADMIN, "Store Manager"));
            save();
        }
    }

    public User findByUsername(String username) {
        for (User user : users) {
            if (user.getUsername().equalsIgnoreCase(username)) {
                return user;
            }
        }
        return null;
    }

    /** Returns the logged-in user, or null when credentials do not match. */
    public User login(String username, String password) {
        User user = findByUsername(username);
        if (user == null) {
            return null;
        }
        if (user.getPasswordHash().equals(hash(password))) {
            return user;
        }
        return null;
    }

    /** Registers a customer. Returns null if the username is already taken. */
    public User register(String username, String password, String fullName) {
        username = DataStore.sanitize(username);
        if (findByUsername(username) != null) {
            return null;
        }
        User user = new User(username, hash(password), User.ROLE_CUSTOMER, DataStore.sanitize(fullName));
        users.add(user);
        save();
        return user;
    }

    public List<User> getAllUsers() {
        return users;
    }

    /** SHA-256, returned as a lowercase hex string. */
    public static String hash(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(input.getBytes("UTF-8"));
            StringBuilder hex = new StringBuilder();
            for (byte b : bytes) {
                String part = Integer.toHexString(0xff & b);
                if (part.length() == 1) {
                    hex.append('0');
                }
                hex.append(part);
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            // Should never happen: SHA-256 and UTF-8 are required on every JVM.
            return String.valueOf(input.hashCode());
        }
    }
}
