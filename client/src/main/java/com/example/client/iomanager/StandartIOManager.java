package com.example.client.iomanager;



import java.util.Scanner;

/**
 * Реализация IOManager для работы с консолью.
 */
public class StandartIOManager implements IOManager {
    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void write(Object obj) {
        System.out.print(obj);
    }

    @Override
    public void writeLine(Object obj) {
        System.out.println(obj);
    }

    @Override
    public void writeError(Object obj) {
        System.err.println(obj);
    }

    @Override
    public String readLine() {
        return scanner.nextLine();
    }

}