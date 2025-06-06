package com.example.server.models;

public class User {
    public String username;
    public String pass;

    public User() {
    }

    public User(String username,String pass) {
        this.username = username;
        this.pass = pass;
    }
}