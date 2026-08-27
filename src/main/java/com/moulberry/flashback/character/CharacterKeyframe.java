package com.moulberry.flashback.character;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;

import java.lang.reflect.Type;
import java.util.Objects;

public class CharacterKeyframe {

    private float value;
    private InterpolationType interpolationType = InterpolationType.LINEAR;

    public CharacterKeyframe(float value) {
        this.value = value;
        this.interpolationType = InterpolationType.LINEAR;
    }

    public CharacterKeyframe(float value, InterpolationType interpolationType) {
        this.value = value;
        this.interpolationType = Objects.requireNonNullElse(interpolationType, InterpolationType.LINEAR);
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public InterpolationType getInterpolationType() {
        return interpolationType;
    }

    public void setInterpolationType(InterpolationType interpolationType) {
        this.interpolationType = Objects.requireNonNullElse(interpolationType, InterpolationType.LINEAR);
    }

    public CharacterKeyframe copy() {
        return new CharacterKeyframe(this.value, this.interpolationType);
    }

    public static class TypeAdapter implements JsonSerializer<CharacterKeyframe>, JsonDeserializer<CharacterKeyframe> {
        @Override
        public CharacterKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            float val = jsonObject.get("value").getAsFloat();
            InterpolationType interp = context.deserialize(jsonObject.get("interpolation"), InterpolationType.class);
            return new CharacterKeyframe(val, interp);
        }

        @Override
        public JsonElement serialize(CharacterKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("value", src.value);
            jsonObject.add("interpolation", context.serialize(src.interpolationType));
            return jsonObject;
        }
    }
}
