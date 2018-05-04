package ru.mirea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static Random r = new Random();

    public static void main(String[] args) throws IOException {
        boolean[] flags = new boolean[2];
        ArrayList<String> files = new ArrayList<>();
        Archiver.checkCommandLine(args, flags, files);

        if (flags[0])
            Archiver.pack(files);
        else
            Archiver.unpack(files);
        //for (int i = 0; i < 10; i++)
        //    HuffmanCompression.compression("ffhhgyffhgg");
    }

    private static String generateString(int size){
        String res = "";
        for (int i = 0; i < size; i++) {
            res += (char) (r.nextInt(150) + 'a');
        }
        return res;
    }
}
