package com.example.client;

import com.example.client.fieldReader.DisciplineFieldReader;
import com.example.client.fieldReader.LabWorkFieldReader;
import com.example.client.iomanager.IOManager;
import com.example.client.iomanager.StandartIOManager;
import com.example.client.models.*;
import com.example.client.utils.SHA1;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Client {
    private static User user;

    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 8088;


    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) {
        System.out.println("Client starting...");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner consoleScanner = new Scanner(System.in)) {

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            System.out.println("Enter commands (e.g., 'register user pass', 'login user pass', 'echo message', 'exit')");

            String userInput;
            while (true) {
                if (user == null) {
                    System.out.print("> ");
                } else {
                    System.out.print("(" + user.username + ") > ");
                }

                userInput = consoleScanner.nextLine();

                if (userInput == null || userInput.trim().isEmpty()) {
                    System.out.println("Please enter a command");
                    continue;
                }


                final String[] tokens = userInput.split(" ");
                final String commandType = tokens[0];
                Command command;
                if (commandType.equalsIgnoreCase("register") || commandType.equalsIgnoreCase("login")) {
                    final String login = tokens[1];
                    final String pass = SHA1.hash(tokens[2]);

                    command = new Command(commandType, List.of(login, pass));
                    if (commandType.equalsIgnoreCase("login")) {
                        user = new User(login, pass);
                    }
                } else {

                    switch (commandType) {
                        case "add":
                            LabWorkFieldReader labWorkFieldReader1 = new LabWorkFieldReader(new StandartIOManager());
                            LabWork labWork1 = labWorkFieldReader1.executeLabWork();
                            labWork1.setOwnerLogin(user.username);
                            command = new UserCommand(commandType, List.of(objectMapper.writeValueAsString(labWork1)), user);
                            break;
                        case "update":
                            LabWorkFieldReader labWorkFieldReader2 = new LabWorkFieldReader(new StandartIOManager());
                            LabWork labWork2 = labWorkFieldReader2.executeLabWork();
                            labWork2.setOwnerLogin(user.username);
                            command = new UserCommand(commandType, List.of(tokens[1], objectMapper.writeValueAsString(labWork2)), user);
                            break;
                        case "count_less_than_discipline":
                            DisciplineFieldReader disciplineFieldReader = new DisciplineFieldReader(new StandartIOManager());
                            Discipline discipline = disciplineFieldReader.executeDiscipline();
                            command = new UserCommand(commandType, List.of(objectMapper.writeValueAsString(discipline)), user);
                            break;
                        case "echo", "remove_lower", "remove_by_id", "filter_by_size":
                            command = new UserCommand(commandType, List.of(tokens[1]), user);
                            break;
                        case "exit", "help", "info", "clear", "head", "show", "remove_first", "print_unique_tuned_in_works", "print_field_ascending_discipline":
                            command = new UserCommand(commandType, List.of(), user);
                            break;
                        default:
                            System.out.println("Unknown command type: " + commandType);
                            continue;
                    }

                }

                out.println(objectMapper.writeValueAsString(command));

                Response response;
                while (!(response = objectMapper.readValue(in.readLine(), Response.class)).data.equals("EOF")) {
                    if (commandType.equals("exit")) {
                        System.out.println("Server: " + response.data);
                        user = null;
                    } else {
                        System.out.println("Server: " + response.data);
                    }
                }
            }

        } catch (UnknownHostException e) {
            System.err.println("Server not found: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("I/O error: " + e.getMessage());
        } catch (Exception e) {
            System.err.println("Any error: " + e.getMessage());
        } finally {
            System.out.println("Client shutdown.");
        }
    }
} 