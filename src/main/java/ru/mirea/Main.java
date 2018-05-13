package ru.mirea;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

public class Main {
    private static Random r = new Random();
    private static long startTime = System.currentTimeMillis();

    public static void main(String[] args) throws IOException, InterruptedException {
        boolean[] flags = new boolean[3];
        ArrayList<String> files = new ArrayList<>();
        StringBuilder password = new StringBuilder();
        HandlerCommandLine.checkCommandLine(args, flags, files, password);

        if (flags[0]) {
            Packer.pack(files, flags[1]);
            if (flags[2]) {
                Encryptor.encryption(password.toString(), files.get(files.size() - 1));
            }
        }
        else {
            if (flags[2]){
                for (String item: files) {
                    Decryptor.decryption(password.toString(), item);
                    Unpacker.unpack(item + "dec");
                }
            }
            else {
                for (String item: files) {
                    Unpacker.unpack(item);
                }
            }
        }


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
