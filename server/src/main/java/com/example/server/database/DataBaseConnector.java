package com.example.server.database;

import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * DataBaseConnector - класс для установления соединения с базой данных через SSH-туннель.
 * Поддерживает работу с PostgresQL на кафедральном сервере se.ifmo.ru
 */
public class DataBaseConnector {
    private static final Logger logger = LogManager.getLogger();

    // Константы SSH
    private static final String SSH_HOST = "se.ifmo.ru";
    private static final int SSH_PORT = 2222;

    // Константы БД
    private static final String DB_NAME = "studs";
    private static final String REMOTE_DB_HOST = "localhost"; // хост на удалённом сервере
    private static final int REMOTE_DB_PORT = 5432; // стандартный порт PostgreSQL на сервере
    private static final int LOCAL_FORWARD_PORT = 5432;

    private final String dbUser;
    private final String dbPassword;
    private final String tunnelUser;
    private final String tunnelPassword;

    private Session sshSession;
    private Connection dbConnection;
    private boolean initialized = false;

    /**
     * Конструктор с пользовательскими данными для БД и туннеля.
     *
     * @param dbUser     логин от БД
     * @param dbPassword пароль от БД
     * @param tunnelUser логин для SSH-туннеля
     * @param tunnelPass пароль для SSH-туннеля
     */
    public DataBaseConnector(String dbUser, String dbPassword, String tunnelUser, String tunnelPass) {
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.tunnelUser = tunnelUser;
        this.tunnelPassword = tunnelPass;
    }

    /**
     * Устанавливает SSH-туннель и JDBC-соединение.
     *
     * @return SQL-соединение
     * @throws JSchException если не удалось подключиться по SSH
     * @throws SQLException  если не удалось подключиться к БД
     */
    public Connection connect() throws JSchException, SQLException {
        if (!initialized) {
            setupSshTunnel(tunnelUser, tunnelPassword);
            setupDatabaseConnection(dbUser, dbPassword);
            initialized = true;
        }
        return dbConnection;
    }

    /**
     * Устанавливает SSH-туннель к удалённому серверу.
     *
     * @param tunnelUser логин для SSH
     * @throws JSchException если ошибка при подключении
     */
    private void setupSshTunnel(String tunnelUser, String tunnelPassword) throws JSchException {
        if (sshSession != null && sshSession.isConnected()) {
            return;
        }

        JSch jsch = new JSch();
        sshSession = jsch.getSession(tunnelUser, SSH_HOST, SSH_PORT);
        sshSession.setPassword(tunnelPassword);

        Properties config = new Properties();
        config.put("StrictHostKeyChecking", "no");
        sshSession.setConfig(config);
        sshSession.connect();

        // Пробрасываем порт
        sshSession.setPortForwardingL(LOCAL_FORWARD_PORT, REMOTE_DB_HOST, REMOTE_DB_PORT);
        logger.info("SSH-туннель успешно создан");
    }

    /**
     * Устанавливает JDBC-соединение с БД через localhost после туннеля.
     *
     * @param dbUser     логин от БД
     * @param dbPassword пароль от БД
     * @throws SQLException если не удалось подключиться к БД
     */
    private void setupDatabaseConnection(String dbUser, String dbPassword) throws SQLException {
        String url = "jdbc:postgresql://localhost:" + LOCAL_FORWARD_PORT + "/" + DB_NAME;
        dbConnection = DriverManager.getConnection(url, dbUser, dbPassword);
        logger.info("Соединение с базой данных установлено: {}", url);
    }

    /**
     * Проверяет, закрыто ли соединение, и пытается переподключиться.
     *
     * @throws JSchException если не удалось создать SSH-туннель
     * @throws SQLException если не удалось восстановить соединение с БД
     */
    private void tryReconnect() throws JSchException, SQLException {
        if (dbConnection == null || dbConnection.isClosed()) {
            setupSshTunnel(tunnelUser, tunnelPassword);
            setupDatabaseConnection(dbUser, dbPassword);
        }
    }


    /**
     * Закрывает все активные соединения (SSH и БД).
     */
    public synchronized void close() {
        try {
            if (dbConnection != null && !dbConnection.isClosed()) {
                dbConnection.close();
            }
            if (sshSession != null && sshSession.isConnected()) {
                sshSession.disconnect();
            }
            logger.info("Соединение с базой данных закрыто");
        } catch (SQLException e) {
            logger.error("Не удалось корректно закрыть соединение", e);
        }
    }
}