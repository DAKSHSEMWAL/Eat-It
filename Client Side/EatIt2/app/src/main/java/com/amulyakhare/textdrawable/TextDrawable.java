package com.amulyakhare.textdrawable;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

public class TextDrawable extends Drawable {
    private TextDrawable() {}

    public static class Builder {
        public TextDrawable buildRound(String text, int color) {
            return new TextDrawable();
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void draw(Canvas canvas) {}

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
