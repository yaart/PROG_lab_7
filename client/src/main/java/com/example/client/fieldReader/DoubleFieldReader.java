package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * DoubleFieldReader - класс для считывания полей типа double
 */
public class DoubleFieldReader extends BaseFieldReader {
    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоком ввода-вывода
     * @param fieldAskString строка, которая запрашивает ввод
     */
    public DoubleFieldReader(IOManager ioManager, String fieldAskString) {
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
            Double.parseDouble(input);
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
    public double executeDouble() throws InterruptedException {
        var out = this.getFieldString().replace(',', '.');

        return Double.parseDouble(out);
    }
}