package dev.ragnarok.fenrir.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.util.Utils;

public class CircleImageButton extends AppCompatImageView {

    public CircleImageButton(Context context) {
        this(context, null);
    }

    public CircleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public void init(Context context, AttributeSet attrs) {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleImageButton);

        setBackgroundResource(R.drawable.circle_back_white);

        try {
            int bgColor = a.getColor(R.styleable.CircleImageButton_backgroundColor, Color.RED);
            int iconColor = a.getColor(R.styleable.CircleImageButton_iconColor, Color.WHITE);

            Utils.setColorFilter(getBackground(), bgColor);
            Utils.setColorFilter(this, iconColor);
        } finally {
            a.recycle();
        }
    }
}
