package com.example.server.models;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

/**
 * Базовый абстрактный класс для представления команд в системе.
 * @see UserCommand
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type"  // Custom discriminator field
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = UserCommand.class, name = "userCommand")
})
public class Command {
    public String name;
    public List<Object> arguments;

    public Command() {
    }

    public Command(String command, List<Object> arguments) {
        this.name = command;
        this.arguments = arguments;
    }
}
