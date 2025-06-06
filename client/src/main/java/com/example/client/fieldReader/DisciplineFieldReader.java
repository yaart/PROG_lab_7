package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;
import com.example.client.models.Discipline;

/**
 * DisciplineFieldReader - класс для чтения объекта Discipline из пользовательского ввода
 */
public class DisciplineFieldReader {
    private final IOManager ioManager;
    private final StringFieldReader nameReader;
    private final LongWrapperFieldReader lectureHoursReader;
    private final LongWrapperFieldReader practiceHoursReader;
    private final LongFieldReader selfStudyHoursReader;
    private final IntegerFieldReader labsCountReader;

    /**
     * Конструктор класса
     * @param ioManager менеджер ввода-вывода
     */
    public DisciplineFieldReader(IOManager ioManager) {
        this.ioManager = ioManager;
        this.nameReader = new StringFieldReader(ioManager, "Введите название дисциплины:");
        this.lectureHoursReader = new LongWrapperFieldReader(ioManager, "Введите количество часов лекций:");
        this.practiceHoursReader = new LongWrapperFieldReader(ioManager,  "Введите количество часов практик:");
        this.selfStudyHoursReader = new LongFieldReader(ioManager, "Введите количество часов самостоятельной работы:");
        this.labsCountReader = new IntegerFieldReader(ioManager, "Введите количество лабораторных работ:");
    }

    /**
     * Выполняет чтение данных и создание объекта Discipline
     * @return объект Discipline
     * @throws InterruptedException если ввод был прерван
     */
    public Discipline executeDiscipline() throws InterruptedException {
        ioManager.writeLine("\n=== Ввод данных дисциплины ===");

        while(true) {
            try {
                String name = nameReader.executeString();
                Long lectureHours = lectureHoursReader.executeLong();
                Long practiceHours = practiceHoursReader.executeLong();
                long selfStudyHours = selfStudyHoursReader.executeLong();
                Integer labsCount = labsCountReader.executeInteger();

                Discipline discipline = new Discipline(
                        name,
                        lectureHours,
                        practiceHours,
                        selfStudyHours,
                        labsCount
                );

                return discipline;

            } catch (RuntimeException e) {
                ioManager.writeError("Ошибка: " + e.getMessage());
                ioManager.writeLine("Пожалуйста, повторите ввод\n");
            }
        }
    }
}