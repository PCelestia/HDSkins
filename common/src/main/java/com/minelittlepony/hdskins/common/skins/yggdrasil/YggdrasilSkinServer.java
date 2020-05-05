package com.minelittlepony.hdskins.common.skins.yggdrasil;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.minelittlepony.hdskins.common.skins.Feature;
import com.minelittlepony.hdskins.common.skins.MoreHttpResponses;
import com.minelittlepony.hdskins.common.skins.Session;
import com.minelittlepony.hdskins.common.skins.SkinServer;
import com.minelittlepony.hdskins.common.skins.SkinRequest;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.InsecureTextureException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import com.mojang.util.UUIDTypeAdapter;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

class YggdrasilSkinServer implements SkinServer {

    private static final Set<Feature> FEATURES = Sets.newHashSet(
            Feature.SYNTHETIC,
            Feature.UPLOAD_USER_SKIN,
            Feature.DOWNLOAD_USER_SKIN,
            Feature.DELETE_USER_SKIN,
            Feature.MODEL_VARIANTS,
            Feature.MODEL_TYPES);

    private static final String ADDRESS = "https://api.mojang.com";

    @Override
    public Set<Feature> getFeatures() {
        return FEATURES;
    }

    @Override
    public Map<Type, MinecraftProfileTexture> loadProfileData(MinecraftSessionService session, GameProfile profile) throws IOException {
        profile.getProperties().removeAll("textures");
        GameProfile newProfile = session.fillProfileProperties(profile, true);

        if (newProfile == profile) {
            throw new IOException("Mojang API error occured. You may be throttled.");
        }
        profile = newProfile;

        try {
            return session.getTextures(profile, true);
        } catch (InsecureTextureException e) {
            throw new IOException(e);
        }
    }

    @Override
    public void performSkinUpload(MinecraftSessionService sessionService, SkinRequest upload) throws IOException, AuthenticationException {
        authorize(sessionService, upload.getSession());

        if (upload instanceof SkinRequest.Upload) {
            send(prepareUpload((SkinRequest.Upload) upload, RequestBuilder.put()));
        } else if (upload instanceof SkinRequest.Delete) {
            send(appendHeaders(upload, RequestBuilder.delete()));
        } else {
            throw new UnsupportedOperationException("Unknown upload: " + upload);
        }
    }

    private RequestBuilder prepareUpload(SkinRequest.Upload upload, RequestBuilder request) throws IOException {
        request = appendHeaders(upload, request);
        String scheme = upload.getImage().getScheme();
        switch (scheme) {
            case "file":
                final File file = new File(upload.getImage());

                MultipartEntityBuilder b = MultipartEntityBuilder.create()
                        .addBinaryBody("file", file, ContentType.create("image/png"), file.getName());

                mapMetadata(upload.getMetadata()).forEach(b::addTextBody);

                return request.setEntity(b.build());
            case "http":
            case "https":
                return request
                        .addParameter("file", upload.getImage().toString())
                        .addParameters(MoreHttpResponses.mapAsParameters(mapMetadata(upload.getMetadata())));
            default:
                throw new IOException("Unsupported URI scheme: " + scheme);
        }
    }

    private RequestBuilder appendHeaders(SkinRequest upload, RequestBuilder request) {
        return request
                .setUri(URI.create(String.format("%s/user/profile/%s/%s", ADDRESS,
                        UUIDTypeAdapter.fromUUID(upload.getSession().getProfile().getId()),
                        upload.getType().toString().toLowerCase(Locale.US))))
                .addHeader("authorization", "Bearer " + upload.getSession().getAccessToken());
    }

    private Map<String, String> mapMetadata(Map<String, String> metadata) {
        return metadata.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                entry -> {
                    String value = entry.getValue();
                    if ("model".contentEquals(entry.getKey()) && "default".contentEquals(value)) {
                        return "classic";
                    }
                    return value;
                })
        );
    }

    private void authorize(MinecraftSessionService sessionService, Session session) throws AuthenticationException {
        sessionService.joinServer(session.getProfile(), session.getAccessToken(), "");
    }

    private void send(RequestBuilder request) throws IOException {
        try (MoreHttpResponses response = MoreHttpResponses.execute(HTTP_CLIENT, request.build())) {
            if (!response.ok()) {
                throw new IOException(response.json(new Gson(), ErrorResponse.class).toString());
            }
        }
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() +
                "\n\t" + "address=" + ADDRESS +
                "\n\t" + "secured=" + true;
    }

    static class ErrorResponse {
        String error;
        String errorMessage;

        @Override
        public String toString() {
            return String.format("%s: %s", error, errorMessage);
        }
    }
}