package vn.edu.nct.quanlyphim.model;

public class User {

    private final long userId;
    private final String username;
    private final String fullName;
    private final String role;
    private final boolean active;

    public User(
            long userId,
            String username,
            String fullName,
            String role,
            boolean active) {

        this.userId = userId;
        this.username = username;
        this.fullName = fullName;
        this.role = role;
        this.active = active;
    }

    public long getUserId() {
        return userId;
    }

    public String getUsername() {
        return username;
    }

    public String getFullName() {
        return fullName;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return active;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role);
    }
}