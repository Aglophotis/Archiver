package ru.mirea;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String[] args) throws IOException {
        boolean[] flags = new boolean[1];
        ArrayList<String> files = new ArrayList<>();
        Archivator.checkCommandLine(args, flags, files);

        if (flags[0])
            Archivator.pack(files);
        else
            Archivator.unpack(files);
    }
}
