package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * LongWrapperFieldReader - класс для считывания полей типа Long.
 */
public class LongWrapperFieldReader extends BaseFieldReader {

    /**
     * Конструктор класса
     *
     * @param ioManager      класс для работы с потоками ввода-вывода
     * @param fieldAskString строка для запроса поля
     */
    public LongWrapperFieldReader(IOManager ioManager, String fieldAskString) {
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
            Long.valueOf(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Метод для считывания поля типа Long
     * @return поле типа Long
     * @throws InterruptedException, если пользователь решил прервать ввод
     */
    public Long executeLong() throws InterruptedException {
        var out = this.getFieldString();
        return Long.valueOf(out);
    }
}
