package ru.mirea;

import java.util.ArrayList;

public class Main {

    public static void main(String[] args) {
        boolean[] flags = new boolean[1];
        ArrayList<String> files = new ArrayList<>();
        Archivator.checkCommandLine(args, flags, files);

        /*System.out.println(flags[0]);
        for (int i = 0; i < files.size(); i++){
            System.out.println(files.get(i));
        }*/
    }
}
