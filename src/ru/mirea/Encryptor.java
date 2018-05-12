package ru.mirea;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class Encryptor {
    private final static int BUFFER_SIZE = 64000;

    public static void encryption(String password, String filename) throws IOException {
        ArrayList<Integer> lengthSubblock = new ArrayList<>();
        byte[] bytes = new byte[BUFFER_SIZE];
        int quantitySymbols;
        for (int i = 0; i < password.length(); i++){
            int tmp = (byte)password.charAt(i);
            if (tmp < 15 && tmp > 0)
                throw new IOException("Incorrect password");
            if (tmp > 0)
                lengthSubblock.add(tmp);
            else
                lengthSubblock.add(256 + tmp);
        }


        String path = new File(".").getCanonicalPath() + "\\original\\" + filename;
        if (!(new File(path).exists()))
            throw new IllegalArgumentException("Unknown name of file");
        final FileInputStream fileInputStream = new FileInputStream(path);
        path = new File(".").getCanonicalPath() + "\\original\\" + filename + "enc";
        final FileOutputStream fileOutputStream = new FileOutputStream(path);
        while ((quantitySymbols = fileInputStream.read(bytes, 0, BUFFER_SIZE)) > 0){
            StringBuilder binarySequence = new StringBuilder();
            for (int i = 0; i < quantitySymbols; i++){
                int tmpByte = (bytes[i] < 0) ? (256 + bytes[i]) : bytes[i];
                String str1 = Integer.toBinaryString(tmpByte);
                StringBuilder str2 = new StringBuilder();
                while (str1.length() + str2.length() < 8){
                    str2.append("0");
                }
                binarySequence.append(str2.append(str1));
            }
            StringBuilder reverseSequence = encrypt(binarySequence, lengthSubblock);
            StringBuilder result = Compressor.bitsToString(reverseSequence.toString());
            char[] characters = result.toString().toCharArray();
            byte[] tmpBytes = new byte[characters.length];
            for (int j = 0; j < characters.length; j++) {
                tmpBytes[j] = (byte) characters[j];
            }
            fileOutputStream.write(tmpBytes, 0, tmpBytes.length);
        }
        fileInputStream.close();
        fileOutputStream.close();
    }

    private static StringBuilder encrypt(StringBuilder binarySequence, ArrayList<Integer> lengthSubblock){
        int i = 0;
        int k = 0;
        StringBuilder reverseSequence = new StringBuilder();
        while (i < binarySequence.length()){
            StringBuilder tmpStr = new StringBuilder();
            if (i + lengthSubblock.get(k % lengthSubblock.size()) < binarySequence.length()) {
                for (int j = 0; j < lengthSubblock.get(k % lengthSubblock.size()); ++j, ++i) {
                    tmpStr.append(binarySequence.charAt(i));
                }
            }
            else {
                for (int j = i; j < binarySequence.length(); j++, i++){
                    tmpStr.append(binarySequence.charAt(i));
                }
            }
            ++k;
            reverseSequence.append(tmpStr.reverse());
        }
        return reverseSequence.reverse();
    }
}
