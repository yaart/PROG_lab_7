package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * IntegerFieldReader - класс для считывания полей типа Integer
 */
public class IntegerFieldReader extends BaseFieldReader {
    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоком ввода-вывода
     * @param fieldAskString строка, которая запрашивает ввод
     */
    public IntegerFieldReader(IOManager ioManager, String fieldAskString) {
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
            Integer.parseInt(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Метод для получения значения Integer, вводимого пользователем
     * @return значение типа Integer
     * @throws InterruptedException если пользователь решил прервать ввод
     */
    public Integer executeInteger() throws InterruptedException {
        var out = this.getFieldString();

        return Integer.valueOf(out);
    }
}