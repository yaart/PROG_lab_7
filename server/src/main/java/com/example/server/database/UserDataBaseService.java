package com.example.server.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * UserDataBaseService - класс для работы с пользователями в БД.
 */
public class UserDataBaseService {
    private final Connection connection;
    private final Logger logger = LogManager.getRootLogger();

    public UserDataBaseService(Connection connection) {
        this.connection = connection;
    }

    /**
     * Инициализирует таблицу Users.
     */
    public void init() throws SQLException {
//        connection.createStatement().execute("DROP TABLE users CASCADE");
        String sql = """
            CREATE TABLE IF NOT EXISTS users (
                id SERIAL PRIMARY KEY,
                login TEXT NOT NULL UNIQUE,
                password_hash TEXT NOT NULL
            );
        """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Таблица 'users' создана или уже существует");
        } catch (SQLException ex) {
            logger.error("Не удалось создать таблицу 'users': {}", ex.getMessage());
            throw new RuntimeException("Create table users failed");
        }
    }

    /**
     * Проверяет логин и пароль в БД.
     *
     * @param login логин
     * @param passwordHash хэш пароля
     * @return true, если данные верны, false в противном случае или при ошибке
     */
    public boolean validateCredentials(String login, String passwordHash) {
        String sql = "SELECT password_hash FROM users WHERE login = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, login);  // Исправлено: был setString(2, login)
            ResultSet rs = pstmt.executeQuery();

            // Проверяем, есть ли результат
            if (!rs.next()) {
                return false; // Пользователь с таким логином не найден
            }

            String storedHash = rs.getString("password_hash");
            return storedHash.equals(passwordHash);
        } catch (SQLException e) {
            logger.error("Ошибка проверки учётных данных: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Регистрация нового пользователя.
     *
     * @param login логин пользователя
     * @param passwordHash хэш пароля
     * @throws RuntimeException если логин уже занят или при ошибках БД
     */
    public void registerNewUser(String login, String passwordHash)
            throws RuntimeException {

        String sql = "INSERT INTO users (login, password_hash) VALUES (?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, login);
            pstmt.setString(2, passwordHash);

            int rows = pstmt.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Register user failed");
            }

        } catch (SQLException e) {
            final String uniqueViolation = "23505";
            if (e.getSQLState().equals(uniqueViolation)) {
                logger.warn("Логин {} уже зарегистрирован", login);
                throw new RuntimeException("Login is already exist");
            } else {
                logger.error("Ошибка регистрации: {}", e.getMessage());
                throw new RuntimeException("Error in register user");
            }
        }
    }
}