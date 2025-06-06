package com.example.server;

import com.example.server.models.*;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.*;
import java.net.Socket;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.lang.Math.pow;

public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    boolean validateCredentials(User user) {
        return ServiceLocator.userDataBaseService.validateCredentials(user.username, user.pass);
    }

    @Override
    public void run() {
        try {
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                System.out.println("Received from client (" + clientSocket.getInetAddress() + "): " + inputLine);
                Command command = objectMapper.readValue(inputLine, Command.class);
                Response response = processCommand(command);
                out.println(objectMapper.writeValueAsString(response));
                out.println("{\"data\": \"EOF\"}");
            }
        } catch (IOException e) {
            System.err.println("IOException for " + clientSocket.getInetAddress() + ": " + e.getMessage());
        } finally {
            try {
                if (out != null) out.close();
                if (in != null) in.close();
                if (clientSocket != null) clientSocket.close();
                System.out.println("Client disconnected: " + (clientSocket != null ? clientSocket.getInetAddress() : "unknown"));
            } catch (IOException e) {
                System.err.println("Error closing resources for client: " + e.getMessage());
            }
        }
    }

    private Response processCommand(Command command) {
        if (command instanceof UserCommand userCommand) {
            boolean authenticated = validateCredentials(userCommand.user);
            if (!authenticated) {
                return new Response("ERROR: Not logged in. Please login first.");
            }
            return switch (userCommand.name) {
                case "echo" -> new Response("ECHO: " + userCommand.arguments.get(0).toString());
                case "add" -> handleAdd(userCommand);
                case "clear" -> handleClear(userCommand);
                case "count_less_than_discipline" -> handleCountLessThanDiscipline(userCommand);
                case "filter_by_size" -> handleFilterBySize(userCommand);
                case "head" -> handleHead(userCommand);
                case "help" -> new Response("HELP: Some info");
                case "info" -> handleInfo();
                case "print_field_ascending_discipline" -> handlePrintFieldAscendingDiscipline(userCommand);
                case "print_unique_tuned_in_works" -> handlePrintUniqueTunedInWorks();
                case "remove_by_id" -> handleRemoveById(userCommand);
                case "remove_first" -> handleRemoveFirst(userCommand);
                case "remove_lower" -> handleRemoveLower(userCommand);
                case "show" -> handleShow(userCommand);
                case "update" -> handleUpdateId(userCommand);
                case "exit" -> new Response("INFO: Exiting");
                default -> new Response("ERROR: Unknown command: " + command);
            };
        } else {
            return switch (command.name) {
                case "register" ->
                        handleRegister(command.arguments.get(0).toString(), command.arguments.get(1).toString());
                case "login" -> handleLogin(command.arguments.get(0).toString(), command.arguments.get(1).toString());
                default -> new Response("ERROR: Unknown command: " + command.name);
            };
        }
    }

    private Response handleRegister(String username, String password) {
        try {
            ServiceLocator.userDataBaseService.registerNewUser(username, password);
        } catch (Exception e) {
            System.err.println("Registration attempt failed: " + e.getMessage());
            return new Response("ERROR: Registration attempt failed.");
        }

        System.out.println("User registered: " + username);
        return new Response("SUCCESS: User registered successfully.");
    }

    private Response handleLogin(String username, String password) {
        if (ServiceLocator.userDataBaseService.validateCredentials(username, password)) {
            System.out.println("User logged in: " + username);
            return new Response("SUCCESS: Logged in as " + username + ".");
        } else {
            System.err.println("Login attempt failed: Invalid password. User: " + username);
            return new Response("ERROR: Invalid password.");
        }
    }

    private Response handleInfo() {
        String type = ServiceLocator.collectionSyncManager.getCollectionType();
        ZonedDateTime creationDate = ServiceLocator.collectionSyncManager.getCreationDate();
        int size = ServiceLocator.collectionSyncManager.size();

        return new Response("Тип коллекции: " + type + "\nДата инициализации: " + creationDate +
                "\nЧисло элементов: " + size);
    }

    private Response handleClear(UserCommand command) {
        try {
            // Удаление из БД (только своих элементов)
            ServiceLocator.collectionDataBaseService.clearCollection(command.user.username);

            // Очистка коллекции в памяти
            ServiceLocator.collectionSyncManager.clear(command.user.username);

            return new Response("Коллекция очищена");
        } catch (Exception e) {
            return new Response("ERROR: " + e.getMessage());
        }
    }

    private Response handleHead(UserCommand command) {
        List<LabWork> list = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username);

        if (list.isEmpty()) {
            return new Response("Empty");
        }

        return new Response(list.get(0).toString());
    }

    private Response handleShow(UserCommand command) {

        try {
            List<LabWork> labWorks = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username).stream()
                    .distinct()
                    .sorted()
                    .toList();

            if (labWorks.isEmpty()) {
                return new Response("Empty");
            }

            String responseText = labWorks.stream()
                    .map(LabWork::toString)
                    .collect(Collectors.joining("\n"));

            return new Response(responseText);
        } catch (Exception e) {
            return new Response("ERROR: " + e.getMessage());
        }
    }

    private Response handleRemoveLower(UserCommand command) {
        try {
            int keyId = Integer.parseInt(command.arguments.get(0).toString());

            List<Integer> removedIds = ServiceLocator.collectionSyncManager.getForRead().stream()
                    .filter(lw -> lw.getId() < keyId && (lw.getOwnerLogin() == null || lw.getOwnerLogin().equals(command.user.username)))
                    .map(LabWork::getId)
                    .toList();

            if (removedIds.isEmpty()) {
                return new Response("Empty");
            }

            for (int id : removedIds) {
                ServiceLocator.collectionDataBaseService.deleteLabWorkById(id, command.user.username); // удаление через БД
            }
            ServiceLocator.collectionSyncManager.removeAll(removedIds); // удаление из памяти
            String message = "Удалены элементы с ID: " + removedIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            return new Response(message);
        } catch (RuntimeException ex) {
            return new Response("Не удалось удалить элементы из БД");
        }
    }

    private Response handleRemoveFirst(UserCommand command) {
        List<LabWork> usersElements = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username);

        if (usersElements.isEmpty()) {
            return new Response("Empty");
        }

        LabWork firstUserElement = usersElements.stream()
                .min(Comparator.naturalOrder()) // сортировка по ID
                .orElseThrow();

        int idToRemove = firstUserElement.getId();

        try {
            ServiceLocator.collectionDataBaseService.deleteLabWorkById(idToRemove, command.user.username); // удаление из БД
            ServiceLocator.collectionSyncManager.removeIf(idToRemove, command.user.username); // удаление из коллекции

            return new Response("Первый ваш элемент (ID: " + idToRemove + ") удален.");
        } catch (RuntimeException ex) {
            return new Response("Ошибка при удалении из базы данных");
        }
    }

    private Response handleRemoveById(UserCommand command) {
        try {
            int id = Integer.parseInt(command.arguments.get(0).toString());


            if (!ServiceLocator.collectionSyncManager.isOwner(id, command.user.username)) {
                return new Response("Вы не являетесь владельцем элемента");
            }

            ServiceLocator.collectionDataBaseService.deleteLabWorkById(id, command.user.username);
            ServiceLocator.collectionSyncManager.removeIf(id, command.user.username);

            return new Response("Элемент " + id + " удален");
        } catch (RuntimeException ex) {
            return new Response("Не удалось удалить элемент");
        }
    }

    private Response handlePrintUniqueTunedInWorks() {
        Set<Integer> uniqueValues = ServiceLocator.collectionSyncManager.getForRead().stream()
                .map(LabWork::getTunedInWorks)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        List<String> sortedValues = uniqueValues.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.toList());

        if (sortedValues.isEmpty()) {
            return new Response("Нет элементов с полем tunedInWorks.");
        }

        String responseText = String.join("\n", sortedValues);

        return new Response(responseText);

    }

    private Response handlePrintFieldAscendingDiscipline(UserCommand command) {

        List<String> disciplines = ServiceLocator.collectionSyncManager.getForRead().stream()
                .filter(lw -> lw.getDiscipline() != null)
                .map(lw -> lw.getDiscipline().getName())
                .sorted()
                .distinct()
                .collect(Collectors.toList());

        if (disciplines.isEmpty()) {
            return new Response("В коллекции нет элементов с дисциплинами");
        }

        String responseText = String.join("\n", disciplines);

        return new Response(responseText);

    }

    private Response handleFilterBySize(UserCommand command) {
        int size = Integer.parseInt(command.arguments.get(0).toString());

        List<LabWork> filtered = ServiceLocator.collectionSyncManager.getForRead().stream()
                .filter(lw -> {
                    Coordinates c = lw.getCoordinates();
                    return c != null && pow(c.getX(), 2) + pow(c.getY(), 2) <= size;
                })
                .toList();

        String responseMessage = "Найдено " + filtered.size() + " элементов с размером <= " + size;

        return new Response(responseMessage);
    }

    private Response handleCountLessThanDiscipline(UserCommand command) {
        try {
            Discipline discipline = objectMapper.readValue(command.arguments.get(0).toString(), Discipline.class);

            long count = ServiceLocator.collectionSyncManager.countLessThanDiscipline(discipline);

            return new Response(String.format("Найдено %d элементов с дисциплиной меньше \"%s\"", count, discipline.getName()));
        } catch (Exception e) {
            return new Response("Не удалось сохранить элемент в БД");
        }
    }

    private Response handleAdd(UserCommand command) {
        try {
            LabWork labWork = objectMapper.readValue(command.arguments.get(0).toString(), LabWork.class);
            labWork.setOwnerLogin(command.user.username);
            labWork.setId((ServiceLocator.collectionSyncManager.size() + 1) * 101);
            ServiceLocator.collectionDataBaseService.addNewLabWork(labWork, command.user.username);

            ServiceLocator.collectionSyncManager.add(labWork);

            return new Response("Элемент успешно добавлен");

        } catch (Exception e) {
            return new Response("Не удалось сохранить элемент в БД");
        }
    }

    private Response handleUpdateId(UserCommand command) {
        try {
            int id = Integer.parseInt(command.arguments.get(0).toString());
            LabWork labWork = objectMapper.readValue(command.arguments.get(1).toString(), LabWork.class);
            labWork.setId(id);

            if (!ServiceLocator.collectionDataBaseService.isOwner(id, command.user.username)) {
                return new Response("Вы не являетесь владельцем");
            }

            ServiceLocator.collectionDataBaseService.updateLabWork(labWork);
            ServiceLocator.collectionSyncManager.update(labWork, command.user.username);
            return new Response("Элемент с ID " + id + " обновлён");
        } catch (RuntimeException ex) {
            return new Response("Ошибка при обновлении");
        } catch (Exception ex) {
            return new Response("Неверные данные");
        }
    }
}
