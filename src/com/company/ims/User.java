package com.company.ims;

public class User {
    private final String username;
    private final String password;
    private final Role role;

    public User(String username, String password, Role role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public String getUsername() { return username; }
    public Role getRole() { return role; }

    public boolean checkPassword(String raw) {
        return password.equals(raw);
    }
}
