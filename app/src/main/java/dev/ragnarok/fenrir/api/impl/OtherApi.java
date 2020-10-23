package dev.ragnarok.fenrir.api.impl;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Map;

import dev.ragnarok.fenrir.api.IVkRetrofitProvider;
import dev.ragnarok.fenrir.api.interfaces.IOtherApi;
import dev.ragnarok.fenrir.settings.Settings;
import dev.ragnarok.fenrir.util.Objects;
import dev.ragnarok.fenrir.util.Optional;
import io.reactivex.rxjava3.core.Single;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class OtherApi implements IOtherApi {

    private final IVkRetrofitProvider provider;
    private final int accountId;

    public OtherApi(int accountId, IVkRetrofitProvider provider) {
        this.provider = provider;
        this.accountId = accountId;
    }

    @Override
    public Single<Optional<String>> rawRequest(String method, Map<String, String> postParams) {
        FormBody.Builder bodyBuilder = new FormBody.Builder();

        for (Map.Entry<String, String> entry : postParams.entrySet()) {
            bodyBuilder.add(entry.getKey(), entry.getValue());
        }

        return provider.provideNormalHttpClient(accountId)
                .flatMap(client -> Single
                        .<Response>create(emitter -> {
                            Request request = new Request.Builder()
                                    .url("https://" + Settings.get().other().get_Api_Domain() + "/method/" + method)
                                    .method("POST", bodyBuilder.build())
                                    .build();

                            Call call = client.newCall(request);

                            emitter.setCancellable(call::cancel);

                            call.enqueue(new Callback() {
                                @Override
                                public void onFailure(@NotNull Call call, @NotNull IOException e) {
                                    emitter.onError(e);
                                }

                                @Override
                                public void onResponse(@NotNull Call call, @NotNull Response response) {
                                    emitter.onSuccess(response);
                                }
                            });
                        }))
                .map(response -> {
                    ResponseBody body = response.body();
                    String responseBodyString = Objects.nonNull(body) ? body.string() : null;
                    return Optional.wrap(responseBodyString);
                });
    }
}