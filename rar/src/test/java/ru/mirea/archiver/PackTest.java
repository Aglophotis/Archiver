package ru.mirea.archiver;

import org.junit.Test;
import ru.mirea.data.Packer;
import ru.mirea.data.PackerImpl;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Random;


public class PackTest {
    private Random r = new Random();

    @Test
    public void packOneTest() throws Exception {
        Packer packer = new PackerImpl();
        String path1 = new File(".").getCanonicalPath() + "\\" + "test1";
        String path3 = new File(".").getCanonicalPath() + "\\" + "testing.afk";
        for (int i = 1; i < 50; i++){
            System.out.println(i);
            FileOutputStream fileOutputStream1 = new FileOutputStream(path1);
            String tmp1 = generateString(i*1000);
            fileOutputStream1.write(tmp1.getBytes(), 0, tmp1.getBytes().length);
            fileOutputStream1.close();

            File[] files = new File[1];
            files[0] = new File(path1);
            File outputFile = new File(new File(".").getCanonicalPath() + "\\" + "testing");

            for (File item : files) {
                packer.pack(item, outputFile, false);
            }


            FileInputStream fileInputStream1 = new FileInputStream(path3);

            StringBuilder str = new StringBuilder();
            StringBuilder strRes = new StringBuilder();
            strRes.append("0test1:" + i*1000 + ":");
            for (int j = 0; j < strRes.length(); j++){
                str.append((char)fileInputStream1.read());
            }
            assertEquals(str.toString(), strRes.toString());

            str.delete(0, str.length());
            strRes.delete(0, strRes.length());
            FileInputStream fileInputStream2 = new FileInputStream(path1);

            for (int j = 0; j < i*1000; j++){
                str.append((char)fileInputStream1.read());
                strRes.append((char)fileInputStream2.read());
            }
            assertEquals(str.toString(), strRes.toString());

            fileInputStream2.close();
            str.delete(0, str.length());
            strRes.delete(0, strRes.length());

            assertEquals(fileInputStream1.available(), 0);
            fileInputStream1.close();
            File file1 = new File(path1);
            File file3 = new File(path3);
            assertTrue(file1.delete());
            assertTrue(file3.delete());
        }
    }

    @Test
    public void packTwoTest() throws Exception {
        Packer packer = new PackerImpl();
        String path1 = new File(".").getCanonicalPath() + "\\" + "test1";
        String path2 = new File(".").getCanonicalPath() + "\\" + "test2";
        String path3 = new File(".").getCanonicalPath() + "\\" + "testing.afk";
        for (int i = 1; i < 50; i++){
            System.out.println(i);
            FileOutputStream fileOutputStream1 = new FileOutputStream(path1);
            FileOutputStream fileOutputStream2 = new FileOutputStream(path2);
            String tmp1 = generateString(i*1000);
            String tmp2 = generateString(i*2000);
            fileOutputStream1.write(tmp1.getBytes(), 0, tmp1.getBytes().length);
            fileOutputStream1.close();
            fileOutputStream2.write(tmp2.getBytes(), 0, tmp2.getBytes().length);
            fileOutputStream2.close();

            File[] files = new File[2];
            files[0] = new File(path1);
            files[1] = new File(path2);
            File outputFile = new File(new File(".").getCanonicalPath() + "\\" + "testing");

            for (File item : files) {
                packer.pack(item, outputFile, false);
            }


            FileInputStream fileInputStream1 = new FileInputStream(path3);

            StringBuilder str = new StringBuilder();
            StringBuilder strRes = new StringBuilder();
            strRes.append("0test1:" + i*1000 + ":");
            for (int j = 0; j < strRes.length(); j++){
                str.append((char)fileInputStream1.read());
            }
            assertEquals(str.toString(), strRes.toString());

            str.delete(0, str.length());
            strRes.delete(0, strRes.length());
            FileInputStream fileInputStream2 = new FileInputStream(path1);

            for (int j = 0; j < i*1000; j++){
                str.append((char)fileInputStream1.read());
                strRes.append((char)fileInputStream2.read());
            }
            assertEquals(str.toString(), strRes.toString());
            fileInputStream2.close();

            str.delete(0, str.length());
            strRes.delete(0, strRes.length());

            strRes.append("0test2:" + i*2000 + ":");
            for (int j = 0; j < strRes.length(); j++){
                str.append((char)fileInputStream1.read());
            }
            assertEquals(str.toString(), strRes.toString());

            str.delete(0, str.length());
            strRes.delete(0, strRes.length());
            FileInputStream fileInputStream3 = new FileInputStream(path2);

            for (int j = 0; j < i*2000; j++){
                str.append((char)fileInputStream1.read());
                strRes.append((char)fileInputStream3.read());
            }
            assertEquals(str.toString(), strRes.toString());
            fileInputStream3.close();

            assertEquals(fileInputStream1.available(), 0);
            fileInputStream1.close();

            File file1 = new File(path1);
            File file2 = new File(path2);
            File file3 = new File(path3);
            assertTrue(file1.delete());
            assertTrue(file2.delete());
            assertTrue(file3.delete());
        }
    }

    private String generateString(int size){
        StringBuilder res = new StringBuilder();
        for (int i = 0; i < size; i++) {
            res.append((char) (r.nextInt(1024)));
        }
        return res.toString();
    }

}