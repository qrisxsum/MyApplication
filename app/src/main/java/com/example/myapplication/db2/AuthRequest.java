package com.example.myapplication.db2;

public class AuthRequest {
    private String username;
    private String password;
    private String type; // type 用来区分登录和注册

    public AuthRequest(String username, String password, String type) {
        this.username = username;
        this.password = password;
        this.type = type; // 设置操作类型：login 或 register
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}

