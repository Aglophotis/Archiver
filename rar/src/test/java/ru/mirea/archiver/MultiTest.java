package ru.mirea.archiver;

import org.junit.Test;
import ru.mirea.data.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

public class MultiTest {
    private Random r = new Random();

    @Test
    public void multiTest() throws Exception {
        Packer packer = new PackerImpl();
        Unpacker unpacker = new UnpackerImpl();
        Decryptor decryptor = new DecryptorImpl();
        Encryptor encryptor = new EncryptorImpl();
        String path1 = new File(".").getCanonicalPath() + "\\" + "test1";
        String path2 = new File(".").getCanonicalPath() + "\\" + "test2";
        String path4 = new File(".").getCanonicalPath() + "\\" + "test3";
        String path5 = new File(".").getCanonicalPath() + "\\" + "test4";
        String path3 = new File(".").getCanonicalPath() + "\\" + "testing.afk";
        String path6 = new File(".").getCanonicalPath() + "\\" + "testing.afkdec";
        for (int i = 1; i < 50; i++){
            System.out.println(i);
            FileOutputStream fileOutputStream1 = new FileOutputStream(path1);
            FileOutputStream fileOutputStream2 = new FileOutputStream(path2);
            String tmp1 = generateStringLong(i*1000);
            String tmp2 = generateStringShort(i*2000);
            fileOutputStream1.write(tmp1.getBytes(), 0, tmp1.getBytes().length);
            fileOutputStream2.write(tmp2.getBytes(), 0, tmp2.getBytes().length);

            fileOutputStream1.close();
            fileOutputStream2.close();

            File[] files = new File[2];
            files[0] = new File(path1);
            files[1] = new File(path2);
            File outputFile = new File(new File(".").getCanonicalPath() + "\\" + "testing");

            for (File item : files) {
                packer.pack(item, outputFile, false);
            }

            File file1 = new File(path1);
            assertTrue(file1.renameTo(new File(path4)));

            File file2 = new File(path2);
            assertTrue(file2.renameTo(new File(path5)));

            String password = generatePassword((i % 10) + 3);
            encryptor.encryption(password, new File(new File(".").getCanonicalPath() + "\\" + "testing"));

            decryptor.decryption(password, new File(path3));

            unpacker.unpack(new File(path6));

            FileInputStream fileInputStream1 = new FileInputStream(path1);
            FileInputStream fileInputStream2 = new FileInputStream(path2);
            FileInputStream fileInputStream3 = new FileInputStream(path4);
            FileInputStream fileInputStream4 = new FileInputStream(path5);

            while ((fileInputStream1.available() != 0) || (fileInputStream3.available() != 0)){
                assertEquals(fileInputStream1.read(), fileInputStream3.read());
            }

            while ((fileInputStream2.available() != 0) || (fileInputStream4.available() != 0)){
                assertEquals(fileInputStream2.read(), fileInputStream4.read());
            }

            fileInputStream1.close();
            fileInputStream2.close();
            fileInputStream3.close();
            fileInputStream4.close();

            File file3 = new File(path3);
            File file4 = new File(path4);
            File file5 = new File(path5);
            assertTrue(file1.delete());
            assertTrue(file2.delete());
            assertTrue(file3.delete());
            assertTrue(file4.delete());
            assertTrue(file5.delete());
        }
    }

    private String generateStringLong(int size){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append((char) (r.nextInt(1024)));
        }
        return res.toString();
    }

    private String generateStringShort(int size){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append((char) (r.nextInt(80)));
        }
        return res.toString();
    }

    private String generatePassword(int size){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append((char) (r.nextInt(50) + 'a'));
        }
        return res.toString();
    }
}