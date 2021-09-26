package com.skcraft.launcher.auth;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.skcraft.launcher.util.HttpRequest;
import lombok.Data;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;

public class CustomLoginService extends AbstractUserPasswordLoginService {
    private final URL authUrl;

    public CustomLoginService(URL authUrl)
    {
        this.authUrl = authUrl;
    }

    @Override
    public Session login(String id, String password) throws IOException, InterruptedException, AuthenticationException {
        AuthenticatePayload payload = new AuthenticatePayload(id, password);
        return call(authUrl, payload);
    }

    @Override
    public Session restore(SavedSession savedSession) throws IOException, InterruptedException, AuthenticationException {
        RefreshPayload payload = new RefreshPayload(savedSession.getAccessToken());
        return call(new URL(authUrl + "?refresh"), payload);
    }

    private Session call(URL url, Object payload)
            throws IOException, InterruptedException, AuthenticationException {
        HttpRequest req = HttpRequest
                .post(url)
                .bodyJson(payload)
                .execute();

        if (req.getResponseCode() != 200) {
            throw new AuthenticationException(req.returnContent().asString("UTF-8"), true);
        }

        return req.returnContent().asJson(Profile.class);
    }

    @Data
    private static class AuthenticatePayload {
        private final String username;
        private final String password;
    }

    @Data
    private static class RefreshPayload {
        private final String accessToken;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Profile implements Session
    {
        private String uuid;
        private String name;
        private byte[] avatarImage;
        private String accessToken;
        @JsonIgnore private final Map<String, String> userProperties = Collections.emptyMap();

        @Override
        @JsonIgnore
        public String getSessionToken() {
            return String.format("token:%s:%s", getAccessToken(), getUuid());
        }

        @Override
        @JsonIgnore
        public UserType getUserType() {
            return UserType.CUSTOM;
        }

        @Override
        public boolean isOnline() {
            return true;
        }
    }
}
