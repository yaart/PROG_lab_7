package com.example.server.database;


import com.example.server.models.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.*;

/**
 * CollectionDataBaseService - класс для работы с датабазой
 */
public class CollectionDataBaseService {
    private static final Logger logger = LogManager.getLogger();

    private final Connection connection;

    public CollectionDataBaseService(Connection connection) {
        this.connection = connection;
    }

    /**
     * Инициализирует таблицу LabWork в БД.
     */
    public void init() throws SQLException {
//        connection.createStatement().execute("DROP TABLE labworks;");
        String sql = """
                    CREATE TABLE IF NOT EXISTS labworks (
                        id SERIAL PRIMARY KEY,
                        name TEXT NOT NULL,
                        x BIGINT NOT NULL CHECK (x > -263),
                        y INT NOT NULL,
                        creation_date TEXT NOT NULL,
                        minimal_point FLOAT NOT NULL CHECK (minimal_point > 0),
                        tuned_in_works INT NOT NULL CHECK (tuned_in_works >= 0),
                        difficulty TEXT NOT NULL,
                        discipline_name TEXT,
                        discipline_lecture_hours BIGINT,
                        discipline_practice_hours BIGINT,
                        discipline_self_study_hours BIGINT,
                        discipline_labs_count INT,
                        owner_login VARCHAR(50) REFERENCES users(login)
                    );
                """;

        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            logger.info("Таблица labworks создана или уже существует");
        } catch (SQLException e) {
            logger.error("Не удалось создать таблицу labworks", e);
            throw new RuntimeException("Not connected to the database");
        }
    }


    /**
     * Добавляет новую LabWork в БД.
     *
     * @param newLabWork новый элемент
     * @param ownerLogin логин пользователя, добавляющего элемент
     * @throws RuntimeException если не удалось добавить, если нет соединения
     */
    public void addNewLabWork(LabWork newLabWork, String ownerLogin) throws RuntimeException {
        String sqlInsert = """
                    INSERT INTO labworks (
                        name, x, y, creation_date, minimal_point, tuned_in_works,
                        difficulty, discipline_name, discipline_lecture_hours,
                        discipline_practice_hours, discipline_self_study_hours,
                        discipline_labs_count, owner_login
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    RETURNING id
                """;

        try (PreparedStatement ps = connection.prepareStatement(sqlInsert)) {
            ps.setString(1, newLabWork.getName());
            ps.setLong(2, newLabWork.getCoordinates().getX());
            ps.setInt(3, newLabWork.getCoordinates().getY());
            ps.setString(4, newLabWork.getCreationDate());
            ps.setFloat(5, newLabWork.getMinimalPoint());
            ps.setInt(6, newLabWork.getTunedInWorks());
            ps.setString(7, newLabWork.getDifficulty().name());

            Discipline discipline = newLabWork.getDiscipline();
            if (discipline != null) {
                ps.setString(8, discipline.getName());
                ps.setLong(9, discipline.getLectureHours());
                ps.setLong(10, discipline.getPracticeHours());
                ps.setLong(11, discipline.getSelfStudyHours());
                ps.setLong(12, discipline.getLabsCount());
            } else {
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.BIGINT);
                ps.setNull(10, Types.BIGINT);
                ps.setNull(11, Types.BIGINT);
                ps.setNull(12, Types.BIGINT);
            }

            ps.setString(13, ownerLogin);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int newId = rs.getInt(1);
                    newLabWork.setId(newId);
                } else {
                    logger.error("Не удалось сохранить элемент:\n{}", newLabWork);
                    throw new RuntimeException("Save labwork failed");
                }
            }

        } catch (SQLException ex) {
            logger.error("Ошибка при добавлении LabWork: {}", ex.getMessage());
            throw new RuntimeException("Add labwork failed");
        }
    }

    /**
     * Обновляет элемент в БД.
     *
     * @param updatedLabWork обновлённый элемент
     * @throws RuntimeException если не удалось обновить
     */
    public void updateLabWork(LabWork updatedLabWork) throws RuntimeException {
        String sql = """
                    UPDATE labworks SET
                        name = ?, x = ?, y = ?, creation_date = ?,
                        minimal_point = ?, tuned_in_works = ?, difficulty = ?,
                        discipline_name = ?, discipline_lecture_hours = ?,
                        discipline_practice_hours = ?, discipline_self_study_hours = ?,
                        discipline_labs_count = ?
                    WHERE id = ?
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, updatedLabWork.getName());
            ps.setLong(2, updatedLabWork.getCoordinates().getX());
            ps.setInt(3, updatedLabWork.getCoordinates().getY());
            ps.setString(4, updatedLabWork.getCreationDate());
            ps.setFloat(5, updatedLabWork.getMinimalPoint());
            ps.setInt(6, updatedLabWork.getTunedInWorks());
            ps.setString(7, updatedLabWork.getDifficulty().name());

            Discipline d = updatedLabWork.getDiscipline();
            if (d != null) {
                ps.setString(8, d.getName());
                ps.setLong(9, d.getLectureHours());
                ps.setLong(10, d.getPracticeHours());
                ps.setLong(11, d.getSelfStudyHours());
                ps.setLong(12, d.getLabsCount());
            } else {
                ps.setNull(8, Types.VARCHAR);
                ps.setNull(9, Types.BIGINT);
                ps.setNull(10, Types.BIGINT);
                ps.setNull(11, Types.BIGINT);
                ps.setNull(12, Types.BIGINT);
            }

            ps.setInt(13, updatedLabWork.getId());

            int rows = ps.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Update labwork failed");
            }

        } catch (SQLException ex) {
            logger.error("Не удалось обновить элемент: {}", ex.getMessage());
            throw new RuntimeException("Update labwork failed");
        }
    }

    /**
     * Удаляет элемент по ID.
     *
     * @param id         ID удаляемого элемента
     * @param ownerLogin логин пользователя
     * @throws RuntimeException если удаление не выполнено
     */
    public void deleteLabWorkById(int id, String ownerLogin) throws RuntimeException {
        String sql = "DELETE FROM labworks WHERE id = ? AND owner_login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, ownerLogin);
            int rowsAffected = ps.executeUpdate();
            if (rowsAffected == 0) {
                throw new RuntimeException("Delete labwork failed");
            }
        } catch (SQLException ex) {
            logger.error("Ошибка при удалении элемента {}: {}", id, ex.getMessage());
            throw new RuntimeException("Delete labwork failed");
        }
    }

    /**
     * Очищает коллекцию для конкретного пользователя.
     *
     * @param login логин пользователя, чьи записи нужно удалить
     * @throws RuntimeException если не удалось очистить
     */
    public void clearCollection(String login) throws RuntimeException {
        String sql = "DELETE FROM labworks WHERE owner_login = ?";
        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, login);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            logger.error("Не удалось очистить таблицу для пользователя " + login, ex);
            throw new RuntimeException("Clear collection failed for user " + login);
        }
    }
    /**
     * Создает объект {@link LabWork} на основе данных из {@link ResultSet}.
     *
     * @param rs объект {@link ResultSet}, содержащий данные строки таблицы
     * @return объект типа {@link LabWork}, собранный из текущей строки результата
     * @throws SQLException если произошла ошибка при чтении данных из ResultSet
     * @throws RuntimeException если значение enum {@code difficulty} не поддерживается
     */
    private LabWork createFromResultSet(ResultSet rs) throws SQLException, RuntimeException {
        int id = rs.getInt("id");
        String name = rs.getString("name");
        long x = rs.getLong("x");
        int y = rs.getInt("y");
        String creationDate = rs.getString("creation_date");
        float minimalPoint = rs.getFloat("minimal_point");
        int tunedInWorks = rs.getInt("tuned_in_works");
        Difficulty difficulty = Difficulty.valueOf(rs.getString("difficulty"));

        String disciplineName = rs.getString("discipline_name");
        Long lectureHours = rs.getObject("discipline_lecture_hours", Long.class);
        Long practiceHours = rs.getObject("discipline_practice_hours", Long.class);
        long selfStudyHours = rs.getLong("discipline_self_study_hours");
        Integer labsCount = rs.getObject("discipline_labs_count", Integer.class);

        Discipline discipline = null;
        if (disciplineName != null) {
            discipline = new Discipline(disciplineName, lectureHours, practiceHours, selfStudyHours, labsCount);
        }

        Coordinates coordinates = new Coordinates(x, y);
        LabWork labWork = new LabWork(id, name, coordinates, creationDate, minimalPoint, tunedInWorks, difficulty, discipline);
        labWork.setOwnerLogin(rs.getString("owner_login"));

        return labWork;
    }

    /**
     * Загружает всю коллекцию из БД.
     *
     * @return список LabWork с владельцами
     * @throws CannotUploadCollectionException если загрузка провалилась
     */
    /**
     * Загружает все элементы LabWork из базы данных.
     *
     * @return список элементов LabWorkWithOwner
     */
    public List<LabWork> loadInMemory() throws RuntimeException {
        String sql = """
                    SELECT * FROM labworks;
                """;
        List<LabWork> loaded = new ArrayList<>();

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                loaded.add(createFromResultSet(rs));
            }

        } catch (SQLException | RuntimeException ex) {
            logger.error("Не удалось загрузить коллекцию", ex);
            throw new RuntimeException("Upload labwork failed");
        }

        return loaded;
    }

    /**
     * Строит объект LabWorkWithOwner из текущей строки ResultSet.
     */
    private LabWork createLabWorkFromCurrentRow(ResultSet rs) throws SQLException, RuntimeException {
        int id = rs.getInt(1);
        String name = rs.getString(2);
        long x = rs.getLong(3);
        int y = rs.getInt(4);
        String creationDate = rs.getString(5);
        float minimalPoint = rs.getFloat(6);
        int tunedInWorks = rs.getInt(7);
        Difficulty difficulty = Difficulty.valueOf(rs.getString(8));

        String disciplineName = rs.getString(9);
        Long lectureHours = rs.getObject(10, Long.class);
        Long practiceHours = rs.getObject(11, Long.class);
        long selfStudyHours = rs.getLong(12);
        Integer labsCount = rs.getObject(13, Integer.class);
        String ownerLogin = rs.getString(14);

        Discipline discipline = null;
        if (disciplineName != null) {
            discipline = new Discipline(disciplineName, lectureHours, practiceHours, selfStudyHours, labsCount);
        }

        LabWork labWork = new LabWork(
                id, name, new Coordinates(x, y), creationDate,
                minimalPoint, tunedInWorks, difficulty, discipline
        );
        labWork.setOwnerLogin(ownerLogin);

        return labWork;
    }

    /**
     * Проверяет, является ли пользователь владельцем элемента.
     *
     * @param id    ID элемента
     * @param login логин пользователя
     * @return true, если пользователь — владелец
     */
    public boolean isOwner(int id, String login) {
        String sql = "SELECT COUNT(*) FROM labworks WHERE id = ? AND owner_login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setString(2, login);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            logger.warn("Ошибка проверки владельца: {}", e.getMessage());
        }
        return false;
    }

    /**
     * Сохраняет все элементы коллекции для пользователя.
     *
     * @param collection коллекция LabWork
     * @param ownerLogin логин пользователя
     */
    /**
     * Сохраняет всю коллекцию в БД.
     */
    public void saveAll(Collection<LabWork> collection, String ownerLogin) throws RuntimeException {
        truncate(ownerLogin); // очищаем старые данные
        insertAll(collection, ownerLogin); // вставляем новые
    }

    private void truncate(String ownerLogin) throws RuntimeException {
        String sql = "DELETE FROM labworks WHERE owner_login = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, ownerLogin);
            ps.executeUpdate();
        } catch (SQLException ex) {
            logger.warn("Не удалось очистить данные", ex);
            throw new RuntimeException("Clear labworks failed");
        }
    }

    /**
     * Сохраняет коллекцию лабораторных работ в базу данных.
     * <p>
     * Используется для массовой вставки (batch insert) с использованием {@link PreparedStatement}.
     * Если у объекта {@link LabWork} отсутствует дисциплина, то соответствующие поля в БД заполняются значением NULL.
     * </p>
     *
     * @param collection коллекция объектов типа {@link LabWork}, которые нужно сохранить
     * @param ownerLogin логин владельца, который будет установлен у всех записей
     * @throws RuntimeException если произошла ошибка при взаимодействии с базой данных
     */
    private void insertAll(Collection<LabWork> collection, String ownerLogin) throws RuntimeException {
        String sql = """
                    INSERT INTO labworks (
                        id, name, x, y, creation_date,
                        minimal_point, tuned_in_works, difficulty,
                        discipline_name, discipline_lecture_hours,
                        discipline_practice_hours, discipline_self_study_hours,
                        discipline_labs_count, owner_login
                    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            for (LabWork labWork : collection) {
                ps.setInt(1, labWork.getId());
                ps.setString(2, labWork.getName());
                ps.setLong(3, labWork.getCoordinates().getX());
                ps.setInt(4, labWork.getCoordinates().getY());
                ps.setString(5, labWork.getCreationDate());
                ps.setFloat(6, labWork.getMinimalPoint());
                ps.setInt(7, labWork.getTunedInWorks());
                ps.setString(8, labWork.getDifficulty().name());

                Discipline d = labWork.getDiscipline();
                if (d != null) {
                    ps.setString(9, d.getName());
                    ps.setLong(10, d.getLectureHours());
                    ps.setLong(11, d.getPracticeHours());
                    ps.setLong(12, d.getSelfStudyHours());
                    ps.setLong(13, d.getLabsCount());
                } else {
                    ps.setNull(9, Types.VARCHAR);
                    ps.setNull(10, Types.BIGINT);
                    ps.setNull(11, Types.BIGINT);
                    ps.setNull(12, Types.BIGINT);
                    ps.setNull(13, Types.BIGINT);
                }

                ps.setString(14, ownerLogin);
                ps.addBatch();
            }

            ps.executeBatch();
            logger.info("Сохранено {} элементов в БД", collection.size());

        } catch (SQLException ex) {
            logger.error("Не удалось сохранить коллекцию", ex);
            throw new RuntimeException("Save labworks failed");
        }
    }
    /**
     * Возвращает множество уникальных логинов владельцев лабораторных работ из базы данных.
     * <p>
     * Выполняет SQL-запрос на выборку уникальных значений столбца "owner_login"
     * из таблицы "labworks".
     * </p>
     *
     * @return множество строк, представляющих логины пользователей
     * @throws SQLException если произошла ошибка при выполнении запроса к БД
     */
    public Set<String> getAllOwners() throws SQLException {
        Set<String> owners = new HashSet<>();
        String sql = "SELECT DISTINCT owner_login FROM labworks";
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                owners.add(rs.getString("owner_login"));
            }
        }
        return owners;
    }
}
