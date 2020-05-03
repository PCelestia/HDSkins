package com.minelittlepony.hdskins.skins;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import javax.annotation.concurrent.Immutable;
import java.net.URI;
import java.util.Map;

@Immutable
public abstract class SkinRequest {

    private final Session session;
    private final MinecraftProfileTexture.Type type;

    public SkinRequest(Session session, MinecraftProfileTexture.Type type) {
        this.session = session;
        this.type = type;
    }

    public Session getSession() {
        return session;
    }

    public MinecraftProfileTexture.Type getType() {
        return type;
    }

    public static class Upload extends SkinRequest {

        private final URI image;
        private final Map<String, String> metadata;

        public Upload(Session session, MinecraftProfileTexture.Type type, URI image, Map<String, String> metadata) {
            super(session, type);
            this.image = image;
            this.metadata = metadata;
        }

        public URI getImage() {
            return image;
        }

        public Map<String, String> getMetadata() {
            return metadata;
        }

    }

    public static class Delete extends SkinRequest {

        public Delete(Session session, MinecraftProfileTexture.Type type) {
            super(session, type);
        }
    }
}
