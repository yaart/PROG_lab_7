package com.example.client.fieldReader;



import com.example.client.iomanager.IOManager;
import com.example.client.models.*;

import java.time.LocalDate;
import java.time.ZonedDateTime;

/**
 * LabWorkFieldReader - класс для чтения объекта LabWork из пользовательского ввода
 */
public class LabWorkFieldReader {
    private final IOManager ioManager;
    private final StringFieldReader nameReader;
    private final CoordinatesFieldReader coordinatesReader;
    private final FloatFieldReader minimalPointReader;
    private final IntegerFieldReader tunedInWorksReader;
    private final DifficultyFieldReader difficultyReader;
    private final DisciplineFieldReader disciplineReader;

    public LabWorkFieldReader(IOManager ioManager) {
        this.ioManager = ioManager;
        this.nameReader = new StringFieldReader(ioManager, "Введите название лабораторной работы:");
        this.coordinatesReader = new CoordinatesFieldReader(ioManager);
        this.minimalPointReader = new FloatFieldReader(ioManager, "Введите минимальный балл:");
        this.tunedInWorksReader = new IntegerFieldReader(ioManager, "Введите количество настроенных работ:");
        this.difficultyReader = new DifficultyFieldReader(ioManager);
        this.disciplineReader = new DisciplineFieldReader(ioManager);
    }

    /**
     * Создает объект LabWork на основе пользовательского ввода
     * @return объект LabWork
     * @throws InterruptedException если ввод был прерван
     */
    public LabWork executeLabWork() throws InterruptedException {
        ioManager.writeLine("\n=== Ввод данных LabWork ===");

        while(true) {
            try {
                // Чтение обязательных полей
                String name = nameReader.executeString();
                Coordinates coordinates = coordinatesReader.executeCoordinates();
                Float minimalPoint = minimalPointReader.executeFloat();
                Integer tunedInWorks = tunedInWorksReader.executeInteger();
                Difficulty difficulty = difficultyReader.executeDifficulty();

                Discipline discipline = null;
                if (askAddDiscipline()) {
                    discipline = disciplineReader.executeDiscipline();
                }

                // Создание объекта
                LabWork labWork = new LabWork(
                        1,
                        name,
                        coordinates,
                        LocalDate.now().toString(), // Автоматическая генерация даты
                        minimalPoint,
                        tunedInWorks,
                        difficulty,
                        discipline
                );

                ioManager.writeLine("LabWork успешно создан");
                return labWork;

            } catch (RuntimeException e) {
                ioManager.writeError("Ошибка: " + e.getMessage());
                ioManager.writeLine("Пожалуйста, повторите ввод\n");
            }
        }
    }

    /**
     * Запрашивает у пользователя необходимость добавления дисциплины
     */
    private boolean askAddDiscipline() throws InterruptedException {
        StringFieldReader choiceReader = new StringFieldReader(ioManager,
                "Добавить дисциплину? (y/n):");

        while(true) {
            String choice = choiceReader.executeString().toLowerCase();
            if(choice.equals("y") || choice.equals("yes")) {
                return true;
            } else if(choice.equals("n") || choice.equals("no")) {
                return false;
            }
            ioManager.writeError("Пожалуйста, введите 'y' или 'n'");
        }
    }
}