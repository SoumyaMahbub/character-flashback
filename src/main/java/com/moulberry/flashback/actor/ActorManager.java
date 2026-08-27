package com.moulberry.flashback.actor;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import net.minecraft.client.multiplayer.ClientLevel;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class ActorManager {

    private final List<FlashbackActor> actors = new ArrayList<>();
    private transient UUID selectedActorId = null;
    private transient ActorTrackType.Category selectedCategory = ActorTrackType.Category.WORLD_TRANSFORM;
    private transient ActorTrackType selectedTrackType = ActorTrackType.WORLD_POS_X;

    private final transient Map<UUID, ActorPlayer> spawnedPlayers = new HashMap<>();

    public ActorManager() {}

    public List<FlashbackActor> getActors() {
        return actors;
    }

    public synchronized FlashbackActor addActor(String name, Vector3f position, int spawnTick) {
        UUID id = UUID.randomUUID();
        String finalName = (name == null || name.isBlank()) ? "Actor " + (actors.size() + 1) : name;
        FlashbackActor actor = new FlashbackActor(id, finalName);
        if (position != null) {
            actor.setPosition(position);
            actor.insertAllKeyframes(spawnTick, InterpolationType.LINEAR);
        }
        this.actors.add(actor);
        this.selectedActorId = id;
        return actor;
    }

    public synchronized FlashbackActor addActor(String name, Vector3f position) {
        return addActor(name, position, 0);
    }

    public synchronized boolean removeActor(UUID id) {
        if (id == null) return false;
        boolean removed = this.actors.removeIf(a -> a.getId().equals(id));
        if (removed && Objects.equals(this.selectedActorId, id)) {
            this.selectedActorId = this.actors.isEmpty() ? null : this.actors.get(0).getId();
        }
        ActorPlayer player = spawnedPlayers.remove(id);
        if (player != null && player.clientLevel != null) {
            player.clientLevel.removeEntity(player.getId(), net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
        }
        return removed;
    }

    public synchronized FlashbackActor duplicateActor(UUID id) {
        FlashbackActor source = getActor(id);
        if (source == null) return null;

        UUID newId = UUID.randomUUID();
        String newName = source.getName() + " (Copy)";
        FlashbackActor copy = source.duplicate(newId, newName);
        copy.setPosition(new Vector3f(source.getEvalPosX() + 1.0f, source.getEvalPosY(), source.getEvalPosZ()));
        this.actors.add(copy);
        this.selectedActorId = newId;
        return copy;
    }

    public synchronized FlashbackActor getActor(UUID id) {
        if (id == null) return null;
        for (FlashbackActor actor : this.actors) {
            if (actor.getId().equals(id)) {
                return actor;
            }
        }
        return null;
    }

    public synchronized FlashbackActor getSelectedActor() {
        if (this.selectedActorId == null) {
            if (!this.actors.isEmpty()) {
                this.selectedActorId = this.actors.get(0).getId();
                return this.actors.get(0);
            }
            return null;
        }
        FlashbackActor selected = getActor(this.selectedActorId);
        if (selected == null && !this.actors.isEmpty()) {
            this.selectedActorId = this.actors.get(0).getId();
            return this.actors.get(0);
        }
        return selected;
    }

    public UUID getSelectedActorId() {
        return selectedActorId;
    }

    public void setSelectedActorId(UUID selectedActorId) {
        this.selectedActorId = selectedActorId;
    }

    public ActorTrackType.Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(ActorTrackType.Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public ActorTrackType getSelectedTrackType() {
        return selectedTrackType;
    }

    public void setSelectedTrackType(ActorTrackType selectedTrackType) {
        this.selectedTrackType = selectedTrackType;
    }

    public synchronized void syncToLevel(ClientLevel level, float tick, boolean isPlaying) {
        if (level == null) return;

        for (FlashbackActor actor : this.actors) {
            if (!actor.isVisible()) {
                ActorPlayer existing = spawnedPlayers.remove(actor.getId());
                if (existing != null) {
                    level.removeEntity(existing.getId(), net.minecraft.world.entity.Entity.RemovalReason.DISCARDED);
                }
                continue;
            }

            ActorPlayer player = spawnedPlayers.get(actor.getId());
            if (player == null || player.clientLevel != level) {
                player = new ActorPlayer(level, actor);
                int entityId = -1000 - Math.abs(actor.getId().hashCode() % 10000);
                player.setId(entityId);
                level.addEntity(player);
                spawnedPlayers.put(actor.getId(), player);
            }

            player.updateFromActor(tick, isPlaying);
        }
    }

    public synchronized void evaluateAll(float tick, boolean isPlaying) {
        for (FlashbackActor actor : this.actors) {
            actor.evaluate(tick, isPlaying);
        }
    }

    public ActorManager copy() {
        ActorManager copy = new ActorManager();
        for (FlashbackActor actor : this.actors) {
            copy.actors.add(actor.duplicate(actor.getId(), actor.getName()));
        }
        copy.selectedActorId = this.selectedActorId;
        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<ActorManager>, JsonDeserializer<ActorManager> {
        @Override
        public ActorManager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            ActorManager manager = new ActorManager();
            if (obj.has("actors")) {
                JsonArray array = obj.getAsJsonArray("actors");
                for (JsonElement elem : array) {
                    FlashbackActor actor = context.deserialize(elem, FlashbackActor.class);
                    if (actor != null) {
                        manager.actors.add(actor);
                    }
                }
            }
            return manager;
        }

        @Override
        public JsonElement serialize(ActorManager src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            JsonArray array = new JsonArray();
            for (FlashbackActor actor : src.actors) {
                array.add(context.serialize(actor));
            }
            obj.add("actors", array);
            return obj;
        }
    }
}
