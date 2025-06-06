package com.example.server;

import com.example.server.database.CollectionDataBaseService;
import com.example.server.models.Discipline;
import com.example.server.models.LabWork;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * CollectionSyncManager - потокобезопасный менеджер коллекции.
 */
public class CollectionSyncManager {
    private final Logger logger = LogManager.getLogger();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final PriorityQueue<LabWork> collection = new PriorityQueue<>();

    public CollectionSyncManager() {}

    /**
     * Добавляет новый элемент в коллекцию.
     */
    public void add(LabWork labWork) {
        lock.writeLock().lock();
        try {
            if (!collection.add(labWork)) {
                logger.warn("Элемент не был добавлен: {}", labWork);
            } else {
                logger.info("Добавлен элемент ID: {}", labWork.getId());
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Удаляет элемент по ID и логину владельца.
     */
    public boolean removeIf(int id, String ownerLogin) {
        lock.writeLock().lock();
        try {
            boolean removed = collection.removeIf(lw -> lw.getId() == id && (lw.getOwnerLogin() == null || lw.getOwnerLogin().equals(ownerLogin)));
            if (removed) {
                logger.info("Удалён элемент: {}", id);
            } else {
                logger.warn("Элемент {} не найден или не принадлежит вам", id);
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Заменяет всю коллекцию новыми элементами.
     *
     * @param newElements новые элементы
     */
    public void replaceAll(Collection<LabWork> newElements) {
        lock.writeLock().lock();
        try {
            collection.clear();
            collection.addAll(newElements);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeAll(List<Integer> ids) {
        lock.writeLock().lock();
        try {
            collection.removeIf(lw -> ids.contains(lw.getId()));
            logger.info("Удалено {} элементов", ids.size());
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Обновляет элемент в коллекции.
     */
    public boolean update(LabWork updated, String ownerLogin) {
        lock.writeLock().lock();
        try {
            boolean removed = collection.removeIf(lw -> lw.getId() == updated.getId() && (lw.getOwnerLogin() == null || lw.getOwnerLogin().equals(ownerLogin)));
            if (removed) {
                collection.add(updated);
                logger.info("Обновлён элемент ID: {}", updated.getId());
            } else {
                logger.warn("Не удалось обновить элемент {}: не владелец или не найден", updated.getId());
            }
            return removed;
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Возвращает копию коллекции для чтения.
     */
    public Collection<LabWork> getForRead() {
        lock.readLock().lock();
        try {
            return List.copyOf(collection);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Возвращает всех пользователей.
     */
    public List<LabWork> getAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(collection);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Сохраняет при завершении
     */
    public void saveOnExit(CollectionDataBaseService labWorkDAO, String currentOwner) throws RuntimeException {
        lock.readLock().lock();
        try {
            labWorkDAO.saveAll(collection, currentOwner);
        } finally {
            lock.readLock().unlock();
        }
    }


    public List<LabWork> getAllByOwner(String ownerLogin) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(lw -> lw.getOwnerLogin() == null || lw.getOwnerLogin().equals(ownerLogin))
                    .toList();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, пуста ли коллекция.
     */
    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return collection.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Подсчёт элементов с дисциплиной меньше заданной.
     */
    public long countLessThanDiscipline(Discipline discipline) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(lw -> lw.getDiscipline() != null && lw.getDiscipline().compareTo(discipline) < 0)
                    .count();
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Подсчёт колличества элементов
     */
    public int size() {
        lock.readLock().lock();
        try {
            return collection.size();
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Сортирует всю коллекцию.
     */
    public void sort() {
        lock.writeLock().lock();
        try {
            List<LabWork> sortedList = new ArrayList<>(collection);
            sortedList.sort(Comparator.naturalOrder());
            collection.clear();
            collection.addAll(sortedList);
            logger.info("Коллекция отсортирована");
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Возвращает тип коллекции.
     */
    public String getCollectionType() {
        return collection.getClass().getSimpleName();
    }

    /**
     * Возвращает дату создания коллекции.
     */
    public ZonedDateTime getCreationDate() {
        // Загружается из БД при старте сервера
        return ZonedDateTime.now(); // ← замени на реальную дату
    }

    /**
     * Очищает коллекцию для пользователя.
     */
    public void clear(String ownerLogin) {
        lock.writeLock().lock();
        try {
            collection.removeIf(lw -> lw.getOwnerLogin() == null || lw.getOwnerLogin().equals(ownerLogin));
            logger.info("Коллекция очищена для пользователя: {}", ownerLogin);
        } finally {
            lock.writeLock().unlock();
        }
    }

    /**
     * Получить элемент по ID.
     */
    public LabWork getById(int id) {
        lock.readLock().lock();
        try {
            return collection.stream()
                    .filter(lw -> lw.getId() == id)
                    .findFirst()
                    .orElse(null);
        } finally {
            lock.readLock().unlock();
        }
    }

    /**
     * Проверяет, принадлежит ли элемент пользователю.
     */
    public boolean isOwner(int id, String login) {
        LabWork lw = getById(id);
        if (lw == null) return false;
        return lw.getOwnerLogin() == null || lw.getOwnerLogin().equals(login);
    }
}
