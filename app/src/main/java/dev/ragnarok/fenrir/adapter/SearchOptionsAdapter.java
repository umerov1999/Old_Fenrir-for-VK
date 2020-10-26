package dev.ragnarok.fenrir.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import dev.ragnarok.fenrir.R;
import dev.ragnarok.fenrir.adapter.base.RecyclerBindableAdapter;
import dev.ragnarok.fenrir.fragment.search.options.BaseOption;
import dev.ragnarok.fenrir.fragment.search.options.DatabaseOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleBooleanOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleNumberOption;
import dev.ragnarok.fenrir.fragment.search.options.SimpleTextOption;
import dev.ragnarok.fenrir.fragment.search.options.SpinnerOption;

public class SearchOptionsAdapter extends RecyclerBindableAdapter<BaseOption, RecyclerView.ViewHolder> {

    private static final int TYPE_NORMAL = 0;
    private static final int TYPE_BOOLEAN = 1;
    private OptionClickListener mOptionClickListener;

    public SearchOptionsAdapter(List<BaseOption> items) {
        super(items);
    }

    @Override
    protected void onBindItemViewHolder(RecyclerView.ViewHolder viewHolder, int position, int type) {
        BaseOption option = getItem(position);

        switch (type) {
            case TYPE_NORMAL:
                NormalHolder normalHolder = (NormalHolder) viewHolder;

                if (option instanceof SimpleNumberOption) {
                    bindSimpleNumberHolder((SimpleNumberOption) option, normalHolder);
                }

                if (option instanceof SpinnerOption) {
                    bindSpinnerHolder((SpinnerOption) option, normalHolder);

                }

                if (option instanceof SimpleTextOption) {
                    bindSimpleTextHolder((SimpleTextOption) option, normalHolder);
                }

                if (option instanceof DatabaseOption) {
                    bindDatabaseHolder((DatabaseOption) option, normalHolder);
                }

                break;
            case TYPE_BOOLEAN:
                SimpleBooleanHolder simpleBooleanHolder = (SimpleBooleanHolder) viewHolder;
                bindSimpleBooleanHolder((SimpleBooleanOption) option, simpleBooleanHolder);
                break;
        }
    }

    private void bindDatabaseHolder(DatabaseOption option, NormalHolder holder) {
        holder.title.setText(option.title);
        holder.value.setText(option.value == null ? null : option.value.title);
        holder.delete.setVisibility(option.value == null ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onDatabaseOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if (mOptionClickListener != null) {
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    private void bindSimpleBooleanHolder(SimpleBooleanOption option, SimpleBooleanHolder holder) {
        holder.checkableView.setText(option.title);

        holder.checkableView.setOnCheckedChangeListener(null);
        holder.checkableView.setChecked(option.checked);

        holder.checkableView.setOnCheckedChangeListener((buttonView, isChecked) -> {
            option.checked = isChecked;
            if (mOptionClickListener != null) {
                mOptionClickListener.onSimpleBooleanOptionChanged(option);
            }
        });
    }

    private void bindSpinnerHolder(SpinnerOption option, NormalHolder holder) {
        if (option.value == null) {
            holder.value.setText(null);
        } else {
            holder.value.setText(option.value.name);
        }

        holder.delete.setVisibility(option.value == null ? View.INVISIBLE : View.VISIBLE);

        holder.title.setText(option.title);
        holder.itemView.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onSpinnerOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if (mOptionClickListener != null) {
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    private void bindSimpleNumberHolder(SimpleNumberOption option, NormalHolder holder) {
        holder.value.setText(option.value == null ? null : String.valueOf(option.value));
        holder.title.setText(option.title);
        holder.delete.setVisibility(option.value == null ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onSimpleNumberOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if (mOptionClickListener != null) {
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    private void bindSimpleTextHolder(SimpleTextOption option, NormalHolder holder) {
        holder.value.setText(option.value);
        holder.title.setText(option.title);

        holder.delete.setVisibility(TextUtils.isEmpty(option.value) ? View.INVISIBLE : View.VISIBLE);

        holder.itemView.setOnClickListener(v -> {
            if (mOptionClickListener != null) {
                mOptionClickListener.onSimpleTextOptionClick(option);
            }
        });

        holder.delete.setOnClickListener(v -> {
            holder.value.setText(null);
            holder.delete.setVisibility(View.INVISIBLE);
            option.value = null;

            if (mOptionClickListener != null) {
                mOptionClickListener.onOptionCleared(option);
            }
        });
    }

    @Override
    protected RecyclerView.ViewHolder viewHolder(View view, int type) {
        switch (type) {
            case TYPE_NORMAL:
                return new NormalHolder(view);
            case TYPE_BOOLEAN:
                return new SimpleBooleanHolder(view);
        }

        return null;
    }

    @Override
    protected int layoutId(int type) {
        switch (type) {
            case TYPE_NORMAL:
                return R.layout.item_search_option_text;
            case TYPE_BOOLEAN:
                return R.layout.item_search_option_checkbox;
        }

        return 0;
    }

    @Override
    protected int getItemType(int position) {
        BaseOption option = getItem(position - getHeadersCount());

        if (option instanceof SimpleNumberOption
                || option instanceof SimpleTextOption
                || option instanceof SpinnerOption
                || option instanceof DatabaseOption) {
            return TYPE_NORMAL;
        }

        if (option instanceof SimpleBooleanOption) {
            return TYPE_BOOLEAN;
        }

        return -1;
    }

    public void setOptionClickListener(OptionClickListener optionClickListener) {
        mOptionClickListener = optionClickListener;
    }

    public interface OptionClickListener {
        void onSpinnerOptionClick(SpinnerOption spinnerOption);

        void onDatabaseOptionClick(DatabaseOption databaseOption);

        void onSimpleNumberOptionClick(SimpleNumberOption option);

        void onSimpleTextOptionClick(SimpleTextOption option);

        void onSimpleBooleanOptionChanged(SimpleBooleanOption option);

        void onOptionCleared(BaseOption option);
    }

    public static class NormalHolder extends RecyclerView.ViewHolder {

        final TextView title;
        final TextView value;
        final ImageView delete;

        NormalHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.title);
            value = itemView.findViewById(R.id.value);
            delete = itemView.findViewById(R.id.delete);
        }
    }

    public static class SimpleBooleanHolder extends RecyclerView.ViewHolder {

        final SwitchCompat checkableView;

        SimpleBooleanHolder(View itemView) {
            super(itemView);
            checkableView = itemView.findViewById(R.id.switchcompat);
        }
    }
}