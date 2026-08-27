package com.moulberry.flashback.actor;

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

public class ActorKeyframe {

    private int tick;
    private float value;
    private InterpolationType interpolationType;

    public ActorKeyframe(int tick, float value, InterpolationType interpolationType) {
        this.tick = tick;
        this.value = value;
        this.interpolationType = Objects.requireNonNullElse(interpolationType, InterpolationType.LINEAR);
    }

    public int getTick() {
        return tick;
    }

    public void setTick(int tick) {
        this.tick = tick;
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

    public ActorKeyframe copy() {
        return new ActorKeyframe(tick, value, interpolationType);
    }

    public static class TypeAdapter implements JsonSerializer<ActorKeyframe>, JsonDeserializer<ActorKeyframe> {
        @Override
        public ActorKeyframe deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            int tick = obj.get("tick").getAsInt();
            float value = obj.get("value").getAsFloat();
            InterpolationType interp = InterpolationType.LINEAR;
            if (obj.has("interp")) {
                try {
                    interp = InterpolationType.valueOf(obj.get("interp").getAsString());
                } catch (Exception ignored) {}
            }
            return new ActorKeyframe(tick, value, interp);
        }

        @Override
        public JsonElement serialize(ActorKeyframe src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("tick", src.tick);
            obj.addProperty("value", src.value);
            obj.addProperty("interp", src.interpolationType.name());
            return obj;
        }
    }
}
