package com.minelittlepony.hdskins.skins;

import com.google.gson.annotations.JsonAdapter;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.exceptions.AuthenticationException;
import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;
import com.mojang.authlib.minecraft.MinecraftSessionService;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

@JsonAdapter(SkinServerSerializer.class)
public interface SkinServer {

    CloseableHttpClient HTTP_CLIENT = HttpClients.custom()
            .useSystemProperties()
            .setSSLSocketFactory(new SSLConnectionSocketFactory(
                    HttpsURLConnection.getDefaultSSLSocketFactory(),
                    SSLConnectionSocketFactory.BROWSER_COMPATIBLE_HOSTNAME_VERIFIER)
            ).build();

    /**
     * Returns true for any features that this skin server supports.
     */
    Set<Feature> getFeatures();

    /**
     * Synchronously loads texture information for the provided profile.
     *
     * @param sessionService The session service
     * @param profile        The profile
     * @return The parsed server response as a textures payload.
     * @throws IOException If any authentication or network error occurs.
     */
    Map<Type, MinecraftProfileTexture> loadProfileData(MinecraftSessionService sessionService, GameProfile profile) throws IOException;

    /**
     * Synchronously uploads a skin to this server.
     *
     * @param sessionService The session service
     * @param upload         The payload to send.
     * @throws IOException             If any authentication or network error occurs.
     * @throws AuthenticationException If the user is not authenticated.
     */
    void performSkinUpload(MinecraftSessionService sessionService, SkinRequest upload) throws IOException, AuthenticationException;
}
