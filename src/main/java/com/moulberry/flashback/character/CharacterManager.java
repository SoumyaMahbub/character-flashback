package com.moulberry.flashback.character;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class CharacterManager {

    private final List<AnimatedCharacter> characters = new ArrayList<>();
    private transient UUID selectedCharacterId = null;
    private transient CharacterTrackType.Category selectedCategory = CharacterTrackType.Category.WORLD_TRANSFORM;
    private transient CharacterTrackType selectedTrackType = CharacterTrackType.WORLD_POS_X;

    public CharacterManager() {}

    public List<AnimatedCharacter> getCharacters() {
        return characters;
    }

    public synchronized AnimatedCharacter addCharacter(String name, Vector3f position, int spawnTick) {
        UUID id = UUID.randomUUID();
        String finalName = (name == null || name.isBlank()) ? "Character " + (characters.size() + 1) : name;
        AnimatedCharacter character = new AnimatedCharacter(id, finalName);
        if (position != null) {
            character.setPosition(position);
            character.insertAllKeyframes(spawnTick, com.moulberry.flashback.keyframe.interpolation.InterpolationType.LINEAR);
        }
        this.characters.add(character);
        this.selectedCharacterId = id;
        return character;
    }

    public synchronized AnimatedCharacter addCharacter(String name, Vector3f position) {
        return addCharacter(name, position, 0);
    }

    public synchronized boolean removeCharacter(UUID id) {
        if (id == null) return false;
        boolean removed = this.characters.removeIf(c -> c.getId().equals(id));
        if (removed && Objects.equals(this.selectedCharacterId, id)) {
            this.selectedCharacterId = this.characters.isEmpty() ? null : this.characters.get(0).getId();
        }
        return removed;
    }

    public synchronized AnimatedCharacter duplicateCharacter(UUID id) {
        AnimatedCharacter source = getCharacter(id);
        if (source == null) return null;

        UUID newId = UUID.randomUUID();
        String newName = source.getName() + " (Copy)";
        AnimatedCharacter copy = source.duplicate(newId, newName);
        // Slightly offset position so it's visible next to original
        copy.setPosition(new Vector3f(source.getEvalPosX() + 1.0f, source.getEvalPosY(), source.getEvalPosZ()));
        this.characters.add(copy);
        this.selectedCharacterId = newId;
        return copy;
    }

    public synchronized AnimatedCharacter getCharacter(UUID id) {
        if (id == null) return null;
        for (AnimatedCharacter character : this.characters) {
            if (character.getId().equals(id)) {
                return character;
            }
        }
        return null;
    }

    public synchronized AnimatedCharacter getSelectedCharacter() {
        if (this.selectedCharacterId == null) {
            if (!this.characters.isEmpty()) {
                this.selectedCharacterId = this.characters.get(0).getId();
                return this.characters.get(0);
            }
            return null;
        }
        AnimatedCharacter selected = getCharacter(this.selectedCharacterId);
        if (selected == null && !this.characters.isEmpty()) {
            this.selectedCharacterId = this.characters.get(0).getId();
            return this.characters.get(0);
        }
        return selected;
    }

    public UUID getSelectedCharacterId() {
        return selectedCharacterId;
    }

    public void setSelectedCharacterId(UUID selectedCharacterId) {
        this.selectedCharacterId = selectedCharacterId;
    }

    public CharacterTrackType.Category getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(CharacterTrackType.Category selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public CharacterTrackType getSelectedTrackType() {
        return selectedTrackType;
    }

    public void setSelectedTrackType(CharacterTrackType selectedTrackType) {
        this.selectedTrackType = selectedTrackType;
    }

    public synchronized void evaluateAll(float tick, boolean isReplayPlaying) {
        for (AnimatedCharacter character : this.characters) {
            character.evaluate(tick, isReplayPlaying);
        }
    }

    public CharacterManager copy() {
        CharacterManager copy = new CharacterManager();
        for (AnimatedCharacter character : this.characters) {
            copy.characters.add(character.duplicate(character.getId(), character.getName()));
        }
        copy.selectedCharacterId = this.selectedCharacterId;
        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<CharacterManager>, JsonDeserializer<CharacterManager> {
        @Override
        public CharacterManager deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            CharacterManager manager = new CharacterManager();
            if (obj.has("characters")) {
                JsonArray array = obj.getAsJsonArray("characters");
                for (JsonElement elem : array) {
                    AnimatedCharacter character = context.deserialize(elem, AnimatedCharacter.class);
                    if (character != null) {
                        manager.characters.add(character);
                    }
                }
            }
            return manager;
        }

        @Override
        public JsonElement serialize(CharacterManager src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            JsonArray array = new JsonArray();
            for (AnimatedCharacter character : src.characters) {
                array.add(context.serialize(character));
            }
            obj.add("characters", array);
            return obj;
        }
    }
}
