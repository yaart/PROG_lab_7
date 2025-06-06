package com.example.client.iomanager;


/**
 * EmptyIOManager - для пустых вводов
 */

public class EmptyIOManager implements IOManager {
    /**
     * Записает в поток
     *
     * @param obj записываемое значение
     */
    @Override
    public void write(Object obj) {

    }

    /**
     * Записывает в поток, добавляя в конец знак переноса строки
     *
     * @param obj записываемое значение
     */
    @Override
    public void writeLine(Object obj) {

    }

    /**
     * Записывает в поток ошибок
     *
     * @param obj записываемое значение
     */
    @Override
    public void writeError(Object obj) {

    }

    /**
     * Чтение строки из потока
     *
     * @return строку из потока
     */
    @Override
    public String readLine() {
        return "";
    }


}