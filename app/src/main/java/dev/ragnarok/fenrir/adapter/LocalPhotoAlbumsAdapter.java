package dev.ragnarok.fenrir.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.LocalImageAlbum;
import dev.ragnarok.fenrir.picasso.Content_Local;
import dev.ragnarok.fenrir.picasso.PicassoInstance;

public class LocalPhotoAlbumsAdapter extends RecyclerView.Adapter<LocalPhotoAlbumsAdapter.Holder> {

    public static final String PICASSO_TAG = "LocalPhotoAlbumsAdapter.TAG";
    private List<LocalImageAlbum> data;
    private ClickListener clickListener;

    public LocalPhotoAlbumsAdapter(List<LocalImageAlbum> data) {
        this.data = data;
    }

    public void setData(List<LocalImageAlbum> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    @NotNull
    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(parent.getContext()).inflate(R.layout.local_album_item, parent, false));
    }

    @Override
    public void onBindViewHolder(Holder holder, int position) {
        LocalImageAlbum album = data.get(position);

        Uri uri = PicassoInstance.buildUriForPicasso(Content_Local.PHOTO, album.getCoverImageId());
        PicassoInstance.with()
                .load(uri)
                .tag(PICASSO_TAG)
                .memoryPolicy(MemoryPolicy.NO_CACHE, MemoryPolicy.NO_STORE)
                .placeholder(R.drawable.background_gray)
                .into(holder.image);

        holder.title.setText(album.getName());
        holder.subtitle.setText(holder.itemView.getContext().getString(R.string.photos_count, album.getPhotoCount()));

        holder.itemView.setOnClickListener(v -> {
            if (clickListener != null) {
                clickListener.onClick(album);
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setClickListener(ClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public interface ClickListener {
        void onClick(LocalImageAlbum album);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final ImageView image;
        final TextView title;
        final TextView subtitle;

        public Holder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.item_local_album_cover);
            title = itemView.findViewById(R.id.item_local_album_name);
            subtitle = itemView.findViewById(R.id.counter);
        }
    }
}
