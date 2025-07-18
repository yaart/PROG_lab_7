package com.example.server.models;

/**
 * Класс, представляющий пользователя системы на стороне сервера.
 * <p>
 * Используется для хранения учетных данных пользователей — имя и пароль.
 * Может применяться при регистрации, аутентификации и авторизации в системе.
 * </p>
 *
 * @see com.example.client.models.User — клиентская версия класса
 */
public class User {
    /**
     * Имя пользователя (логин).
     * <p>Должно быть уникальным в системе.</p>
     */
    public String username;

    /**
     * Пароль пользователя.
     * <p>Хранится в открытом виде. Рекомендуется использовать безопасное хранение,
     * например, хэширование с солью, при использовании в реальных приложениях.</p>
     */
    public String pass;

    /**
     * Конструктор по умолчанию.
     * <p>Используется, например, фреймворками сериализации (Jackson, Hibernate).</p>
     */
    public User() {
    }

    /**
     * Конструктор для создания объекта пользователя с заданными учетными данными.
     *
     * @param username имя пользователя, не должно быть {@code null}
     * @param pass     пароль пользователя, может быть {@code null} (не рекомендуется)
     */
    public User(String username, String pass) {
        this.username = username;
        this.pass = pass;
    }
}