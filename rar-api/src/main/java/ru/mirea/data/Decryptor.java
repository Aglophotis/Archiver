package ru.mirea.data;

import java.io.File;

public interface Decryptor {
    int decryption(String password, File inputFile) throws Exception;
}
