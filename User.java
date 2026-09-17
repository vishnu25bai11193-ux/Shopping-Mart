/**
 * An account of the system. Two roles exist: ADMIN and CUSTOMER.
 * Stored in data/users.csv as: username,passwordHash,role,fullName
 */
public class User {

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_CUSTOMER = "CUSTOMER";

    private String username;
    private String passwordHash;
    private String role;
    private String fullName;

    public User(String username, String passwordHash, String role, String fullName) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.role = role;
        this.fullName = fullName;
    }

    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getRole() { return role; }
    public String getFullName() { return fullName; }

    public boolean isAdmin() { return ROLE_ADMIN.equalsIgnoreCase(role); }

    public String toCsv() {
        return username + "," + passwordHash + "," + role + "," + fullName;
    }

    public static User fromCsv(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 4) {
            return null;
        }
        return new User(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim());
    }

    @Override
    public String toString() {
        return username + " (" + role + ")";
    }
}
