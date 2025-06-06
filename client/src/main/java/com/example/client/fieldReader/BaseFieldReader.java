package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

import java.util.NoSuchElementException;

/**
 * BaseFieldReader - базовый класс для считывания полей
 */
public abstract class BaseFieldReader {
    protected String fieldAskString;
    protected boolean isErrorMessagePrints = true;
    protected IOManager ioManager;

    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоками ввода-вывода
     * @param fieldAskString строка для запроса поля
     */
    public BaseFieldReader(IOManager ioManager, String fieldAskString) {
        this.ioManager = ioManager;
        this.fieldAskString = fieldAskString;
    }

    /**
     * Метод для получения поля, которое прошло проверку на тип, в виде поля
     * @return возвращает поле в виде String
     * @throws InterruptedException если пользователь решил прервать ввод
     */
    protected String getFieldString() throws InterruptedException {
        String output;

        while(true) {
            ioManager.writeLine(fieldAskString);

            try {
                output = ioManager.readLine();
            }
            catch (NoSuchElementException e) {
                throw new InterruptedException();
            }

            if(isInputCorrect(output)) {
                break;
            }

            if(this.isErrorMessagePrints) {
                ioManager.writeError("Некорректное введенное поле");
            }
        }

        return output;
    }

    /**
     * Метод для проверки вводимого значения на тип
     * @param input вводимое поле
     * @return true, если вводимое поле прошло проверку на тип, и false в противном случае
     */
    public abstract boolean isInputCorrect(String input);

    /**
     * Метод для установки fieldAskString
     * @param newFieldAskString новое значение fieldAskString
     */
    public void setFieldAskString(String newFieldAskString) {
        this.fieldAskString = newFieldAskString;
    }
}