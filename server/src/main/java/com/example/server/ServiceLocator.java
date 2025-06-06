package com.example.server;

import com.example.server.database.CollectionDataBaseService;
import com.example.server.database.DataBaseConnector;
import com.example.server.database.UserDataBaseService;
import com.example.server.models.LabWork;
import com.jcraft.jsch.JSchException;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ServiceLocator {
    static UserDataBaseService userDataBaseService;
    static CollectionDataBaseService collectionDataBaseService;
    static CollectionSyncManager collectionSyncManager;

    static void init(DataBaseConnector connector) throws JSchException, SQLException {
        // Подключение к базе данных через SSH-туннель
        Connection connection = connector.connect();

        // DAO для работы с пользователями и LabWork
        userDataBaseService = new UserDataBaseService(connection);
        collectionDataBaseService = new CollectionDataBaseService(connection);

        // Инициализация таблиц в БД
        userDataBaseService.init();
        collectionDataBaseService.init();

        collectionSyncManager = new CollectionSyncManager();
        List<LabWork> loaded = collectionDataBaseService.loadInMemory();
        collectionSyncManager.replaceAll(loaded.stream()
                .toList());
    }
}
