package dev.ragnarok.fenrir.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso3.Transformation;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.AppChatUser;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.model.UserPlatform;
import dev.ragnarok.fenrir.picasso.PicassoInstance;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Utils;
import dev.ragnarok.fenrir.util.ViewUtils;
import dev.ragnarok.fenrir.view.OnlineView;

import static dev.ragnarok.fenrir.util.Utils.isEmpty;
import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

public class ChatMembersListDomainAdapter extends RecyclerView.Adapter<ChatMembersListDomainAdapter.ViewHolder> {

    private final Transformation transformation;
    private final int paddingForFirstLast;
    private List<AppChatUser> data;
    private ActionListener actionListener;

    public ChatMembersListDomainAdapter(Context context, List<AppChatUser> users) {
        data = users;
        transformation = CurrentTheme.createTransformationForAvatar(context);
        paddingForFirstLast = Utils.is600dp(context) ? (int) Utils.dpToPx(16, context) : 0;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new ViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_chat_user_list_second, viewGroup, false));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Context context = holder.itemView.getContext();

        AppChatUser item = data.get(position);
        Owner user = item.getMember();

        boolean online = false;
        boolean onlineMobile = false;

        @UserPlatform
        int platform = UserPlatform.UNKNOWN;
        int app = 0;

        if (user instanceof User) {
            User interlocuter = (User) user;
            online = interlocuter.isOnline();
            onlineMobile = interlocuter.isOnlineMobile();
            platform = interlocuter.getPlatform();
            app = interlocuter.getOnlineApp();
        }

        Integer iconRes = ViewUtils.getOnlineIcon(online, onlineMobile, platform, app);
        holder.vOnline.setIcon(iconRes != null ? iconRes : 0);
        holder.vOnline.setVisibility(online ? View.VISIBLE : View.GONE);

        String userAvatarUrl = user.getMaxSquareAvatar();

        if (isEmpty(userAvatarUrl)) {
            PicassoInstance.with()
                    .load(R.drawable.ic_avatar_unknown)
                    .transform(transformation)
                    .into(holder.ivAvatar);
        } else {
            PicassoInstance.with()
                    .load(userAvatarUrl)
                    .transform(transformation)
                    .into(holder.ivAvatar);
        }

        holder.tvName.setText(user.getFullName());
        boolean isCreator = user.getOwnerId() == item.getInvitedBy();

        if (isCreator) {
            holder.tvSubline.setText(R.string.creator_of_conversation);
        } else {
            holder.tvSubline.setText(context.getString(R.string.invited_by, item.getInviter().getFullName()));
        }

        if (nonEmpty(user.getDomain())) {
            holder.tvDomain.setText("@" + user.getDomain());
        } else {
            holder.tvDomain.setText("@id" + user.getOwnerId());
        }

        holder.itemView.setOnClickListener(view -> {
            if (Objects.nonNull(actionListener)) {
                actionListener.onUserClick(item);
            }
        });

        View view = holder.itemView;

        view.setPadding(view.getPaddingLeft(),
                position == 0 ? paddingForFirstLast : 0,
                view.getPaddingRight(),
                position == getItemCount() - 1 ? paddingForFirstLast : 0);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<AppChatUser> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void setActionListener(ActionListener actionListener) {
        this.actionListener = actionListener;
    }

    public interface ActionListener {
        void onUserClick(AppChatUser user);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        OnlineView vOnline;
        ImageView ivAvatar;
        TextView tvName;
        TextView tvDomain;
        TextView tvSubline;

        ViewHolder(View root) {
            super(root);
            vOnline = root.findViewById(R.id.item_user_online);
            ivAvatar = root.findViewById(R.id.item_user_avatar);
            tvName = root.findViewById(R.id.item_user_name);
            tvSubline = root.findViewById(R.id.item_user_invited_by);
            tvDomain = root.findViewById(R.id.item_user_domain);
        }
    }
}
