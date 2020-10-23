package dev.ragnarok.fenrir.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import dev.ragnarok.fenrir.R;

public class BubbleLinearLayout extends LinearLayout {

    //private static final String TAG = BubbleLinearLayout.class.getSimpleName();

    private BubbleDrawable bubbleDrawable;
    private float mArrowWidth;
    private float mAngle;
    private float mArrowHeight;
    private float mArrowPosition;
    private float mRadius;
    private BubbleDrawable.ArrowLocation mArrowLocation;
    private int bubbleColor;
    private int apply_gradient_bubbleColor;

    public BubbleLinearLayout(Context context) {
        super(context);
        initView(null);
    }

    public BubbleLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    private void initView(AttributeSet attrs) {
        setWillNotDraw(false);
        if (attrs != null) {
            @SuppressLint("CustomViewStyleable") TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.BubbleView);
            mArrowWidth = array.getDimension(R.styleable.BubbleView_arrowWidth, BubbleDrawable.Builder.DEFAULT_ARROW_WITH);
            mArrowHeight = array.getDimension(R.styleable.BubbleView_arrowHeight, BubbleDrawable.Builder.DEFAULT_ARROW_HEIGHT);
            mAngle = array.getDimension(R.styleable.BubbleView_angle, BubbleDrawable.Builder.DEFAULT_ANGLE);
            mArrowPosition = array.getDimension(R.styleable.BubbleView_arrowPosition, BubbleDrawable.Builder.DEFAULT_ARROW_POSITION);
            mRadius = array.getDimension(R.styleable.BubbleView_cornerRadius, BubbleDrawable.Builder.DEFAULT_RADIUS);
            bubbleColor = array.getColor(R.styleable.BubbleView_bubbleColor, BubbleDrawable.Builder.DEFAULT_BUBBLE_COLOR);
            apply_gradient_bubbleColor = array.getColor(R.styleable.BubbleView_secondary_bubbleColor, BubbleDrawable.Builder.DEFAULT_BUBBLE_COLOR);
            int location = array.getInt(R.styleable.BubbleView_arrowLocation, 0);
            mArrowLocation = BubbleDrawable.ArrowLocation.mapIntToValue(location);
            array.recycle();
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if (w > 0 && h > 0) {
            setUp(w, h);
        }
    }

    private void setUp(int left, int right, int top, int bottom) {
        //Logger.d(TAG, "setUp, left: " + left + ", right: " + right);

        if (right < left || bottom < top) {
            return;
        }

        RectF rectF = new RectF(left, top, right, bottom);
        bubbleDrawable = new BubbleDrawable.Builder()
                .rect(rectF)
                .arrowLocation(mArrowLocation)
                .bubbleType(BubbleDrawable.BubbleType.COLOR)
                .angle(mAngle)
                .arrowHeight(mArrowHeight)
                .arrowWidth(mArrowWidth)
                .arrowPosition(mArrowPosition)
                .cornerRadius(mRadius)
                .bubbleColorGradient(bubbleColor, apply_gradient_bubbleColor)
                .build();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        setUp();
    }

    private void setUp(int width, int height) {
        setUp(0, width, 0, height);
        //setBackgroundDrawable(bubbleDrawable); // comment by R.Kolbasa
    }

    private void setUp() {
        setUp(getWidth(), getHeight());
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (bubbleDrawable != null) {
            bubbleDrawable.draw(canvas);
        }

        super.onDraw(canvas);
    }

    public void setNonGradientColor(int ARGBColor) {
        bubbleColor = Color.argb(Color.alpha(ARGBColor), Color.red(ARGBColor), Color.green(ARGBColor), Color.blue(ARGBColor));
        apply_gradient_bubbleColor = bubbleColor;
        setUp();
    }

    public void setGradientColor(int first, int second) {
        bubbleColor = Color.argb(Color.alpha(first), Color.red(first), Color.green(first), Color.blue(first));
        apply_gradient_bubbleColor = Color.argb(Color.alpha(second), Color.red(second), Color.green(second), Color.blue(second));
        setUp();
    }

    public void setBubbleColor(int ARGBColor) {
        bubbleColor = Color.argb(Color.alpha(ARGBColor), Color.red(ARGBColor), Color.green(ARGBColor), Color.blue(ARGBColor));
        setUp();
    }

    public void setSecondaryBubbleColor(int ARGBColor) {
        apply_gradient_bubbleColor = Color.argb(Color.alpha(ARGBColor), Color.red(ARGBColor), Color.green(ARGBColor), Color.blue(ARGBColor));
        setUp();
    }
}