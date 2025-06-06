package com.example.client.models;

import java.io.Serializable;
import java.util.Objects;

/**
 * Класс, представляющий дисциплину.
 * Дисциплина содержит название, количество часов лекций, практик, самостоятельной работы и количество лабораторных работ.
 */
public class Discipline implements Comparable<Discipline>, Serializable {
    private String name;
    private Long lectureHours;
    private Long practiceHours;
    private long selfStudyHours;
    private Integer labsCount;

    public Discipline() {}

    /**
     * Конструктор для создания объекта Discipline.
     *
     * @param name           название дисциплины (не может быть null или пустым)
     * @param lectureHours   количество часов лекций (может быть null)
     * @param practiceHours  количество часов практик (не может быть null)
     * @param selfStudyHours количество часов самостоятельной работы
     * @param labsCount      количество лабораторных работ (не может быть null)
     * @throws IllegalArgumentException если нарушены ограничения на поля
     */
    public Discipline(String name, Long lectureHours, Long practiceHours, long selfStudyHours, Integer labsCount) {
        this.name = name;
        this.lectureHours = lectureHours;
        this.practiceHours = practiceHours;
        this.selfStudyHours = selfStudyHours;
        this.labsCount = labsCount;

        if (!validate()) {
            throw new RuntimeException("Неверные аргументы для создания объекта класса Discipline");
        }
    }

    /**
     * Возвращает название дисциплины.
     *
     * @return название дисциплины
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает количество часов лекций.
     *
     * @return количество часов лекций
     */
    public Long getLectureHours() {
        return lectureHours;
    }

    /**
     * Возвращает количество часов практик.
     *
     * @return количество часов практик
     */
    public Long getPracticeHours() {
        return practiceHours;
    }

    /**
     * Возвращает количество часов самостоятельной работы.
     *
     * @return количество часов самостоятельной работы
     */
    public long getSelfStudyHours() {
        return selfStudyHours;
    }

    /**
     * Возвращает количество лабораторных работ.
     *
     * @return количество лабораторных работ
     */
    public Integer getLabsCount() {
        return labsCount;
    }

    /**
     * Метод для проверки валидации объекта
     *
     * @return true, если объект проходит валидацию, false, если не проходит валидацию
     */
    public boolean validate() {
        if (this.name == null || this.name.isEmpty()) {
            return false;
        }

        if (this.lectureHours == null) {
            return false;
        }

        if (this.practiceHours == null) {
            return false;
        }

        if (this.labsCount == null) {
            return false;
        }

        return true;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;
        Discipline that = (Discipline) object;
        return selfStudyHours == that.selfStudyHours &&
                Objects.equals(name, that.name) &&
                Objects.equals(lectureHours, that.lectureHours) &&
                Objects.equals(practiceHours, that.practiceHours) &&
                Objects.equals(labsCount, that.labsCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, lectureHours, practiceHours, selfStudyHours, labsCount);
    }

    @Override
    public String toString() {
        return "Discipline{" +
                "name='" + name + '\'' +
                ", lectureHours=" + lectureHours +
                ", practiceHours=" + practiceHours +
                ", selfStudyHours=" + selfStudyHours +
                ", labsCount=" + labsCount +
                '}';
    }


    @Override
    public int compareTo(Discipline other) {
        if (other == null) {
            throw new NullPointerException("Сравниваемый объект не может быть null");
        }
        return this.name.compareTo(other.name);
    }
}