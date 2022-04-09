package com.example.android_media_player.Helpers;

public class StringHelper {
    public static String capitalize(String s) {
        String res = s.replace("_", " ");
        return res.substring(0, 1).toUpperCase() + res.substring(1);
    }
}
