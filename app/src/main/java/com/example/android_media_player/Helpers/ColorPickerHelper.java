package com.example.android_media_player.Helpers;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;
import android.widget.SeekBar;

import androidx.appcompat.app.ActionBar;

import com.example.android_media_player.MainActivity;

public class ColorPickerHelper {
    public static Integer getColorFromSharedPreferences(SharedPreferences settings) {
        String colorPickerLastColor = settings.getString(MainActivity.COLOR_PICKER_LAST_COLOR_CACHE_NAME, MainActivity.DEFAULT_APP_COLOR);

        if (colorPickerLastColor != null) {
            return Color.parseColor(colorPickerLastColor);
        }

        return null;
    }

    public static void setActionBarColor(ActionBar actionBar, SharedPreferences settings) {
        Integer color = getColorFromSharedPreferences(settings);

        if (color != null) {
            actionBar.setBackgroundDrawable(new ColorDrawable(color));
        }
    }

    public static void setSeekBarColor(SeekBar seekBar, SharedPreferences settings) {
        Integer color = getColorFromSharedPreferences(settings);

        if (color != null) {
            seekBar.getProgressDrawable().setColorFilter(color, PorterDuff.Mode.MULTIPLY);
            seekBar.getThumb().setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
        }
    }

    public static void setImageViewColor(ImageView imageView, SharedPreferences settings) {
        Integer color = getColorFromSharedPreferences(settings);

        if (color != null) {
            imageView.setColorFilter(color);
        }
    }
}
