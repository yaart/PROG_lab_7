package com.example.server.models;

import java.util.List;

public class UserCommand extends Command {
    public User user;

    public UserCommand() {
        super();
    }

    public UserCommand(String name, List<Object> arguments, User user) {
        super(name, arguments);
        this.user = user;
    }
}
