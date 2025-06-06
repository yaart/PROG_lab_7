package com.example.client.iomanager;


/**
 * Интерфейс для управления вводом-выводом
 */
public interface IOManager {
    /**
     * Выводит объект без перевода строки
     * @param obj объект для вывода
     */
    public void write(Object obj);

    /**
     * Выводит объект с переводом строки
     * @param obj объект для вывода
     */
    public void writeLine(Object obj);

    /**
     * Выводит сообщение об ошибке
     * @param obj объект для вывода
     */
    public void writeError(Object obj);

    /**
     * Читает строку из входного потока
     * @return прочитанная строка
     */
    public String readLine();

    /**
     * Проверяет наличие данных для чтения
     * @return true если есть данные
     */

}