package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * FloatFieldReader - класс для считывания полей типа double
 */
public class FloatFieldReader extends BaseFieldReader {
    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоком ввода-вывода
     * @param fieldAskString строка, которая запрашивает ввод
     */
    public FloatFieldReader(IOManager ioManager, String fieldAskString) {
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
        input = input.replace(',', '.');
        try {
            Float.parseFloat(input);
            return true;
        }
        catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Метод для получения значения double, вводимого пользователем
     * @return значение типа double
     * @throws InterruptedException если пользователь решил прервать ввод
     */
    public float executeFloat() throws InterruptedException {
        var out = this.getFieldString().replace(',', '.');

        return Float.parseFloat(out);
    }
}