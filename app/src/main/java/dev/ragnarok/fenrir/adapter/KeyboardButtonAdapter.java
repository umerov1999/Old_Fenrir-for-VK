package dev.ragnarok.fenrir.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Keyboard;

public class KeyboardButtonAdapter extends RecyclerView.Adapter<KeyboardButtonAdapter.ButtonHolder> {
    private final Context mContext;
    private List<Keyboard.Button> mData;
    private ClickListener mClickListener;

    public KeyboardButtonAdapter(Context context, List<Keyboard.Button> data) {
        mData = data;
        mContext = context;
    }

    @NonNull
    @Override
    public ButtonHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ButtonHolder(LayoutInflater.from(mContext).inflate(R.layout.item_keyboard_button, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ButtonHolder holder, int position) {
        Keyboard.Button item = mData.get(position);
        holder.button.setText(item.getLabel());
        holder.button.setTextColor(Color.parseColor("#ffffff"));
        switch (item.getColor()) {
            case "secondary":
                holder.button.setTextColor(Color.parseColor("#000000"));
                holder.button.setBackgroundColor(Color.parseColor("#ffffff"));
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
        holder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onButtonClick(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setItems(List<Keyboard.Button> data) {
        mData = data;
        notifyDataSetChanged();
    }

    public void setClickListener(ClickListener clickListener) {
        mClickListener = clickListener;
    }

    public interface ClickListener {
        void onButtonClick(@NonNull Keyboard.Button item);
    }

    static class ButtonHolder extends RecyclerView.ViewHolder {
        final MaterialButton button;

        ButtonHolder(View itemView) {
            super(itemView);
            button = itemView.findViewById(R.id.keyboard_button);
        }
    }
}