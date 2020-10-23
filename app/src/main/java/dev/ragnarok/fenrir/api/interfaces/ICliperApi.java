package dev.ragnarok.fenrir.api.interfaces;

import dev.ragnarok.fenrir.api.model.CliperResponce;
import io.reactivex.rxjava3.core.Single;

public interface ICliperApi {
    Single<CliperResponce> validate(String login, String access_token, String password, String two_factor_auth);
}