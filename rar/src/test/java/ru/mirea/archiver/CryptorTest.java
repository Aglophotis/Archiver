package ru.mirea.archiver;

import org.junit.Test;
import ru.mirea.data.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;

import static org.junit.Assert.*;

public class CryptorTest {
    private Random r = new Random();
    private final int BUFFER_SIZE = 64000;

    @Test
    public void cryptorTest() throws Exception {
        Packer packer = new PackerImpl();
        Encryptor encryptor = new EncryptorImpl();
        Decryptor decryptor = new DecryptorImpl();
        String path1 = new File(".").getCanonicalPath() + "\\" + "test1";
        String path2 = new File(".").getCanonicalPath() + "\\" + "test2";
        String path3 = new File(".").getCanonicalPath() + "\\" + "testing1.afk";
        String path4 = new File(".").getCanonicalPath() + "\\" + "testing2.afk";
        String path5 = new File(".").getCanonicalPath() + "\\" + "testing1.afkdec";
        for (int i = 1; i < 50; i++){
            System.out.println(i);
            FileOutputStream fileOutputStream1 = new FileOutputStream(path1);
            FileOutputStream fileOutputStream2 = new FileOutputStream(path2);
            String tmp1 = generateString(i*1000);
            String tmp2 = generateString(i*2000);
            fileOutputStream1.write(tmp1.getBytes(), 0, tmp1.getBytes().length);
            fileOutputStream2.write(tmp2.getBytes(), 0, tmp2.getBytes().length);

            fileOutputStream1.close();
            fileOutputStream2.close();

            ArrayList<String> files = new ArrayList<>();
            files.add("test1");
            files.add("test2");
            files.add("testing1");

            packer.pack(files, false);

            FileOutputStream fileOutputStream3 = new FileOutputStream(path4);
            FileInputStream fileInputStream1 = new FileInputStream(path3);

            byte[] bytes = new byte[BUFFER_SIZE];
            int quantitySymbols;
            while((quantitySymbols = fileInputStream1.read(bytes, 0, BUFFER_SIZE)) > 0){
                fileOutputStream3.write(bytes, 0, quantitySymbols);
            }

            fileOutputStream3.close();
            fileInputStream1.close();

            String password = generatePassword((i % 12) + 3);

            encryptor.encryption(password, "testing1");

            decryptor.decryption(password, "testing1.afk");

            fileInputStream1 = new FileInputStream(path5);
            FileInputStream fileInputStream2 = new FileInputStream(path4);
            while((fileInputStream1.available() != 0) || (fileInputStream2.available() != 0)){
                assertEquals(fileInputStream1.read(), fileInputStream2.read());
            }

            fileInputStream1.close();
            fileInputStream2.close();

            decryptor.decryption(password + "1", "testing1.afk");

            fileInputStream1 = new FileInputStream(path5);
            fileInputStream2 = new FileInputStream(path4);
            boolean isEquals = true;
            while((fileInputStream1.available() != 0) || (fileInputStream2.available() != 0)){
                if (fileInputStream1.read() != fileInputStream2.read()){
                    isEquals = false;
                    break;
                }
            }
            assertTrue(!isEquals);

            fileInputStream1.close();
            fileInputStream2.close();

            File file1 = new File(path1);
            File file2 = new File(path2);
            File file3 = new File(path4);
            File file4 = new File(path5);
            File file5 = new File(path3);
            assertTrue(file1.delete());
            assertTrue(file2.delete());
            assertTrue(file3.delete());
            assertTrue(file4.delete());
            assertTrue(file5.delete());
        }
    }

    private String generateString(int size){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append((char) (r.nextInt(255)));
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