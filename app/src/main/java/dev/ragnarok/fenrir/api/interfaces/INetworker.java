package dev.ragnarok.fenrir.api.interfaces;

public interface INetworker {

    IAccountApis vkDefault(int accountId);

    IAccountApis vkManual(int accountId, String accessToken);

    IAuthApi vkDirectAuth();

    IAuthApi vkAuth();

    IAudioCoverApi amazonAudioCover();

    ICliperApi cliperApi();

    ILongpollApi longpoll();

    IUploadApi uploads();
}