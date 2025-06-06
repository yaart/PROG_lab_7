package com.example.client.fieldReader;


import com.example.client.iomanager.IOManager;
import com.example.client.models.Coordinates;

/**
 * CoordinatesFieldReader - класс для создания объекта Coordinates через пользовательский ввод.
 */
public class CoordinatesFieldReader {
    private final IOManager ioManager;

    /**
     * Конструктор класса
     * @param ioManager класс для работы с вводом-выводом
     */
    public CoordinatesFieldReader(IOManager ioManager) {
        this.ioManager = ioManager;
    }

    /**
     * Метод для получения объекта Label с использованием пользовательских данных
     * @throws InterruptedException если пользователь прервал ввод
     */
    public Coordinates executeCoordinates() throws InterruptedException {
        ioManager.writeLine("Ввод значений полей Coordinates");
        while (true) {

            LongFieldReader xReader = new LongFieldReader(this.ioManager, "Введите x: ");
            IntFieldReader yReader = new IntFieldReader(this.ioManager, "Введите y: ");

            var x = xReader.executeLong();
            var y = yReader.executeInt();

            try {
                Coordinates coordinates = new Coordinates(x, y);
                return coordinates;
            }
            catch(RuntimeException e) {
                ioManager.writeError(e.getMessage());
            }
        }
    }
}