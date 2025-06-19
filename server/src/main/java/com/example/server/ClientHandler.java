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

/**
 * Обработчик клиентского подключения.
 * <p>
 * Этот класс реализует {@link Runnable} и предназначен для обслуживания одного клиента через TCP-сокет.
 *
 * <p>Каждое соединение обрабатывается в отдельном потоке.</p>
 *
 * @see ClientHandler#run() — точка входа для потока
 * @see Command — базовый класс команд
 * @see UserCommand — команда, связанная с пользователем
 */
public class ClientHandler implements Runnable {
    private final Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public ClientHandler(Socket socket) {
        this.clientSocket = socket;
    }

    /**
     * Проверяет учетные данные пользователя.
     * <p>
     * Используется для подтверждения прав доступа перед выполнением команд,
     * требующих авторизации.
     * </p>
     *
     * @param user объект пользователя, чьи учетные данные нужно проверить
     * @return true, если пользователь существует и пароль верен; иначе false
     */
    boolean validateCredentials(User user) {
        return ServiceLocator.userDataBaseService.validateCredentials(user.username, user.pass);
    }

    /**
     * Основной метод, управляющий взаимодействием с клиентом.
     * <p>
     * Запускается при старте потока и обслуживает одного клиента через сокет:
     * <ul>
     *     <li>Чтение команд от клиента</li>
     *     <li>Десериализация JSON в объект {@link Command}</li>
     *     <li>Обработка команды</li>
     *     <li>Отправка ответа клиенту</li>
     * </ul>
     * </p>
     *
     * <p>После завершения общения закрывает все ресурсы (ввод/вывод, сокет).</p>
     */
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

    /**
     * Обрабатывает полученную команду и возвращает соответствующий ответ.
     * <p>
     * Если команда является экземпляром {@link UserCommand}, проверяет аутентификацию пользователя.
     * В зависимости от типа команды вызывает соответствующий обработчик.
     * </p>
     *
     * @param command полученная команда от клиента
     * @return объект {@link Response} с результатом выполнения команды
     */
    private Response processCommand(Command command) {
        String help_text = "Справка по командам:\n" +
                " add                  - add {element}: добавить новый элемент в коллекцию\n" +
                " clear                - clear: очистить коллекцию\n" +
                " count_less_than_discipline - count_less_than_discipline {discipline}: подсчёт количества элементов, у которых дисциплина меньше заданной\n" +
                " filter_by_size       - filter_by_size {size}: вывести элементы, размер которых меньше заданного\n" +
                " head                 - head : вывести первый элемент коллекции\n" +
                " help                 - help : вывести справку по доступным командам\n" +
                " info                 - info : вывести информацию о коллекции\n" +
                " print_field_ascending_discipline - print_field_ascending_discipline : вывести все дисциплины из коллекции в порядке возрастания\n" +
                " print_unique_tuned_in_works - print_unique_tuned_in_works : вывести уникальные значения поля tunedInWorks\n" +
                " remove_by_id         - remove_by_id id : удалить элемент из коллекции по его ID\n" +
                " remove_first         - remove_first : удалить первый элемент из коллекции\n" +
                " remove_lower         - remove_lower id : удалить все элементы с ID меньше указанного\n" +
                " show                 - show : вывести все элементы коллекции\n" +
                " update               - update id {element} : обновить значение элемента коллекции, id которого равен заданному\n";

        if (command instanceof UserCommand userCommand) {
            boolean authenticated = validateCredentials(userCommand.user);
            if (!authenticated) {
                return new Response("ERROR: Not logged in. Please login first.");
            }
            return switch (userCommand.name) {
                case "echo" -> new Response(userCommand.arguments.get(0).toString());
                case "add" -> handleAdd(userCommand);
                case "clear" -> handleClear(userCommand);
                case "count_less_than_discipline" -> handleCountLessThanDiscipline(userCommand);
                case "filter_by_size" -> handleFilterBySize(userCommand);
                case "head" -> handleHead(userCommand);
                case "help" -> new Response(help_text);
                case "info" -> handleInfo(userCommand);
                case "print_field_ascending_discipline" -> handlePrintFieldAscendingDiscipline(userCommand);
                case "print_unique_tuned_in_works" -> handlePrintUniqueTunedInWorks(userCommand);
                case "remove_by_id" -> handleRemoveById(userCommand);
                case "remove_first" -> handleRemoveFirst(userCommand);
                case "remove_lower" -> handleRemoveLower(userCommand);
                case "show" -> handleShow(userCommand);
                case "update" -> handleUpdateId(userCommand);
                case "exit" -> new Response("INFO: Exiting");
                case "show_owner" -> handleShowOwner();
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

    /**
     * Регистрирует нового пользователя в системе.
     * <p>
     * Передает данные в сервис базы данных. При ошибке возвращает сообщение о неудаче.
     * </p>
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return ответ с результатом регистрации
     */
    private Response handleRegister(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new Response("ERROR: Логин и пароль обязательны.");
        }
        try {
            ServiceLocator.userDataBaseService.registerNewUser(username, password);
        } catch (Exception e) {
            System.err.println("Registration attempt failed: " + e.getMessage());
            return new Response("ERROR: Registration attempt failed.");
        }

        System.out.println("User registered: " + username);
        return new Response("SUCCESS: User registered successfully.");
    }

    /**
     * Выполняет вход пользователя в систему.
     * <p>
     * Проверяет учетные данные и возвращает результат аутентификации.
     * </p>
     *
     * @param username имя пользователя
     * @param password пароль пользователя
     * @return ответ с информацией об успешном или неудачном входе
     */
    private Response handleLogin(String username, String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return new Response("ERROR: Логин и пароль обязательны.");
        }
        if (ServiceLocator.userDataBaseService.validateCredentials(username, password)) {
            System.out.println("User logged in: " + username);
            return new Response("SUCCESS: Logged in as " + username + ".");
        } else {
            System.err.println("Login attempt failed: Invalid password. User: " + username);
            return new Response("ERROR: Invalid password.");
        }
    }

    /**
     * Возвращает информацию о текущем состоянии коллекции.
     * <p>
     * Информация включает тип коллекции, дату создания и количество элементов.
     * </p>
     *
     * @return ответ с информацией о коллекции
     */
    private Response handleInfo(UserCommand command) {
        String type = ServiceLocator.collectionSyncManager.getCollectionType();
        ZonedDateTime creationDate = ServiceLocator.collectionSyncManager.getCreationDate();
        int size = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username).size();

        return new Response("Тип коллекции: " + type + "\nДата инициализации: " + creationDate +
                "\nЧисло элементов: " + size);
    }

    /**
     * Очищает коллекцию пользователя
     *
     * @param command команда, содержащая добавляемый элемент
     * @return ответ с результатом операции
     */
    private Response handleClear(UserCommand command) {
        try {
            ServiceLocator.collectionDataBaseService.clearCollection(command.user.username);

            ServiceLocator.collectionSyncManager.clear(command.user.username);

            return new Response("Коллекция очищена");
        } catch (Exception e) {
            return new Response("ERROR: " + e.getMessage());
        }
    }

    /**
     * Возвращает верхний элемент коллекции пользователя
     *
     * @return ответ с результатом операции
     */
    private Response handleHead(UserCommand command) {
        List<LabWork> list = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username);

        if (list.isEmpty()) {
            return new Response("Empty");
        }

        return new Response(list.get(0).toString());
    }

    /**
     * Возвращает всю коллекцию пользователя.
     * @return ответ с информацией о коллекции
     */
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

    /**
     * Возвращает все элементы коллекции, принадлежащие текущему пользователю.
     * <p>
     * Метод извлекает данные из {@link CollectionSyncManager}, фильтрует их по владельцу,
     * убирает дубликаты, сортирует и формирует текстовый ответ со списком элементов.
     * </p>
     *
     * <p>Если коллекция пользователя пуста, возвращается сообщение "Empty".</p>
     * @return объект {@link Response}, содержащий:
     *         <ul>
     *             <li>список всех элементов пользователя, разделённых переводом строки</li>
     *             <li>сообщение "Empty", если элементов нет</li>
     *             <li>ошибку, если произошло исключение</li>
     *         </ul>
     */
    private Response handleShowOwner() {
        try {
            List<LabWork> labWorks = ServiceLocator.collectionSyncManager.getAll().stream()
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

    /**
     * Удаляет элементы коллекции с Id меньшим чем переданное.
     * <p>
     * Проверяет принадлежность элемента пользователю, удаляет из БД и коллекции.
     * </p>
     *
     * @param command команда, содержащая ID удаляемого элемента
     * @return ответ с результатом удаления
     */
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

    /**
     * Удаляет верхний элемент коллекции.
     * <p>
     * Проверяет принадлежность элемента пользователю, удаляет из БД и коллекции.
     * </p>
     *
     * @param command команда, содержащая ID удаляемого элемента
     * @return ответ с результатом удаления
     */
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

            return new Response("Первый верхний элемент (ID: " + idToRemove + ") удален.");
        } catch (RuntimeException ex) {
            return new Response("Ошибка при удалении из базы данных");
        }
    }

    /**
     * Удаляет элемент коллекции по указанному ID.
     * <p>
     * Проверяет принадлежность элемента пользователю, удаляет из БД и коллекции.
     * </p>
     *
     * @param command команда, содержащая ID удаляемого элемента
     * @return ответ с результатом удаления
     */
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

    /**
     * Возвращает список уникальных значений поля {@code tunedInWorks} из коллекции.
     * <p>
     * Метод собирает все ненулевые значения поля {@code tunedInWorks} из элементов коллекции,
     * фильтрует дубликаты, сортирует их и формирует текстовый ответ.
     * </p>
     *
     * @return объект {@link Response}, содержащий отсортированный список уникальных значений
     *         или сообщение о том, что таких элементов нет
     */
    private Response handlePrintUniqueTunedInWorks(UserCommand command) {
        Set<Integer> uniqueValues = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username).stream()
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
    /**
     * Возвращает список уникальных названий дисциплин из коллекции, отсортированных по алфавиту.
     * <p>
     * Метод собирает названия дисциплин из всех элементов коллекции, у которых поле
     * {@code discipline} не равно null, убирает дубликаты и сортирует результат.
     * </p>
     *
     * @param command команда пользователя, не используется напрямую, но требуется для сигнатуры
     * @return объект {@link Response}, содержащий отсортированный список дисциплин
     *         или сообщение об отсутствии элементов с дисциплинами
     */
    private Response handlePrintFieldAscendingDiscipline(UserCommand command) {

        List<String> disciplines = ServiceLocator.collectionSyncManager.getAllByOwner(command.user.username).stream()
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
    /**
     * Фильтрует элементы коллекции по размеру координат.
     * <p>
     * Размер вычисляется как евклидова норма: √(x² + y²).
     * Возвращаются только те элементы, у которых эта величина меньше или равна заданному размеру.
     * </p>
     *
     * @param command команда пользователя, содержащая один аргумент — максимальный размер
     * @return объект {@link Response}, содержащий количество найденных элементов
     */
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

    /**
     * Считает количество элементов, чья дисциплина "меньше" заданной (лексикографически).
     * <p>
     * Дисциплины сравниваются по имени. Используется метод {@link CollectionSyncManager#countLessThanDiscipline(Discipline)}.
     * </p>
     *
     * @param command команда пользователя, содержащая сериализованный объект типа {@link Discipline}
     * @return объект {@link Response}, содержащий количество подходящих элементов
     */
    private Response handleCountLessThanDiscipline(UserCommand command) {
        try {
            Discipline discipline = objectMapper.readValue(command.arguments.get(0).toString(), Discipline.class);

            long count = ServiceLocator.collectionSyncManager.countLessThanDiscipline(discipline);

            return new Response(String.format("Найдено %d элементов с дисциплиной меньше \"%s\"", count, discipline.getName()));
        } catch (Exception e) {
            return new Response("Не удалось сохранить элемент в БД");
        }
    }

    /**
     * Добавляет новый элемент типа {@link LabWork} в коллекцию.
     * <p>
     * Устанавливает владельца элемента и уникальный ID, сохраняет в БД и коллекцию.
     * </p>
     *
     * @param command команда, содержащая добавляемый элемент
     * @return ответ с результатом операции
     */
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

    /**
     * Обновляет существующий элемент коллекции по его ID.
     * <p>
     * Проверяет, что пользователь является владельцем элемента,
     * обновляет запись в БД и в памяти.
     * </p>
     *
     * @param command команда, содержащая ID и новые данные элемента
     * @return ответ с результатом обновления
     */
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
