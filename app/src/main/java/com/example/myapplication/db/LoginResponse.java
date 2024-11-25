package com.example.myapplication.db;

public class LoginResponse {
    private User user; // 嵌套的 User 对象

    // Getter 和 Setter
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
