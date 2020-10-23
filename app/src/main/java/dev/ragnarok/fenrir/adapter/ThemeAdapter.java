package dev.ragnarok.fenrir.adapter;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

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

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_theme, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ThemeValue category = data.get(position);

        holder.title.setText(category.name);
        holder.primary.setBackgroundColor(category.color_primary);
        holder.secondary.setBackgroundColor(category.color_secondary);
        holder.selected.setVisibility(Settings.get().ui().getMainThemeKey().equals(category.id) ? View.VISIBLE : View.GONE);
        holder.clicked.setOnClickListener(v -> clickListener.onClick(position, category));
        holder.gradient.setBackground(new GradientDrawable(GradientDrawable.Orientation.TL_BR,
                new int[]{category.color_primary, category.color_secondary}));
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

        ImageView primary;
        ImageView secondary;
        ImageView selected;
        ImageView gradient;
        ViewGroup clicked;
        TextView title;

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
