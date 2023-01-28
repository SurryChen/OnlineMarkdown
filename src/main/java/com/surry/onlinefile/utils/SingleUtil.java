package com.surry.onlinefile.utils;

public class SingleUtil {

    public static String single(String name, Object novel) {

        return "{\"" + name + "\":\"" + novel.toString() + "\"}";

    }

}
