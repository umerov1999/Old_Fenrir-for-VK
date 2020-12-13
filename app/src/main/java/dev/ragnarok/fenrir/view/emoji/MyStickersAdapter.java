package dev.ragnarok.fenrir.view.emoji;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.Utils;

public class MyStickersAdapter extends RecyclerView.Adapter<MyStickersAdapter.Holder> {
    private final Context context;
    private EmojiconsPopup.OnMyStickerClickedListener myStickerClickedListener;

    public MyStickersAdapter(Context context) {
        this.context = context;
    }

    public void setMyStickerClickedListener(EmojiconsPopup.OnMyStickerClickedListener listener) {
        myStickerClickedListener = listener;
    }

    @NonNull
    @Override
    public MyStickersAdapter.Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.sticker_grid_item, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        String path = Utils.getCachedMyStickers().get(position);

        holder.image.setVisibility(View.VISIBLE);
        PicassoInstance.with()
                .load("file://" + path)
                //.networkPolicy(NetworkPolicy.OFFLINE)
                .tag(Constants.PICASSO_TAG)
                .into(holder.image);
        holder.root.setOnClickListener(v -> myStickerClickedListener.onMyStickerClick(path));
    }

    @Override
    public int getItemCount() {
        return Utils.getCachedMyStickers().size();
    }

    static final class Holder extends RecyclerView.ViewHolder {
        final View root;
        final ImageView image;

        Holder(@NonNull View itemView) {
            super(itemView);
            root = itemView.getRootView();
            image = itemView.findViewById(R.id.sticker);
        }
    }
}
