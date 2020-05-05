package com.minelittlepony.hdskins.common.skins.valhalla;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.minelittlepony.hdskins.common.skins.Feature;
import com.minelittlepony.hdskins.common.skins.MoreHttpResponses;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.skins.SkinServer;
import com.minelittlepony.hdskins.common.skins.SkinRequest;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.authlib.yggdrasil.response.MinecraftTexturesPayload;
import com.mojang.util.UUIDTypeAdapter;
import org.apache.http.HttpHeaders;
import org.apache.http.NameValuePair;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

class ValhallaSkinServer implements SkinServer {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(UUID.class, new UUIDTypeAdapter())
            .create();

    private static final String API_PREFIX = "/api/v1";

    private static final Set<Feature> FEATURES = Sets.newHashSet(
            Feature.DOWNLOAD_USER_SKIN,
            Feature.UPLOAD_USER_SKIN,
            Feature.DELETE_USER_SKIN,
            Feature.MODEL_VARIANTS,
            Feature.MODEL_TYPES);

    private final String address;

    private transient String accessToken;

    public ValhallaSkinServer(String address) {
        this.address = address;
    }

    private String getApiPrefix() {
        return address + API_PREFIX;
    }

    @Override
    public Map<Type, MinecraftProfileTexture> loadProfileData(MinecraftSessionService sessionService, GameProfile profile) throws IOException {
        try (MoreHttpResponses response = MoreHttpResponses.execute(HTTP_CLIENT, new HttpGet(getTexturesURI(profile)))) {

            if (response.ok()) {
                MinecraftTexturesPayload payload = response.json(GSON, MinecraftTexturesPayload.class);
                return payload.getTextures();
            }
            throw new IOException(getErrorMessage(response));
        }
    }

    @Override
    public void performSkinUpload(MinecraftSessionService sessionService, SkinRequest upload) throws IOException, AuthenticationException {
        try {
            uploadPlayerSkin(sessionService, upload);
        } catch (IOException e) {
            if (e.getMessage().equals("Authorization failed")) {
                accessToken = null;
                uploadPlayerSkin(sessionService, upload);
            }

            throw e;
        }
    }

    private void uploadPlayerSkin(MinecraftSessionService sessionService, SkinRequest upload) throws IOException, AuthenticationException {
        authorize(sessionService, upload.getSession());

        if (upload instanceof SkinRequest.Delete) {
            resetSkin(upload);
        } else if (upload instanceof SkinRequest.Upload) {
            SkinRequest.Upload upload2 = (SkinRequest.Upload) upload;
            String scheme = upload2.getImage().getScheme();
            switch (scheme) {
                case "file":
                    uploadFile(upload2);
                    break;
                case "http":
                case "https":
                    uploadUrl(upload2);
                    break;
                default:
                    throw new IOException("Unsupported URI scheme: " + scheme);
            }
        } else {
            throw new UnsupportedOperationException("Unknown upload: " + upload);
        }
    }

    private void resetSkin(SkinRequest upload) throws IOException {
        upload(RequestBuilder.delete()
                .setUri(buildUserTextureUri(upload))
                .addHeader(HttpHeaders.AUTHORIZATION, this.accessToken)
                .build());
    }

    private void uploadFile(SkinRequest.Upload upload) throws IOException {
        final File file = new File(upload.getImage());

        MultipartEntityBuilder b = MultipartEntityBuilder.create()
                .addBinaryBody("file", file, ContentType.create("image/png"), file.getName());

        upload.getMetadata().forEach(b::addTextBody);

        upload(RequestBuilder.put()
                .setUri(buildUserTextureUri(upload))
                .addHeader(HttpHeaders.AUTHORIZATION, this.accessToken)
                .setEntity(b.build())
                .build());
    }

    private void uploadUrl(SkinRequest.Upload upload) throws IOException {
        upload(RequestBuilder.post()
                .setUri(buildUserTextureUri(upload))
                .addHeader(HttpHeaders.AUTHORIZATION, this.accessToken)
                .addParameter("file", upload.getImage().toString())
                .addParameters(mapAsParameters(upload.getMetadata()))
                .build());
    }

    static NameValuePair[] mapAsParameters(Map<String, String> parameters) {
        return parameters.entrySet().stream()
                .map(entry -> new BasicNameValuePair(entry.getKey(), entry.getValue()))
                .toArray(NameValuePair[]::new);
    }

    private void upload(HttpUriRequest request) throws IOException {
        try (MoreHttpResponses response = MoreHttpResponses.execute(HTTP_CLIENT, request)) {
            if (!response.ok()) {
                throw new IOException(response.json(GSON, JsonObject.class).get("message").getAsString());
            }
        }
    }

    private void authorize(MinecraftSessionService sessionService, Session session) throws IOException, AuthenticationException {
        if (this.accessToken != null) {
            return;
        }
        GameProfile profile = session.getProfile();
        AuthHandshake handshake = authHandshake(profile.getName());

        if (handshake.offline) {
            return;
        }

        // join the session server
        sessionService.joinServer(profile, session.getAccessToken(), handshake.serverId);

        AuthResponse response = authResponse(profile.getName(), handshake.verifyToken);
        if (!response.userId.equals(profile.getId())) {
            throw new IOException("UUID mismatch!"); // probably won't ever throw
        }
        this.accessToken = response.accessToken;
    }

    private AuthHandshake authHandshake(String name) throws IOException {
        try (MoreHttpResponses resp = MoreHttpResponses.execute(HTTP_CLIENT, RequestBuilder.post()
                .setUri(getHandshakeURI())
                .addParameter("name", name)
                .build())) {
            if (resp.ok()) {
                return resp.json(GSON, AuthHandshake.class);
            }
            throw new IOException(getErrorMessage(resp));
        }
    }

    private AuthResponse authResponse(String name, long verifyToken) throws IOException {
        try (MoreHttpResponses resp = MoreHttpResponses.execute(HTTP_CLIENT, RequestBuilder.post()
                .setUri(getResponseURI())
                .addParameter("name", name)
                .addParameter("verifyToken", String.valueOf(verifyToken))
                .build())) {
            if (resp.ok()) {
                return resp.json(GSON, AuthResponse.class);
            }
            throw new IOException(getErrorMessage(resp));
        }
    }

    private String getErrorMessage(MoreHttpResponses resp) throws IOException {
        return resp.json(GSON, JsonObject.class).get("message").getAsString();
    }

    private URI buildUserTextureUri(SkinRequest upload) {
        String user = UUIDTypeAdapter.fromUUID(upload.getSession().getProfile().getId());
        String skinType = upload.getType().toString().toLowerCase(Locale.US);
        return URI.create(String.format("%s/user/%s/%s", this.getApiPrefix(), user, skinType));
    }

    private URI getTexturesURI(GameProfile profile) {
        Preconditions.checkNotNull(profile.getId(), "profile id required for skins");
        return URI.create(String.format("%s/user/%s", this.getApiPrefix(), UUIDTypeAdapter.fromUUID(profile.getId())));
    }

    private URI getHandshakeURI() {
        return URI.create(String.format("%s/auth/handshake", this.getApiPrefix()));
    }

    private URI getResponseURI() {
        return URI.create(String.format("%s/auth/response", this.getApiPrefix()));
    }

    @Override
    public Set<Feature> getFeatures() {
        return FEATURES;
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName()
                + "\n\t" + "address=" + address;
    }

    private static class AuthHandshake {
        private boolean offline;
        private String serverId;
        private long verifyToken;
    }

    private static class AuthResponse {
        private String accessToken;
        private UUID userId;
    }
}