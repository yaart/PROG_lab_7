package com.example.server;

import com.example.server.database.CollectionDataBaseService;
import com.example.server.database.DataBaseConnector;
import com.example.server.database.UserDataBaseService;
import com.example.server.models.LabWork;
import com.jcraft.jsch.JSchException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/**
 * Локатор сервисов, предоставляющий доступ к общим ресурсам приложения.
 * <p>
 * Этот класс реализует паттерн "Service Locator" и используется для централизованного получения
 * доступа к следующим компонентам:
 * <ul>
 *     <li>{@link UserDataBaseService} — для работы с пользователями</li>
 *     <li>{@link CollectionDataBaseService} — для работы с коллекцией лабораторных работ через БД</li>
 *     <li>{@link CollectionSyncManager} — для управления коллекцией в памяти с поддержкой синхронизации</li>
 * </ul>
 * </p>
 *
 * <p>Инициализация выполняется один раз при запуске сервера.</p>
 *
 * @see ServiceLocator#init(DataBaseConnector) — точка инициализации всех сервисов
 */
public class ServiceLocator {
    /**
     * Сервис для работы с данными пользователей (регистрация, аутентификация).
     */
    static UserDataBaseService userDataBaseService;

    /**
     * Сервис для работы с коллекцией лабораторных работ в базе данных.
     */
    static CollectionDataBaseService collectionDataBaseService;

    /**
     * Менеджер коллекции, работающий в оперативной памяти, с поддержкой синхронизации.
     */
    static CollectionSyncManager collectionSyncManager;

    /**
     * Инициализирует все необходимые сервисы и загружает данные в память.
     * <p>
     * Устанавливает соединение с БД, создаёт экземпляры сервисов и наполняет
     * {@link CollectionSyncManager} данными из БД.
     * </p>
     *
     * @param connector объект-подключатель к базе данных через SSH и JDBC
     * @throws JSchException если произошла ошибка при настройке SSH-туннеля
     * @throws SQLException если произошла ошибка при подключении к БД или выполнении запросов
     */
    static void init(DataBaseConnector connector) throws JSchException, SQLException {
        Connection connection = connector.connect();

        userDataBaseService = new UserDataBaseService(connection);
        collectionDataBaseService = new CollectionDataBaseService(connection);

        userDataBaseService.init();
        collectionDataBaseService.init();

        collectionSyncManager = new CollectionSyncManager();
        List<LabWork> loaded = collectionDataBaseService.loadInMemory();
        collectionSyncManager.replaceAll(loaded.stream()
                .toList());
    }
}