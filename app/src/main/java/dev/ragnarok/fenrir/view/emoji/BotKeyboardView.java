package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Keyboard;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Utils;

public class BotKeyboardView extends ScrollView {

    private final LinearLayout container;
    private final ArrayList<View> buttonViews = new ArrayList<>();
    private final boolean isFullSize = Settings.get().ui().isEmojis_full_screen();
    private List<List<Keyboard.Button>> botButtons;
    private BotKeyboardViewDelegate delegate;
    private int panelHeight;
    private int buttonHeight;

    private final ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
        @Override
        public void onGlobalLayout() {
            Rect r = new Rect();
            if (this == null) {
                return;
            }
            getWindowVisibleDisplayFrame(r);

            int screenHeight = getRootView().getHeight();
            int heightDifference = screenHeight - (r.bottom - r.top);

            int navBarHeight = getContext().getResources().getIdentifier("navigation_bar_height", "dimen", "android");

            if (navBarHeight > 0) {
                heightDifference -= getContext().getResources().getDimensionPixelSize(navBarHeight);
            }

            int statusbarHeight = getContext().getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (statusbarHeight > 0) {
                heightDifference -= getContext().getResources().getDimensionPixelSize(statusbarHeight);
            }

            if (heightDifference > 200) {
                setPanelHeight(heightDifference);
            }
        }
    };

    public BotKeyboardView(Context context) {
        super(context);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        addView(container);
        listenKeyboardSize();
    }

    public BotKeyboardView(Context context, AttributeSet attrs) {
        super(context, attrs);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        addView(container);
        listenKeyboardSize();
    }

    public BotKeyboardView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        addView(container);
        listenKeyboardSize();
    }

    public BotKeyboardView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        container = new LinearLayout(context);
        container.setOrientation(LinearLayout.VERTICAL);
        addView(container);
        listenKeyboardSize();
    }

    private static int getSize(float size) {
        return (int) (size < 0 ? size : Utils.dp(size));
    }

    private static LinearLayout.LayoutParams createLinear(int width, int height, float leftMargin, float topMargin, float rightMargin, float bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height));
        layoutParams.setMargins(Utils.dp(leftMargin), Utils.dp(topMargin), Utils.dp(rightMargin), Utils.dp(bottomMargin));
        return layoutParams;
    }

    private static LinearLayout.LayoutParams createLinear(int width, int height, float weight, int leftMargin, int topMargin, int rightMargin, int bottomMargin) {
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(getSize(width), getSize(height), weight);
        layoutParams.setMargins(Utils.dp(leftMargin), Utils.dp(topMargin), Utils.dp(rightMargin), Utils.dp(bottomMargin));
        return layoutParams;
    }

    public void setDelegate(BotKeyboardViewDelegate botKeyboardViewDelegate) {
        delegate = botKeyboardViewDelegate;
    }

    public void invalidateViews() {
        for (int a = 0; a < buttonViews.size(); a++) {
            buttonViews.get(a).invalidate();
        }
    }

    public boolean setButtons(@Nullable List<List<Keyboard.Button>> buttons) {
        if (botButtons == null ? buttons == null : botButtons.equals(buttons)) {
            return false;
        }
        botButtons = buttons;
        container.removeAllViews();
        buttonViews.clear();
        scrollTo(0, 0);

        if (buttons != null && buttons.size() != 0) {
            buttonHeight = !isFullSize ? 42 : (int) Math.max(42, (panelHeight - Utils.dp(30) - (botButtons.size() - 1) * Utils.dp(10)) / botButtons.size() / Utils.getDensity());
            for (int a = 0; a < buttons.size(); a++) {
                List<Keyboard.Button> row = buttons.get(a);

                LinearLayout layout = new LinearLayout(getContext());
                layout.setOrientation(LinearLayout.HORIZONTAL);
                container.addView(layout, createLinear(ViewGroup.LayoutParams.MATCH_PARENT, buttonHeight, 15, a == 0 ? 15 : 10, 15, a == buttons.size() - 1 ? 15 : 0));

                float weight = 1.0f / row.size();
                for (int b = 0; b < row.size(); b++) {
                    Keyboard.Button button = row.get(b);
                    ButtonHolder holder = new ButtonHolder(LayoutInflater.from(getContext()).inflate(R.layout.item_keyboard_button, layout, false));
                    holder.button.setTag(button);
                    holder.button.setText(button.getLabel());
                    holder.button.setTextColor(Color.parseColor("#ffffff"));
                    switch (button.getColor()) {
                        case "default":
                        case "secondary":
                            holder.button.setTextColor(Color.parseColor("#000000"));
                            holder.button.setBackgroundColor(Color.parseColor("#eeeeee"));
                            break;
                        case "negative":
                            holder.button.setBackgroundColor(Color.parseColor("#E64646"));
                            break;
                        case "positive":
                            holder.button.setBackgroundColor(Color.parseColor("#4BB34B"));
                            break;
                        default:
                            holder.button.setBackgroundColor(Color.parseColor("#5181B8"));
                            break;
                    }

                    layout.addView(holder.itemView, createLinear(0, ViewGroup.LayoutParams.MATCH_PARENT, weight, 0, 0, b != row.size() - 1 ? 10 : 0, 0));
                    holder.button.setOnClickListener(v -> delegate.didPressedButton((Keyboard.Button) v.getTag()));
                    buttonViews.add(holder.itemView);
                }
            }
        }
        return true;
    }

    public void setPanelHeight(int height) {
        panelHeight = height;
        if (isFullSize && botButtons != null && botButtons.size() != 0) {
            buttonHeight = !isFullSize ? 42 : (int) Math.max(42, (panelHeight - Utils.dp(30) - (botButtons.size() - 1) * Utils.dp(10)) / botButtons.size() / Utils.getDensity());
            int count = container.getChildCount();
            int newHeight = Utils.dp(buttonHeight);
            for (int a = 0; a < count; a++) {
                View v = container.getChildAt(a);
                LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) v.getLayoutParams();
                if (layoutParams.height != newHeight) {
                    layoutParams.height = newHeight;
                    v.setLayoutParams(layoutParams);
                }
            }
        }
    }

    public int getKeyboardHeight() {
        if (botButtons == null) {
            return 0;
        }
        return isFullSize ? panelHeight : botButtons.size() * Utils.dp(buttonHeight) + Utils.dp(30) + (botButtons.size() - 1) * Utils.dp(10);
    }

    private void listenKeyboardSize() {
        getViewTreeObserver().addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    public void destroy() {
        getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        container.removeAllViews();
        buttonViews.clear();
    }

    public interface BotKeyboardViewDelegate {
        void didPressedButton(Keyboard.Button button);
    }

    private static class ButtonHolder extends RecyclerView.ViewHolder {
        final MaterialButton button;

        ButtonHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.keyboard_button);
        }
    }
}
