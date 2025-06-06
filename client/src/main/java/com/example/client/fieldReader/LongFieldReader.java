package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * LongFieldReader - класс для считывания полей типа long
 */
public class LongFieldReader extends BaseFieldReader {
    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоком ввода-вывода
     * @param fieldAskString строка, которая запрашивает ввод
     */
    public LongFieldReader(IOManager ioManager, String fieldAskString) {
        super(ioManager, fieldAskString);
    }

    /**
     * Метод для проверки вводимого значения на тип
     *
     * @param input вводимое поле
     * @return true, если вводимое поле прошло проверку на тип, и false в противном случае
     */
    @Override
    public boolean isInputCorrect(String input) {
        try {
            Long.parseLong(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Метод для получения поля типа long
     * @return поле типа long
     * @throws InterruptedException если пользователь решил прервать ввод
     */
    public long executeLong() throws InterruptedException {
        var out = this.getFieldString();

        return Long.parseLong(out);
    }
}