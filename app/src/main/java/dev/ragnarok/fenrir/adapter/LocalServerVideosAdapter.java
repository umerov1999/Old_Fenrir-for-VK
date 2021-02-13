package dev.ragnarok.fenrir.adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Locale;

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.domain.ILocalServerInteractor;
import dev.ragnarok.fenrir.domain.InteractorFactory;
import dev.ragnarok.fenrir.link.VkLinkParser;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.ModalBottomSheetDialogFragment;
import dev.ragnarok.fenrir.modalbottomsheetdialogfragment.OptionRequest;
import dev.ragnarok.fenrir.model.Video;
import dev.ragnarok.fenrir.model.menu.AudioItem;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.util.AppPerms;
import dev.ragnarok.fenrir.util.CustomToast;
import dev.ragnarok.fenrir.util.DownloadWorkUtils;
import dev.ragnarok.fenrir.util.RxUtils;
import dev.ragnarok.fenrir.util.Utils;
import io.reactivex.rxjava3.disposables.Disposable;

import static dev.ragnarok.fenrir.util.Utils.firstNonEmptyString;

public class LocalServerVideosAdapter extends RecyclerView.Adapter<LocalServerVideosAdapter.Holder> {

    private final Context context;
    private final ILocalServerInteractor mVideoInteractor;
    private List<Video> data;
    private VideoOnClickListener videoOnClickListener;
    private Disposable listDisposable = Disposable.disposed();

    public LocalServerVideosAdapter(@NonNull Context context, @NonNull List<Video> data) {
        this.context = context;
        this.data = data;
        mVideoInteractor = InteractorFactory.createLocalServerInteractor();
    }

    private static String BytesToSize(long Bytes) {
        long tb = 1099511627776L;
        long gb = 1073741824;
        long mb = 1048576;
        long kb = 1024;

        String returnSize;
        if (Bytes >= tb)
            returnSize = String.format(Locale.getDefault(), "%.2f TB", (double) Bytes / tb);
        else if (Bytes >= gb)
            returnSize = String.format(Locale.getDefault(), "%.2f GB", (double) Bytes / gb);
        else if (Bytes >= mb)
            returnSize = String.format(Locale.getDefault(), "%.2f MB", (double) Bytes / mb);
        else if (Bytes >= kb)
            returnSize = String.format(Locale.getDefault(), "%.2f KB", (double) Bytes / kb);
        else returnSize = String.format(Locale.getDefault(), "%d Bytes", Bytes);
        return returnSize;
    }

    @NonNull
    @Override
    public Holder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_local_server_video, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull Holder holder, int position) {
        Video video = data.get(position);

        holder.title.setText(video.getTitle());
        holder.description.setText(video.getDescription());
        holder.videoLenght.setText(BytesToSize(video.getDuration()));

        String photoUrl = video.getImage();

        if (Utils.nonEmpty(photoUrl)) {
            PicassoInstance.with()
                    .load(photoUrl)
                    .tag(Constants.PICASSO_TAG)
                    .into(holder.image);
        } else {
            PicassoInstance.with().cancelRequest(holder.image);
        }

        holder.card.setOnClickListener(v -> {
            if (videoOnClickListener != null) {
                videoOnClickListener.onVideoClick(position, video);
            }
        });
        holder.card.setOnLongClickListener(v -> {
            ModalBottomSheetDialogFragment.Builder menus = new ModalBottomSheetDialogFragment.Builder();
            menus.add(new OptionRequest(AudioItem.save_item_audio, context.getString(R.string.download), R.drawable.save));
            menus.add(new OptionRequest(AudioItem.play_item_audio, context.getString(R.string.play), R.drawable.play));
            menus.add(new OptionRequest(AudioItem.edit_track, context.getString(R.string.update_time), R.drawable.ic_recent));
            menus.header(firstNonEmptyString(video.getDescription(), " ") + " - " + video.getTitle(), R.drawable.video, null);
            menus.columns(2);
            menus.show(((FragmentActivity) context).getSupportFragmentManager(), "server_video_options", option -> {
                switch (option.getId()) {
                    case AudioItem.save_item_audio:
                        if (!AppPerms.hasReadWriteStoragePermission(context)) {
                            if (videoOnClickListener != null) {
                                videoOnClickListener.onRequestWritePermissions();
                            }
                            break;
                        }
                        DownloadWorkUtils.doDownloadVideo(context, video, video.getMp4link720(), "Local");
                        break;
                    case AudioItem.play_item_audio:
                        if (videoOnClickListener != null) {
                            videoOnClickListener.onVideoClick(position, video);
                        }
                        break;
                    case AudioItem.edit_track:
                        String hash = VkLinkParser.parseLocalServerURL(video.getMp4link720());
                        if (Utils.isEmpty(hash)) {
                            break;
                        }
                        listDisposable = mVideoInteractor.update_time(hash).compose(RxUtils.applySingleIOToMainSchedulers()).subscribe(t -> CustomToast.CreateCustomToast(context).showToast(R.string.success), t -> Utils.showErrorInAdapter((Activity) context, t));
                        break;
                    default:
                        break;
                }
            });
            return true;
        });
    }

    @Override
    public void onDetachedFromRecyclerView(@NotNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        listDisposable.dispose();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setVideoOnClickListener(VideoOnClickListener videoOnClickListener) {
        this.videoOnClickListener = videoOnClickListener;
    }

    public void setData(List<Video> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public interface VideoOnClickListener {
        void onVideoClick(int position, Video video);

        void onRequestWritePermissions();
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final View card;
        final ImageView image;
        final TextView videoLenght;
        final TextView title;
        final TextView description;

        public Holder(View itemView) {
            super(itemView);
            card = itemView.findViewById(R.id.card_view);
            image = itemView.findViewById(R.id.video_image);
            videoLenght = itemView.findViewById(R.id.video_lenght);
            title = itemView.findViewById(R.id.title);
            description = itemView.findViewById(R.id.description);
        }
    }
}
