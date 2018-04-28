package ru.mirea;

import java.util.ArrayList;

public class Archivator {
    public static void checkCommandLine(String[] args, boolean[] flags, ArrayList<String> files){
        int flagsQuantity = 0;
            while (args[flagsQuantity].charAt(0) == '-') {
                switch (args[flagsQuantity]){
                    case "-pack":
                        flags[0] = true;
                        break;
                    case "-unpack":
                        flags[0] = false;
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid flag: " + args[flagsQuantity]);
                }
                flagsQuantity++;
            }

            if (flagsQuantity == 0) {
                throw new IllegalArgumentException("incorrect number of flags");
            }

            if ((flags[0] && args.length-flagsQuantity-1 > 0) || (!flags[0] && args.length-flagsQuantity > 0)) {
                for (int i = flagsQuantity; i < args.length; i++) {
                    files.add(args[i]);
                }
            }
            else{
                throw new IllegalArgumentException("incorrect number of files");
            }
        }
    }
