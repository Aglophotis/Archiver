package ru.mirea.data;

import java.io.IOException;
import java.util.ArrayList;

public interface Packer {
    void pack(ArrayList<String> files, boolean isCompression) throws Exception;
}
