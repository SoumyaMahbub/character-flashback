package com.moulberry.flashback.actor;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.yggdrasil.ProfileResult;
import com.moulberry.flashback.FilePlayerSkin;
import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.combo_options.ComboOption;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.client.resources.PlayerSkin;

import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class ActorSkin {

    public enum SkinType implements ComboOption {
        DEFAULT_STEVE("Default (Steve)"),
        DEFAULT_ALEX("Default (Alex)"),
        FILE("Local PNG File"),
        USERNAME("Minecraft Username / UUID");

        private final String displayName;

        SkinType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String text() {
            return displayName;
        }
    }

    public enum ModelType implements ComboOption {
        AUTO("Auto Detect"),
        WIDE("Classic (4px Arms)"),
        SLIM("Slim (3px Arms)");

        private final String displayName;

        ModelType(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        @Override
        public String text() {
            return displayName;
        }
    }

    private SkinType skinType = SkinType.DEFAULT_STEVE;
    private ModelType modelType = ModelType.AUTO;
    private String skinValue = "";

    private transient FilePlayerSkin filePlayerSkin = null;
    private transient PlayerSkin cachedPlayerSkin = null;
    private transient boolean skinFetchPending = false;
    private transient long lastFetchAttempt = 0;

    public ActorSkin() {}

    public ActorSkin(SkinType skinType, String skinValue, ModelType modelType) {
        this.skinType = skinType;
        this.skinValue = Objects.requireNonNullElse(skinValue, "");
        this.modelType = modelType;
    }

    public SkinType getSkinType() {
        return skinType;
    }

    public void setSkinType(SkinType skinType) {
        if (this.skinType != skinType) {
            this.skinType = skinType;
            this.invalidateCache();
        }
    }

    public ModelType getModelType() {
        return modelType;
    }

    public void setModelType(ModelType modelType) {
        this.modelType = modelType;
    }

    public String getSkinValue() {
        return skinValue;
    }

    public void setSkinValue(String skinValue) {
        if (!Objects.equals(this.skinValue, skinValue)) {
            this.skinValue = Objects.requireNonNullElse(skinValue, "");
            this.invalidateCache();
        }
    }

    public void invalidateCache() {
        this.filePlayerSkin = null;
        this.cachedPlayerSkin = null;
        this.skinFetchPending = false;
    }

    public PlayerSkin getOrLoadSkin() {
        if (this.cachedPlayerSkin != null) {
            return this.cachedPlayerSkin;
        }

        switch (this.skinType) {
            case DEFAULT_STEVE -> {
                this.cachedPlayerSkin = DefaultPlayerSkin.get(UUID.nameUUIDFromBytes(new byte[]{0}));
                return this.cachedPlayerSkin;
            }
            case DEFAULT_ALEX -> {
                this.cachedPlayerSkin = DefaultPlayerSkin.get(UUID.nameUUIDFromBytes(new byte[]{1}));
                return this.cachedPlayerSkin;
            }
            case FILE -> {
                if (this.skinValue != null && !this.skinValue.isBlank()) {
                    if (this.filePlayerSkin == null) {
                        this.filePlayerSkin = new FilePlayerSkin(this.skinValue);
                    }
                    PlayerSkin loaded = this.filePlayerSkin.getSkin();
                    if (loaded != null) {
                        this.cachedPlayerSkin = loaded;
                        return this.cachedPlayerSkin;
                    }
                }
                return DefaultPlayerSkin.getDefaultSkin();
            }
            case USERNAME -> {
                if (this.skinValue != null && !this.skinValue.isBlank()) {
                    long now = System.currentTimeMillis();
                    if (!this.skinFetchPending && (now - this.lastFetchAttempt > 3000)) {
                        this.skinFetchPending = true;
                        this.lastFetchAttempt = now;
                        CompletableFuture.runAsync(() -> {
                            try {
                                Minecraft minecraft = Minecraft.getInstance();
                                UUID targetUuid;
                                try {
                                    targetUuid = UUID.fromString(this.skinValue.trim());
                                } catch (IllegalArgumentException e) {
                                    targetUuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + this.skinValue.trim()).getBytes(StandardCharsets.UTF_8));
                                }

                                GameProfile profile = new GameProfile(targetUuid, this.skinValue.trim());
                                try {
                                    ProfileResult fullProfile = minecraft.getMinecraftSessionService().fetchProfile(targetUuid, true);
                                    if (fullProfile != null && fullProfile.profile() != null) {
                                        profile = fullProfile.profile();
                                    }
                                } catch (Exception ignored) {}

                                CompletableFuture<Optional<PlayerSkin>> skinFuture = minecraft.getSkinManager().getOrLoad(profile);
                                skinFuture.thenAccept(optSkin -> {
                                    optSkin.ifPresent(skin -> this.cachedPlayerSkin = skin);
                                    this.skinFetchPending = false;
                                });
                            } catch (Exception e) {
                                Flashback.LOGGER.warn("Failed to fetch skin for " + this.skinValue, e);
                                this.skinFetchPending = false;
                            }
                        });
                    }
                }
                return DefaultPlayerSkin.getDefaultSkin();
            }
        }

        return DefaultPlayerSkin.getDefaultSkin();
    }

    public boolean isSlimModel() {
        if (this.modelType == ModelType.SLIM) return true;
        if (this.modelType == ModelType.WIDE) return false;

        PlayerSkin skin = getOrLoadSkin();
        return skin != null && skin.model() == PlayerSkin.Model.SLIM;
    }

    public ActorSkin copy() {
        return new ActorSkin(this.skinType, this.skinValue, this.modelType);
    }

    public static class TypeAdapter implements JsonSerializer<ActorSkin>, JsonDeserializer<ActorSkin> {
        @Override
        public ActorSkin deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            SkinType type = obj.has("type") ? SkinType.valueOf(obj.get("type").getAsString()) : SkinType.DEFAULT_STEVE;
            ModelType model = obj.has("model") ? ModelType.valueOf(obj.get("model").getAsString()) : ModelType.AUTO;
            String val = obj.has("value") ? obj.get("value").getAsString() : "";
            return new ActorSkin(type, val, model);
        }

        @Override
        public JsonElement serialize(ActorSkin src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("type", src.skinType.name());
            obj.addProperty("model", src.modelType.name());
            obj.addProperty("value", src.skinValue);
            return obj;
        }
    }
}
