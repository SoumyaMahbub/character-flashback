package com.moulberry.flashback.state;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.keyframe.Keyframe;
import com.moulberry.flashback.keyframe.KeyframeType;

import java.lang.reflect.Type;

public interface EditorSceneHistoryAction {

    void apply(EditorScene editorScene);

    record SetKeyframe(KeyframeType<?> type, int trackIndex, int tick, Keyframe keyframe) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (this.trackIndex < editorScene.keyframeTracks.size()) {
                KeyframeTrack track = editorScene.keyframeTracks.get(this.trackIndex);
                if (track.keyframeType == this.type) {
                    track.keyframesByTick.put(this.tick, this.keyframe.copy());
                }
            }
        }

        public static class TypeAdapter implements JsonSerializer<SetKeyframe>, JsonDeserializer<SetKeyframe> {
            @Override
            public SetKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                KeyframeType<?> type = context.deserialize(jsonObject.get("keyframe_type"), KeyframeType.class);
                int trackIndex = jsonObject.get("trackIndex").getAsInt();
                int tick = jsonObject.get("tick").getAsInt();
                Keyframe keyframe = context.deserialize(jsonObject.get("keyframe"), Keyframe.class);
                return new SetKeyframe(type, trackIndex, tick, keyframe);
            }

            @Override
            public JsonElement serialize(SetKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "set_keyframe");
                jsonObject.add("keyframe_type", context.serialize(src.type));
                jsonObject.addProperty("trackIndex", src.trackIndex);
                jsonObject.addProperty("tick", src.tick);
                jsonObject.add("keyframe", context.serialize(src.keyframe));
                return jsonObject;
            }
        }
    }

    record RemoveKeyframe(KeyframeType<?> type, int trackIndex, int tick) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (this.trackIndex < editorScene.keyframeTracks.size()) {
                KeyframeTrack track = editorScene.keyframeTracks.get(this.trackIndex);
                if (track.keyframeType == this.type) {
                    track.keyframesByTick.remove(this.tick);
                }
            }
        }

        public static class TypeAdapter implements JsonSerializer<RemoveKeyframe>, JsonDeserializer<RemoveKeyframe> {
            @Override
            public RemoveKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                KeyframeType<?> type = context.deserialize(jsonObject.get("keyframe_type"), KeyframeType.class);
                int trackIndex = jsonObject.get("trackIndex").getAsInt();
                int tick = jsonObject.get("tick").getAsInt();
                return new RemoveKeyframe(type, trackIndex, tick);
            }

            @Override
            public JsonElement serialize(RemoveKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "remove_keyframe");
                jsonObject.add("keyframe_type", context.serialize(src.type));
                jsonObject.addProperty("trackIndex", src.trackIndex);
                jsonObject.addProperty("tick", src.tick);
                return jsonObject;
            }
        }
    }

    record AddTrack(KeyframeType<?> type, int trackIndex) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (this.trackIndex <= editorScene.keyframeTracks.size()) {
                editorScene.keyframeTracks.add(this.trackIndex, new KeyframeTrack(this.type));
            }
        }

        public static class TypeAdapter implements JsonSerializer<AddTrack>, JsonDeserializer<AddTrack> {
            @Override
            public AddTrack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                KeyframeType<?> type = context.deserialize(jsonObject.get("keyframe_type"), KeyframeType.class);
                int trackIndex = jsonObject.get("trackIndex").getAsInt();
                return new AddTrack(type, trackIndex);
            }

            @Override
            public JsonElement serialize(AddTrack src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "add_track");
                jsonObject.add("keyframe_type", context.serialize(src.type));
                jsonObject.addProperty("trackIndex", src.trackIndex);
                return jsonObject;
            }
        }
    }

    record RemoveTrack(KeyframeType<?> type, int trackIndex) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (this.trackIndex < editorScene.keyframeTracks.size()) {
                KeyframeTrack keyframeTrack = editorScene.keyframeTracks.get(this.trackIndex);
                if (keyframeTrack.keyframeType == type) {
                    editorScene.keyframeTracks.remove(this.trackIndex);
                }
            }
        }

        public static class TypeAdapter implements JsonSerializer<RemoveTrack>, JsonDeserializer<RemoveTrack> {
            @Override
            public RemoveTrack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                KeyframeType<?> type = context.deserialize(jsonObject.get("keyframe_type"), KeyframeType.class);
                int trackIndex = jsonObject.get("trackIndex").getAsInt();
                return new RemoveTrack(type, trackIndex);
            }

            @Override
            public JsonElement serialize(RemoveTrack src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "remove_track");
                jsonObject.add("keyframe_type", context.serialize(src.type));
                jsonObject.addProperty("trackIndex", src.trackIndex);
                return jsonObject;
            }
        }
    }

    record SetCharacterKeyframe(java.util.UUID characterId, com.moulberry.flashback.character.CharacterTrackType trackType, int tick, com.moulberry.flashback.character.CharacterKeyframe keyframe) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (editorScene.characterManager != null) {
                var character = editorScene.characterManager.getCharacter(this.characterId);
                if (character != null) {
                    character.setKeyframe(this.trackType, this.tick, this.keyframe.getValue(), this.keyframe.getInterpolationType());
                }
            }
        }

        public static class TypeAdapter implements JsonSerializer<SetCharacterKeyframe>, JsonDeserializer<SetCharacterKeyframe> {
            @Override
            public SetCharacterKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                java.util.UUID characterId = java.util.UUID.fromString(jsonObject.get("character_id").getAsString());
                com.moulberry.flashback.character.CharacterTrackType trackType = com.moulberry.flashback.character.CharacterTrackType.valueOf(jsonObject.get("track_type").getAsString());
                int tick = jsonObject.get("tick").getAsInt();
                com.moulberry.flashback.character.CharacterKeyframe keyframe = context.deserialize(jsonObject.get("keyframe"), com.moulberry.flashback.character.CharacterKeyframe.class);
                return new SetCharacterKeyframe(characterId, trackType, tick, keyframe);
            }

            @Override
            public JsonElement serialize(SetCharacterKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "set_character_keyframe");
                jsonObject.addProperty("character_id", src.characterId.toString());
                jsonObject.addProperty("track_type", src.trackType.name());
                jsonObject.addProperty("tick", src.tick);
                jsonObject.add("keyframe", context.serialize(src.keyframe));
                return jsonObject;
            }
        }
    }

    record RemoveCharacterKeyframe(java.util.UUID characterId, com.moulberry.flashback.character.CharacterTrackType trackType, int tick) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (editorScene.characterManager != null) {
                var character = editorScene.characterManager.getCharacter(this.characterId);
                if (character != null) {
                    character.removeKeyframe(this.trackType, this.tick);
                }
            }
        }

        public static class TypeAdapter implements JsonSerializer<RemoveCharacterKeyframe>, JsonDeserializer<RemoveCharacterKeyframe> {
            @Override
            public RemoveCharacterKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                java.util.UUID characterId = java.util.UUID.fromString(jsonObject.get("character_id").getAsString());
                com.moulberry.flashback.character.CharacterTrackType trackType = com.moulberry.flashback.character.CharacterTrackType.valueOf(jsonObject.get("track_type").getAsString());
                int tick = jsonObject.get("tick").getAsInt();
                return new RemoveCharacterKeyframe(characterId, trackType, tick);
            }

            @Override
            public JsonElement serialize(RemoveCharacterKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "remove_character_keyframe");
                jsonObject.addProperty("character_id", src.characterId.toString());
                jsonObject.addProperty("track_type", src.trackType.name());
                jsonObject.addProperty("tick", src.tick);
                return jsonObject;
            }
        }
    }

    record AddCharacter(com.moulberry.flashback.character.AnimatedCharacter character) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (editorScene.characterManager != null) {
                if (editorScene.characterManager.getCharacter(this.character.getId()) == null) {
                    editorScene.characterManager.getCharacters().add(this.character.duplicate(this.character.getId(), this.character.getName()));
                }
            }
        }

        public static class TypeAdapter implements JsonSerializer<AddCharacter>, JsonDeserializer<AddCharacter> {
            @Override
            public AddCharacter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                com.moulberry.flashback.character.AnimatedCharacter character = context.deserialize(jsonObject.get("character"), com.moulberry.flashback.character.AnimatedCharacter.class);
                return new AddCharacter(character);
            }

            @Override
            public JsonElement serialize(AddCharacter src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "add_character");
                jsonObject.add("character", context.serialize(src.character));
                return jsonObject;
            }
        }
    }

    record RemoveCharacter(com.moulberry.flashback.character.AnimatedCharacter character) implements EditorSceneHistoryAction {
        @Override
        public void apply(EditorScene editorScene) {
            if (editorScene.characterManager != null) {
                editorScene.characterManager.removeCharacter(this.character.getId());
            }
        }

        public static class TypeAdapter implements JsonSerializer<RemoveCharacter>, JsonDeserializer<RemoveCharacter> {
            @Override
            public RemoveCharacter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
                JsonObject jsonObject = json.getAsJsonObject();
                com.moulberry.flashback.character.AnimatedCharacter character = context.deserialize(jsonObject.get("character"), com.moulberry.flashback.character.AnimatedCharacter.class);
                return new RemoveCharacter(character);
            }

            @Override
            public JsonElement serialize(RemoveCharacter src, Type typeOfSrc, JsonSerializationContext context) {
                JsonObject jsonObject = new JsonObject();
                jsonObject.addProperty("action_type", "remove_character");
                jsonObject.add("character", context.serialize(src.character));
                return jsonObject;
            }
        }
    }

    class TypeAdapter implements JsonSerializer<EditorSceneHistoryAction>, JsonDeserializer<EditorSceneHistoryAction> {
        @Override
        public EditorSceneHistoryAction deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            String type = jsonObject.get("action_type").getAsString();
            return switch (type) {
                case "set_keyframe" -> context.deserialize(json, SetKeyframe.class);
                case "remove_keyframe" -> context.deserialize(json, RemoveKeyframe.class);
                case "add_track" -> context.deserialize(json, AddTrack.class);
                case "remove_track" -> context.deserialize(json, RemoveTrack.class);
                case "set_character_keyframe" -> context.deserialize(json, SetCharacterKeyframe.class);
                case "remove_character_keyframe" -> context.deserialize(json, RemoveCharacterKeyframe.class);
                case "add_character" -> context.deserialize(json, AddCharacter.class);
                case "remove_character" -> context.deserialize(json, RemoveCharacter.class);
                default -> throw new IllegalStateException("Unknown action type: " + type);
            };
        }

        @Override
        public JsonElement serialize(EditorSceneHistoryAction src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject;
            switch (src) {
                case SetKeyframe setKeyframe -> {
                    jsonObject = (JsonObject) context.serialize(setKeyframe);
                    jsonObject.addProperty("action_type", "set_keyframe");
                }
                case RemoveKeyframe removeKeyframe -> {
                    jsonObject = (JsonObject) context.serialize(removeKeyframe);
                    jsonObject.addProperty("action_type", "remove_keyframe");
                }
                case AddTrack addTrack -> {
                    jsonObject = (JsonObject) context.serialize(addTrack);
                    jsonObject.addProperty("action_type", "add_track");
                }
                case RemoveTrack removeTrack -> {
                    jsonObject = (JsonObject) context.serialize(removeTrack);
                    jsonObject.addProperty("action_type", "remove_track");
                }
                case SetCharacterKeyframe setCharacterKeyframe -> {
                    jsonObject = (JsonObject) context.serialize(setCharacterKeyframe);
                    jsonObject.addProperty("action_type", "set_character_keyframe");
                }
                case RemoveCharacterKeyframe removeCharacterKeyframe -> {
                    jsonObject = (JsonObject) context.serialize(removeCharacterKeyframe);
                    jsonObject.addProperty("action_type", "remove_character_keyframe");
                }
                case AddCharacter addCharacter -> {
                    jsonObject = (JsonObject) context.serialize(addCharacter);
                    jsonObject.addProperty("action_type", "add_character");
                }
                case RemoveCharacter removeCharacter -> {
                    jsonObject = (JsonObject) context.serialize(removeCharacter);
                    jsonObject.addProperty("action_type", "remove_character");
                }
                default -> throw new IllegalStateException("Unknown action type: " + src.getClass());
            }
            return jsonObject;
        }
    }

}
