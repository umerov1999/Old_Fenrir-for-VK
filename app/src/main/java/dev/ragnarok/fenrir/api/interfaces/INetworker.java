package dev.ragnarok.fenrir.api.interfaces;

public interface INetworker {

    IAccountApis vkDefault(int accountId);

    IAccountApis vkManual(int accountId, String accessToken);

    IAuthApi vkDirectAuth();

    IAuthApi vkAuth();

    IAudioCoverApi amazonAudioCover();

    ILocalServerApi localServerApi();

    ILongpollApi longpoll();

    IUploadApi uploads();
}
