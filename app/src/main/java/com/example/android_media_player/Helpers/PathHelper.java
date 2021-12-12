package com.example.android_media_player.Helpers;

import android.net.Uri;
import android.os.Environment;

import java.util.ArrayList;
import java.util.Arrays;

public class PathHelper {
    public static String getAbsolutePathStringFromUri(Uri uri) {
        if (uri.toString().startsWith("file:///")) {
            return uri.getPath();
        }

        ArrayList<String> strings = new ArrayList<>(Arrays.asList(uri.toString().split("/")));

        String pathString = Uri.decode(strings.get(strings.size() - 1));

        ArrayList<String> pathStrings = new ArrayList<>(Arrays.asList(pathString.split(":")));

        if (pathStrings.get(0).equals("primary")) {
            pathStrings.set(0, Environment.getExternalStorageDirectory().getPath());
        }
        else {
            pathStrings.add(0, "/storage");
        }

        String res = pathCombine(pathStrings);

        if (!res.endsWith("/")) {
            return res + "/";
        }
        return res;
    }

    public static String pathCombine(ArrayList<String> pathStrings) {
        StringBuilder res = new StringBuilder();

        for (String str : pathStrings) {
            if (res.length() == 0) {
                res.append(str);
            }
            else if (res.charAt(res.length() - 1) == '/') {
                res.append(str);
            }
            else {
                res.append('/').append(str);
            }
        }

        return res.toString();
    }

    public static String addSlash(String s) {
        if (s.endsWith("/")) {
            return s;
        }
        return s + "/";
    }


    public static ArrayList<String> getSubfoldersFromFilePath(String rootPath, String filePath) {
        StringBuilder sb = new StringBuilder(filePath);
        sb.delete(0, rootPath.length());

        if (sb.indexOf("/") < 0) {
            return new ArrayList<>();
        }

        sb.delete(sb.lastIndexOf("/"), sb.length());

        int checkTo = sb.length() - 1;

        ArrayList<String> subfolders = new ArrayList<>();
        subfolders.add(addSlash(sb.toString()));

        while (checkTo >= 0) {
            int index = sb.lastIndexOf("/", checkTo);
            if (index >= 0 && index < sb.length()) {
                String subfolder = sb.substring(0, index);
                subfolders.add(addSlash(subfolder));
                checkTo = index - 1;
            }
            else {
                break;
            }
        }

        return subfolders;

    }
}
