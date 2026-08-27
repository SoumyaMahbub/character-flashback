package com.moulberry.flashback.character;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.reflect.TypeToken;
import com.moulberry.flashback.keyframe.interpolation.InterpolationType;
import com.moulberry.flashback.keyframe.interpolation.SidedInterpolationType;

import java.lang.reflect.Type;
import java.util.Map;
import java.util.TreeMap;

public class CharacterAnimationTrack {

    private final CharacterTrackType trackType;
    private final TreeMap<Integer, CharacterKeyframe> keyframesByTick = new TreeMap<>();
    private boolean enabled = true;

    public CharacterAnimationTrack(CharacterTrackType trackType) {
        this.trackType = trackType;
    }

    public CharacterTrackType getTrackType() {
        return trackType;
    }

    public TreeMap<Integer, CharacterKeyframe> getKeyframesByTick() {
        return keyframesByTick;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public void setKeyframe(int tick, float value, InterpolationType interpolationType) {
        this.keyframesByTick.put(tick, new CharacterKeyframe(value, interpolationType));
    }

    public void setKeyframe(int tick, CharacterKeyframe keyframe) {
        this.keyframesByTick.put(tick, keyframe.copy());
    }

    public void removeKeyframe(int tick) {
        this.keyframesByTick.remove(tick);
    }

    public boolean hasKeyframe(int tick) {
        return this.keyframesByTick.containsKey(tick);
    }

    public boolean isEmpty() {
        return this.keyframesByTick.isEmpty();
    }

    public float evaluate(float tick) {
        if (!this.enabled || this.keyframesByTick.isEmpty()) {
            return this.trackType.getDefaultValue();
        }

        if (this.keyframesByTick.size() == 1) {
            return this.keyframesByTick.firstEntry().getValue().getValue();
        }

        Map.Entry<Integer, CharacterKeyframe> floorEntry = this.keyframesByTick.floorEntry((int) Math.floor(tick));
        Map.Entry<Integer, CharacterKeyframe> ceilEntry = this.keyframesByTick.ceilingEntry((int) Math.ceil(tick));

        if (floorEntry == null) {
            return this.keyframesByTick.firstEntry().getValue().getValue();
        }
        if (ceilEntry == null || floorEntry.getKey().equals(ceilEntry.getKey())) {
            return floorEntry.getValue().getValue();
        }

        int t0 = floorEntry.getKey();
        int t1 = ceilEntry.getKey();
        float v0 = floorEntry.getValue().getValue();
        float v1 = ceilEntry.getValue().getValue();

        if (tick <= t0) return v0;
        if (tick >= t1) return v1;

        float amount = (tick - t0) / (float) (t1 - t0);

        InterpolationType interpolationType = floorEntry.getValue().getInterpolationType();
        SidedInterpolationType left = interpolationType.rightSide;
        SidedInterpolationType right = ceilEntry.getValue().getInterpolationType().leftSide;

        if (left == SidedInterpolationType.HOLD) {
            return v0;
        }

        if (left == SidedInterpolationType.SMOOTH || right == SidedInterpolationType.SMOOTH) {
            Map.Entry<Integer, CharacterKeyframe> beforeEntry = this.keyframesByTick.floorEntry(t0 - 1);
            Map.Entry<Integer, CharacterKeyframe> afterAfterEntry = this.keyframesByTick.ceilingEntry(t1 + 1);

            float p0 = beforeEntry != null ? beforeEntry.getValue().getValue() : v0;
            float p1 = v0;
            float p2 = v1;
            float p3 = afterAfterEntry != null ? afterAfterEntry.getValue().getValue() : v1;

            return catmullRom(p0, p1, p2, p3, amount);
        }

        double adjustedAmount = SidedInterpolationType.interpolate(left, right, amount);
        return (float) (v0 + (v1 - v0) * adjustedAmount);
    }

    private static float catmullRom(float p0, float p1, float p2, float p3, float t) {
        float t2 = t * t;
        float t3 = t2 * t;
        return 0.5f * ((2.0f * p1) +
                (-p0 + p2) * t +
                (2.0f * p0 - 5.0f * p1 + 4.0f * p2 - p3) * t2 +
                (-p0 + 3.0f * p1 - 3.0f * p2 + p3) * t3);
    }

    public CharacterAnimationTrack copy() {
        CharacterAnimationTrack copy = new CharacterAnimationTrack(this.trackType);
        copy.enabled = this.enabled;
        for (Map.Entry<Integer, CharacterKeyframe> entry : this.keyframesByTick.entrySet()) {
            copy.keyframesByTick.put(entry.getKey(), entry.getValue().copy());
        }
        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<CharacterAnimationTrack>, JsonDeserializer<CharacterAnimationTrack> {
        private static final Type MAP_TYPE = new TypeToken<TreeMap<Integer, CharacterKeyframe>>() {}.getType();

        @Override
        public CharacterAnimationTrack deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            CharacterTrackType trackType = CharacterTrackType.valueOf(jsonObject.get("track_type").getAsString());
            CharacterAnimationTrack track = new CharacterAnimationTrack(trackType);
            if (jsonObject.has("enabled")) {
                track.setEnabled(jsonObject.get("enabled").getAsBoolean());
            }
            if (jsonObject.has("keyframes")) {
                TreeMap<Integer, CharacterKeyframe> keyframes = context.deserialize(jsonObject.get("keyframes"), MAP_TYPE);
                if (keyframes != null) {
                    track.keyframesByTick.putAll(keyframes);
                }
            }
            return track;
        }

        @Override
        public JsonElement serialize(CharacterAnimationTrack src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject jsonObject = new JsonObject();
            jsonObject.addProperty("track_type", src.trackType.name());
            jsonObject.addProperty("enabled", src.enabled);
            jsonObject.add("keyframes", context.serialize(src.keyframesByTick, MAP_TYPE));
            return jsonObject;
        }
    }
}
