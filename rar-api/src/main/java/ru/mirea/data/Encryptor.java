package ru.mirea.data;

import java.io.File;

public interface Encryptor {
    int encryption(String password, File outputFile) throws Exception;
}
