package com.example.server;

import com.example.server.database.DataBaseConnector;
import com.jcraft.jsch.JSchException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Основной класс серверного приложения.
 * <p>
 * Запускает TCP-сервер, ожидающий подключения клиентов. Для каждого клиента создаётся отдельный поток,
 * обрабатывающий команды с помощью {@link ClientHandler}.
 * </p>
 *
 * <p>Сервер использует:
 * <ul>
 *     <li>{@link DataBaseConnector} для подключения к базе данных через SSH</li>
 *     <li>{@link ServiceLocator} для доступа к сервисам (база данных, коллекция и т.д.)</li>
 *     <li>{@link ExecutorService} для ограничения числа одновременно обслуживаемых клиентов</li>
 * </ul>
 * </p>
 *
 * @see ClientHandler — класс, обслуживающий одного клиента
 * @see DataBaseConnector — класс для работы с БД через SSH
 * @see ServiceLocator — точка доступа к общим ресурсам сервера
 */
public class Server {
    private static final Logger logger = LogManager.getLogger();

    /**
     * Имя пользователя для подключения к БД.
     * По умолчанию берется из переменной окружения "DB_USER", иначе используется значение по умолчанию.
     */
    static String dbUser = System.getenv().getOrDefault("DB_USER", "s468198");

    /**
     * Пароль пользователя для подключения к БД.
     * По умолчанию берется из переменной окружения "DB_PASSWORD", иначе используется значение по умолчанию.
     */
    static String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "wj32bxeNkskKnr3V");

    /**
     * Имя пользователя для SSH-подключения.
     * По умолчанию берется из переменной окружения "SSH_USER", иначе используется значение по умолчанию.
     */
    static String sshUser = System.getenv().getOrDefault("SSH_USER", "s468198");

    /**
     * Пароль для SSH-подключения.
     * По умолчанию берется из переменной окружения "SSH_PASSWORD", иначе используется значение по умолчанию.
     */
    static String sshPassword = System.getenv().getOrDefault("SSH_PASSWORD", "fbFR!6830");

    /**
     * Порт, на котором будет запущен сервер.
     */
    private static final int PORT = 8088;

    /**
     * Максимальное количество потоков (одновременно обслуживаемых клиентов).
     */
    private static final int MAX_THREADS = 4;

    /**
     * Объект для подключения к базе данных.
     */
    private static DataBaseConnector dataBaseConnector;

    /**
     * Точка входа в серверное приложение.
     * <p>
     * Выполняет инициализацию, запуск сервера и ожидание клиентских подключений.
     * </p>
     *
     * @param args аргументы командной строки (не используются)
     */
    public static void main(String[] args) {
        try {
            init();
        } catch (Exception e) {
            logger.fatal(e.getMessage());
            return;
        }

        ExecutorService executorService = Executors.newFixedThreadPool(MAX_THREADS);
        System.out.println("Server starting on port: " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Waiting for clients...");
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("Client connected: " + clientSocket.getInetAddress());
                executorService.submit(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            executorService.shutdown();
            dispose();
        }
    }

    /**
     * Инициализирует подключение к базе данных и регистрирует сервисы через {@link ServiceLocator}.
     *
     * @throws JSchException если произошла ошибка при подключении через SSH
     * @throws SQLException если произошла ошибка при подключении к БД
     */
    private static void init() throws JSchException, SQLException {
        dataBaseConnector = new DataBaseConnector(dbUser, dbPassword, sshUser, sshPassword);
        ServiceLocator.init(dataBaseConnector);
    }

    /**
     * Освобождает ресурсы перед завершением работы сервера.
     * <p>
     * Закрывает соединение с базой данных.
     * </p>
     */
    public static synchronized void dispose() {
        dataBaseConnector.close();
    }
}