package ru.mirea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static Random r = new Random();
    private static long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException {
        boolean[] flags = new boolean[2];
        ArrayList<String> files = new ArrayList<>();
        HandlerCommandLine.checkCommandLine(args, flags, files);

        if (flags[0])
            Packer.pack(files, flags[1]);
        else
            Unpacker.unpack(files);

        long timeSpent = System.currentTimeMillis() - startTime;
        System.out.println(timeSpent);
    }

    private static String generateString(int size){
        String res = "";
        for (int i = 0; i < size; i++) {
            res += (char) (r.nextInt(150) + 'a');
        }
        return res;
    }
}
