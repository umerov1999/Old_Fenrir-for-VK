package dev.ragnarok.fenrir.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.ThemeValue;
import dev.ragnarok.fenrir.settings.Settings;

public class ThemeAdapter extends RecyclerView.Adapter<ThemeAdapter.ViewHolder> {

    private List<ThemeValue> data;
    private ClickListener clickListener;

    public ThemeAdapter(List<ThemeValue> data) {
        this.data = data;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ThemeValue category = data.get(position);
        boolean isDark = Settings.get().ui().isDarkModeEnabled(holder.itemView.getContext());

        holder.title.setText(category.name);
        holder.primary.setBackgroundColor(isDark ? category.color_night_primary : category.color_day_primary);
        holder.secondary.setBackgroundColor(isDark ? category.color_night_secondary : category.color_day_secondary);
        holder.selected.setVisibility(Settings.get().ui().getMainThemeKey().equals(category.id) ? View.VISIBLE : View.GONE);
        holder.clicked.setOnClickListener(v -> clickListener.onClick(position, category));
        holder.gradient.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{isDark ? category.color_night_primary : category.color_day_primary, isDark ? category.color_night_secondary : category.color_day_secondary}));
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<ThemeValue> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface ClickListener {
        void onClick(int index, ThemeValue value);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        final ImageView primary;
        final ImageView secondary;
        final ImageView selected;
        final ImageView gradient;
        final ViewGroup clicked;
        final TextView title;

        public ViewHolder(View itemView) {
            super(itemView);
            primary = itemView.findViewById(R.id.theme_icon_primary);
            secondary = itemView.findViewById(R.id.theme_icon_secondary);
            selected = itemView.findViewById(R.id.selected);
            clicked = itemView.findViewById(R.id.theme_type);
            title = itemView.findViewById(R.id.item_title);
            gradient = itemView.findViewById(R.id.theme_icon_gradient);
        }
    }
}
