package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * StringFieldReader - класс для считывания полей типа String
 */
public class StringFieldReader extends BaseFieldReader {
    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоком ввода-вывода
     * @param fieldAskString строка, которая запрашивает ввод
     */
    public StringFieldReader(IOManager ioManager, String fieldAskString) {
        super(ioManager, fieldAskString);
        this.isErrorMessagePrints = false;
    }

    /**
     * Метод для проверки вводимого значения на тип
     *
     * @param input вводимое поле
     * @return true, если вводимое поле прошло проверку на тип, и false в противном случае
     */
    @Override
    public boolean isInputCorrect(String input) {

        return true;
    }

    /**
     * Метод для получения поля типа Long
     * @return поле типа Long
     * @throws InterruptedException если пользователь решил прервать ввод
     */
    public String executeString() throws InterruptedException {
        return this.getFieldString();
    }
}