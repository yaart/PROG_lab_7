package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;

/**
 * IntFieldReader - класс для считывания полей типа int
 */
public class IntFieldReader extends BaseFieldReader {
    /**
     * Конструктор класса
     * @param ioManager класс для работы с потоком ввода-вывода
     * @param fieldAskString строка, которая запрашивает ввод
     */
    public IntFieldReader(IOManager ioManager, String fieldAskString) {
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
     * Метод для считвания поля типа int
     * @return поле типа int
     * @throws InterruptedException если пользователь решил прервать ввод
     */
    public int executeInt() throws InterruptedException {
        var ouput = this.getFieldString();

        return Integer.parseInt(ouput);
    }

}