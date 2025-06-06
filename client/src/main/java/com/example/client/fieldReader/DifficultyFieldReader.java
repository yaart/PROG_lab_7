package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;
import com.example.client.models.Difficulty;

/**
 * DifficultyFieldReader - класс для чтения значения Difficulty из пользовательского ввода
 */
public class DifficultyFieldReader extends BaseFieldReader {
    /**
     * Конструктор с автоматической генерацией строки запроса
     *
     * @param ioManager менеджер ввода-вывода
     */
    public DifficultyFieldReader(IOManager ioManager) {
        super(ioManager, "");
        this.setFieldAskString(generateFieldAskString());
    }

    /**
     * Конструктор с пользовательской строкой запроса
     *
     * @param ioManager      менеджер ввода-вывода
     * @param fieldAskString кастомное приглашение для ввода
     */
    public DifficultyFieldReader(IOManager ioManager, String fieldAskString) {
        super(ioManager, fieldAskString);
    }

    /**
     * Генерирует строку с доступными значениями Difficulty
     *
     * @return строка приглашения для ввода
     */
    private String generateFieldAskString() {
        StringBuilder fieldAskString = new StringBuilder("Введите сложность (");

        for (Difficulty difficulty : Difficulty.values()) {
            fieldAskString.append(difficulty.name()).append(", ");
        }

        // Удаляем последнюю запятую и пробел
        fieldAskString.setLength(fieldAskString.length() - 2);
        fieldAskString.append("): ");

        return fieldAskString.toString();
    }

    /**
     * Проверяет корректность введенного значения
     *
     * @param input введенная строка
     * @return true если значение валидно
     */
    @Override
    public boolean isInputCorrect(String input) {
        try {
            Difficulty.valueOf(input.toUpperCase());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * Выполняет чтение значения Difficulty
     *
     * @return значение перечисления Difficulty
     * @throws InterruptedException если ввод был прерван
     */
    public Difficulty executeDifficulty() throws InterruptedException {
        var out = getFieldString().toUpperCase();
        return Difficulty.valueOf(out);
    }

}

