package com.cepheuen.elegantnumberbutton.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class ElegantNumberButton extends FrameLayout {
    private String number = "1";

    public ElegantNumberButton(Context context) {
        super(context);
    }

    public ElegantNumberButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ElegantNumberButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public void setOnValueChangeListener(OnValueChangeListener listener) {}

    public interface OnValueChangeListener {
        void onValueChange(ElegantNumberButton view, int oldValue, int newValue);
    }
}
