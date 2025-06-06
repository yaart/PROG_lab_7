package com.example.client.models;

import java.io.Serializable;
/**
 * Класс, представляющий координаты.
 * Координаты состоят из двух полей: x (должно быть больше -263) и y.
 */
public class Coordinates implements Serializable {
    private long x; // Значение поля должно быть больше -263
    private int y;

    /**
     * Конструктор класса.
     *
     * @param x значение координаты x (должно быть больше -263)
     * @param y значение координаты y
     */
    public Coordinates(long x, int y) {
        this.x = x;
        this.y = y;

        if (!validate()) {
            throw new RuntimeException("Координата x должна быть больше -263");
        }
    }

    /**
     * Конструктор по умолчанию.
     */
    public Coordinates() {
    }

    /**
     * Возвращает значение поля x.
     */
    public long getX() {
        return x;
    }

    /**
     * Возвращает значение поля y.
     */
    public int getY() {
        return y;
    }

    /**
     * Метод для проверки валидации объекта
     *
     * @return true, если объект проходит валидацию, false, если не проходит валидацию
     */
    public boolean validate() {
        if (this.x <= -263) {
            return false;
        }
        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Coordinates that = (Coordinates) object;
        return x == that.x && y == that.y;
    }

    @Override
    public int hashCode() {
        return 31 * Long.hashCode(x) + Integer.hashCode(y);
    }

    @Override
    public String toString() {
        return "Координаты: (" +
                "x=" + x +
                ", y=" + y +
                ')';
    }
}