package dev.ragnarok.fenrir.fragment.conversation;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

import dev.ragnarok.fenrir.Extra;
import dev.ragnarok.fenrir.adapter.AudioRecyclerAdapter;
import dev.ragnarok.fenrir.model.Audio;
import dev.ragnarok.fenrir.mvp.core.IPresenterFactory;
import dev.ragnarok.fenrir.mvp.presenter.conversations.ChatAttachmentAudioPresenter;
import dev.ragnarok.fenrir.mvp.view.conversations.IChatAttachmentAudiosView;

public class ConversationAudiosFragment extends AbsChatAttachmentsFragment<Audio, ChatAttachmentAudioPresenter, IChatAttachmentAudiosView>
        implements AudioRecyclerAdapter.ClickListener, IChatAttachmentAudiosView {


    @Override
    protected RecyclerView.LayoutManager createLayoutManager() {
        return new LinearLayoutManager(requireActivity(), LinearLayoutManager.VERTICAL, false);
    }

    @Override
    public RecyclerView.Adapter<?> createAdapter() {
        AudioRecyclerAdapter audioRecyclerAdapter = new AudioRecyclerAdapter(requireActivity(), Collections.emptyList(), false, false, 0, null);
        audioRecyclerAdapter.setClickListener(this);
        return audioRecyclerAdapter;
    }

    @Override
    public void onClick(int position, int catalog, Audio audio) {
        getPresenter().fireAudioPlayClick(position, audio);
    }

    @Override
    public void onEdit(int position, Audio audio) {

    }

    @Override
    public void onDelete(int position) {

    }

    @Override
    public void displayAttachments(List<Audio> data) {
        ((AudioRecyclerAdapter) getAdapter()).setData(data);
    }

    @NotNull
    @Override
    public IPresenterFactory<ChatAttachmentAudioPresenter> getPresenterFactory(@Nullable Bundle saveInstanceState) {
        return () -> new ChatAttachmentAudioPresenter(
                getArguments().getInt(Extra.PEER_ID),
                getArguments().getInt(Extra.ACCOUNT_ID),
                saveInstanceState
        );
    }
}
