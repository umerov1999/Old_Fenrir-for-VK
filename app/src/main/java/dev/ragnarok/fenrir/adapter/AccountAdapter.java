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

import dev.ragnarok.fenrir.Constants;
import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.model.Account;
import dev.ragnarok.fenrir.model.Owner;
import dev.ragnarok.fenrir.model.User;
import dev.ragnarok.fenrir.settings.CurrentTheme;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.ViewUtils;

import static dev.ragnarok.fenrir.util.Utils.nonEmpty;

public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.Holder> {

    private final Context context;
    private final List<Account> data;
    private final Transformation transformation;
    private final Callback callback;

    public AccountAdapter(Context context, List<Account> items, Callback callback) {
        this.context = context;
        data = items;
        transformation = CurrentTheme.createTransformationForAvatar(context);
        this.callback = callback;
    }

    @NotNull
    @Override
    public Holder onCreateViewHolder(@NotNull ViewGroup parent, int viewType) {
        return new Holder(LayoutInflater.from(context).inflate(R.layout.item_account, parent, false));
    }

    @NotNull
    public Account getByPosition(int position) {
        return data.get(position);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NotNull Holder holder, int position) {
        Account account = data.get(position);

        Owner owner = account.getOwner();

        if (Objects.isNull(owner)) {
            holder.firstName.setText(String.valueOf(account.getId()));
            ViewUtils.displayAvatar(holder.avatar, transformation, null, Constants.PICASSO_TAG);
        } else {
            holder.firstName.setText(owner.getFullName());
            ViewUtils.displayAvatar(holder.avatar, transformation, owner.getMaxSquareAvatar(), Constants.PICASSO_TAG);
        }

        if (account.getId() < 0) {
            holder.lastName.setText("club" + Math.abs(account.getId()));
        } else {
            User user = (User) owner;

            if (Objects.nonNull(user) && nonEmpty(user.getDomain())) {
                holder.lastName.setText("@" + user.getDomain());
            } else {
                holder.lastName.setText("@id" + account.getId());
            }
        }

        boolean isCurrent = account.getId() == Settings.get()
                .accounts()
                .getCurrent();

        holder.active.setVisibility(isCurrent ? View.VISIBLE : View.INVISIBLE);
        holder.account.setOnClickListener(v -> callback.onClick(account));
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface Callback {
        void onClick(Account account);
    }

    public static class Holder extends RecyclerView.ViewHolder {

        final TextView firstName;
        final TextView lastName;
        final ImageView avatar;
        final ImageView active;
        final View account;

        public Holder(View itemView) {
            super(itemView);
            firstName = itemView.findViewById(R.id.first_name);
            lastName = itemView.findViewById(R.id.last_name);
            avatar = itemView.findViewById(R.id.avatar);
            active = itemView.findViewById(R.id.active);
            account = itemView.findViewById(R.id.account_select);
        }
    }
}