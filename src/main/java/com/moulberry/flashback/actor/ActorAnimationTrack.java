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
import net.minecraft.util.Mth;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.NavigableMap;
import java.util.TreeMap;

public class ActorAnimationTrack {

    private final ActorTrackType trackType;
    private final NavigableMap<Integer, ActorKeyframe> keyframes = new TreeMap<>();

    public ActorAnimationTrack(ActorTrackType trackType) {
        this.trackType = trackType;
    }

    public ActorTrackType getTrackType() {
        return trackType;
    }

    public NavigableMap<Integer, ActorKeyframe> getKeyframes() {
        return keyframes;
    }

    public boolean isEmpty() {
        return keyframes.isEmpty();
    }

    public void setKeyframe(int tick, float value, InterpolationType interpolationType) {
        keyframes.put(tick, new ActorKeyframe(tick, value, interpolationType));
    }

    public void removeKeyframe(int tick) {
        keyframes.remove(tick);
    }

    public void clear() {
        keyframes.clear();
    }

    public float evaluate(float tick) {
        if (keyframes.isEmpty()) {
            return trackType.getDefaultValue();
        }

        Map.Entry<Integer, ActorKeyframe> floor = keyframes.floorEntry((int) Math.floor(tick));
        Map.Entry<Integer, ActorKeyframe> ceiling = keyframes.ceilingEntry((int) Math.floor(tick));

        if (floor == null && ceiling == null) {
            return trackType.getDefaultValue();
        }
        if (floor == null) {
            return ceiling.getValue().getValue();
        }
        if (ceiling == null || floor.getKey().equals(ceiling.getKey())) {
            Map.Entry<Integer, ActorKeyframe> higher = keyframes.higherEntry(floor.getKey());
            if (higher == null) {
                return floor.getValue().getValue();
            }
            ceiling = higher;
        }

        int t0 = floor.getKey();
        int t1 = ceiling.getKey();
        float v0 = floor.getValue().getValue();
        float v1 = ceiling.getValue().getValue();

        if (t0 == t1 || tick <= t0) {
            return v0;
        }
        if (tick >= t1) {
            return v1;
        }

        float progress = (tick - t0) / (float) (t1 - t0);
        InterpolationType interp = floor.getValue().getInterpolationType();

        return switch (interp) {
            case HOLD -> v0;
            case SMOOTH, EASE_IN_OUT -> {
                float cosP = (1.0f - (float) Math.cos(progress * Math.PI)) * 0.5f;
                yield Mth.lerp(cosP, v0, v1);
            }
            case EASE_IN -> {
                float expP = (float) Math.pow(progress, 2.0);
                yield Mth.lerp(expP, v0, v1);
            }
            case EASE_OUT -> {
                float expP = 1.0f - (float) Math.pow(1.0 - progress, 2.0);
                yield Mth.lerp(expP, v0, v1);
            }
            case LINEAR, HERMITE -> Mth.lerp(progress, v0, v1);
        };
    }

    public ActorAnimationTrack copy() {
        ActorAnimationTrack copy = new ActorAnimationTrack(trackType);
        for (ActorKeyframe kf : keyframes.values()) {
            copy.keyframes.put(kf.getTick(), kf.copy());
        }
        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<ActorAnimationTrack>, JsonDeserializer<ActorAnimationTrack> {
        @Override
        public ActorAnimationTrack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            ActorTrackType type = ActorTrackType.valueOf(obj.get("track_type").getAsString());
            ActorAnimationTrack track = new ActorAnimationTrack(type);

            if (obj.has("keyframes")) {
                JsonArray array = obj.getAsJsonArray("keyframes");
                for (JsonElement elem : array) {
                    ActorKeyframe kf = context.deserialize(elem, ActorKeyframe.class);
                    if (kf != null) {
                        track.keyframes.put(kf.getTick(), kf);
                    }
                }
            }
            return track;
        }

        @Override
        public JsonElement serialize(ActorAnimationTrack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("track_type", src.trackType.name());
            JsonArray array = new JsonArray();
            for (ActorKeyframe kf : src.keyframes.values()) {
                array.add(context.serialize(kf));
            }
            obj.add("keyframes", array);
            return obj;
        }
    }
}
