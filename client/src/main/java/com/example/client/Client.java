package com.example.client;

import com.example.client.fieldReader.DisciplineFieldReader;
import com.example.client.fieldReader.LabWorkFieldReader;
import com.example.client.iomanager.StandartIOManager;
import com.example.client.models.*;
import com.example.client.utils.SHA1;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Основной класс клиентского приложения, реализующий взаимодействие с сервером через TCP-сокеты.
 * <p>
 * Поддерживает следующие функции:
 * <ul>
 *     <li>Регистрация и авторизация пользователей</li>
 *     <li>Отправка команд на сервер</li>
 *     <li>Выполнение скриптов из файлов (с защитой от рекурсии)</li>
 *     <li>Обработка ответов от сервера</li>
 * </ul>
 * </p>
 *
 */
public class Client {
    private static User user;

    /**
     * Хост сервера по умолчанию.
     */
    private static final String SERVER_HOST = "localhost";

    /**
     * Порт сервера по умолчанию.
     */
    private static final int SERVER_PORT = 8088;

    /**
     * Объект для сериализации/десериализации JSON.
     */
    private static final ObjectMapper objectMapper = new ObjectMapper();
    /**
     * Точка входа в клиентское приложение.
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        System.out.println("Client starting...");

        try (Socket socket = new Socket(SERVER_HOST, SERVER_PORT);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             Scanner consoleScanner = new Scanner(System.in)) {

            System.out.println("Connected to server at " + SERVER_HOST + ":" + SERVER_PORT);
            System.out.println("Введите команды ('register user pass', 'login user pass')");

            String userInput;
            while (true) {
                if (user == null) {
                    System.out.print("> ");
                } else {
                    System.out.print("(" + user.username + ") > ");
                }

                userInput = consoleScanner.nextLine();

                if (userInput == null || userInput.trim().isEmpty()) {
                    System.out.println("Введите команду");
                    continue;
                }

                Command command = create(userInput, out, in, new ArrayList<>());

                if (command == null) {
                    continue;
                }

                out.println(objectMapper.writeValueAsString(command));

                Response response;
                while (!(response = objectMapper.readValue(in.readLine(), Response.class)).data.equals("EOF")) {
                    if (userInput.split(" ")[0].equals("exit")) {
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

    /**
     * Создаёт объект команды на основе пользовательского ввода.
     * <p>
     * Обрабатывает как простые команды, так и выполнение скриптов из файлов.
     * Предотвращает бесконечную рекурсию при вложенных скриптах.
     * </p>
     *
     * @param line     строка ввода пользователя
     * @param out      поток вывода для отправки команд на сервер
     * @param in       поток ввода для получения ответов от сервера
     * @param scripts  список уже выполняемых скриптов (для защиты от рекурсии)
     * @return созданный объект команды или {@code null}, если команда не распознана или произошла ошибка
     * @throws InterruptedException если поток был прерван
     * @throws JsonProcessingException если произошла ошибка сериализации
     */
    static Command create(String line, PrintWriter out, BufferedReader in, List<String> scripts) throws InterruptedException, JsonProcessingException {
        final String[] tokens = line.split(" ");
        final String commandType = tokens[0];

        Command command;
        if (commandType.equals("execute_script")) {
            if (tokens.length < 2) {
                System.out.println("ERROR: Укажите имя файла для execute_script");
                return null;
            }

            String scriptName = tokens[1];

            if (scripts.contains(scriptName)) {
                System.err.println("Обнаружена рекурсия! Пропускаем файл: " + scriptName);

                return new UserCommand("echo", List.of("Recursion was found!"), user);

            }

            try (BufferedReader reader = new BufferedReader(new FileReader(scriptName))) {
                String input;
                List<String> newScripts = new ArrayList<>(scripts);
                newScripts.add(scriptName);

                while ((input = reader.readLine()) != null) {
                    input = input.trim();
                    if (input.isEmpty()) continue;

                    try {
                        Command c = create(input, out, in, newScripts);
                        if (c == null) continue;

                        out.println(objectMapper.writeValueAsString(c));

                        Response response;
                        while (!(response = objectMapper.readValue(in.readLine(), Response.class)).data.equals("EOF")) {
                            if ("exit".equals(input.split(" ")[0])) {
                                user = null;
                            }
                            System.out.println("Server: " + response.data);
                        }
                    } catch (Exception e) {
                        System.err.println("Ошибка при выполнении команды из скрипта: " + e.getMessage());
                        // Не прерываем выполнение всего скрипта
                    }
                }
            } catch (FileNotFoundException e) {
                System.err.println("Файл не найден: " + scriptName);
            } catch (IOException e) {
                System.err.println("Ошибка чтения файла: " + e.getMessage());
            }

            return null; // завершающая команда
        } else if (commandType.equalsIgnoreCase("register") || commandType.equalsIgnoreCase("login")) {

            if (tokens.length < 3 ||tokens[1] == null || tokens[1].trim().isEmpty()|| tokens[2] == null || tokens[2].trim().isEmpty()) {
                System.out.println("ERROR: Нужны логин и пароль для команды '" + commandType);
                return null;
            }

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
                case "exit", "help", "info", "clear", "head", "show", "remove_first", "print_unique_tuned_in_works",
                     "print_field_ascending_discipline", "show_owner":
                    command = new UserCommand(commandType, List.of(), user);
                    break;
                default:
                    System.out.println("Unknown command type: " + commandType);
                    return null;
            }

        }
        return command;
    }
} 