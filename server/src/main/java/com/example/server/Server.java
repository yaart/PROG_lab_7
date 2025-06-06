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

public class Server {
    private static final Logger logger = LogManager.getLogger();

    // Получаем логин/пароль от БД и SSH
    static String dbUser = System.getenv().getOrDefault("DB_USER", "s468198");
    static String dbPassword = System.getenv().getOrDefault("DB_PASSWORD", "wj32bxeNkskKnr3V");
    static String sshUser = System.getenv().getOrDefault("SSH_USER", "s468198");
    static String sshPassword = System.getenv().getOrDefault("SSH_PASSWORD", "fbFR!6830");


    private static final int PORT = 8088;
    private static final int MAX_THREADS = 4;

    private static DataBaseConnector dataBaseConnector;

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

    private static void init() throws JSchException, SQLException {
        dataBaseConnector = new DataBaseConnector(dbUser, dbPassword, sshUser, sshPassword);
        ServiceLocator.init(dataBaseConnector);
    }

    public static synchronized void dispose() {
        dataBaseConnector.close();
    }
} 