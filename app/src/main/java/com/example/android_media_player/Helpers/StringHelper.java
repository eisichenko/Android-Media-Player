package com.example.android_media_player.Helpers;

import java.util.Locale;

public class StringHelper {
    public static String capitalize(String s) {
        String res = s.replace("_", " ");
        return res.substring(0, 1).toUpperCase() + res.substring(1);
    }

    public static String formatPopularity(Double popularity) {
        if (popularity > 100_000_000) {
            return String.format(Locale.US, "%,.0f", popularity);
        }
        else if (popularity > 10_000_000) {
            return String.format(Locale.US, "%,.1f", popularity);
        }
        else if (popularity > 100_000) {
            return String.format(Locale.US, "%,.2f", popularity);
        }
        else if (popularity > 10_000) {
            return String.format(Locale.US, "%,.4f", popularity);
        }
        else if (popularity > 1_000) {
            return String.format(Locale.US, "%,.7f", popularity);
        }

        return String.format(Locale.US, "%,.10f", popularity);
    }
}
