package ru.mirea.archiver;

import ru.mirea.data.*;

import java.util.ArrayList;

public class Menu {

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        boolean[] flags = new boolean[3];
        ArrayList<String> files = new ArrayList<>();
        StringBuilder password = new StringBuilder();
        HandlerCommandLine.checkCommandLine(args, flags, files, password);

        Packer packer = new PackerImpl();
        Unpacker unpacker = new UnpackerImpl();
        Encryptor encryptor = new EncryptorImpl();
        Decryptor decryptor = new DecryptorImpl();



        if (flags[0]) {
            packer.pack(files, flags[1]);
            if (flags[2]) {
                encryptor.encryption(password.toString(), files.get(files.size() - 1));
            }
        }
        else {
            if (flags[2]){
                for (String item: files) {
                    decryptor.decryption(password.toString(), item);
                    unpacker.unpack(item + "dec");
                }
            }
            else {
                for (String item: files) {
                    unpacker.unpack(item);
                }
            }
        }


        long timeSpent = System.currentTimeMillis() - startTime;
        System.out.println(timeSpent);
    }
}
