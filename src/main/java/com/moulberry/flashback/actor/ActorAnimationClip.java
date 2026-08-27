package com.moulberry.flashback.actor;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.moulberry.flashback.Flashback;
import com.moulberry.flashback.combo_options.ComboOption;
import net.minecraft.util.Mth;

import java.lang.reflect.Type;

public class ActorAnimationClip {

    public enum ClipType implements ComboOption {
        NONE("None (Static Pose)"),
        IDLE("Idle Breathing"),
        WALK("Walk Cycle"),
        RUN("Run Cycle"),
        WAVE("Friendly Wave"),
        ZOMBIE_WALK("Zombie Walk"),
        ATTACK("Attack Swing"),
        SNEAK_WALK("Sneak Walk"),
        CHEER("Cheer / Celebrate"),
        DANCE("Dance Groove"),
        SWIM("Swimming Stroke"),
        SIT_IDLE("Sitting Idle"),
        BOW_AIM("Bow Aiming");

        private final String displayName;

        ClipType(String displayName) {
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

    private String name = "Active Clip";
    private ClipType clipType = ClipType.NONE;
    private int lengthTicks = 20;
    private boolean loop = true;
    private float speed = 1.0f;
    private int startTickOffset = 0;
    private float weight = 1.0f;
    private boolean realtimePreview = true;

    public ActorAnimationClip() {}

    public ActorAnimationClip(String name, ClipType clipType, int lengthTicks) {
        this.name = name;
        this.clipType = clipType;
        this.lengthTicks = Math.max(1, lengthTicks);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ClipType getClipType() {
        return clipType;
    }

    public void setClipType(ClipType clipType) {
        this.clipType = clipType;
        switch (clipType) {
            case IDLE -> this.lengthTicks = 40;
            case WALK -> this.lengthTicks = 20;
            case RUN -> this.lengthTicks = 14;
            case WAVE -> this.lengthTicks = 30;
            case ZOMBIE_WALK -> this.lengthTicks = 35;
            case ATTACK -> this.lengthTicks = 16;
            case SNEAK_WALK -> this.lengthTicks = 28;
            case CHEER -> this.lengthTicks = 24;
            case DANCE -> this.lengthTicks = 32;
            case SWIM -> this.lengthTicks = 24;
            case SIT_IDLE -> this.lengthTicks = 45;
            case BOW_AIM -> this.lengthTicks = 30;
            case NONE -> {}
        }
    }

    public int getLengthTicks() {
        return lengthTicks;
    }

    public void setLengthTicks(int lengthTicks) {
        this.lengthTicks = Math.max(1, lengthTicks);
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = Math.max(0.01f, speed);
    }

    public int getStartTickOffset() {
        return startTickOffset;
    }

    public void setStartTickOffset(int startTickOffset) {
        this.startTickOffset = startTickOffset;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = Mth.clamp(weight, 0.0f, 1.0f);
    }

    public boolean isRealtimePreview() {
        return realtimePreview;
    }

    public void setRealtimePreview(boolean realtimePreview) {
        this.realtimePreview = realtimePreview;
    }

    public void applyToPose(ActorPose targetPose, float currentTick, boolean isReplayPlaying) {
        if (this.clipType == ClipType.NONE || this.weight <= 0.001f || targetPose == null) {
            return;
        }

        float tickForClip;
        if (Flashback.isExporting() || isReplayPlaying) {
            tickForClip = currentTick;
        } else {
            if (this.realtimePreview) {
                tickForClip = (float) (System.currentTimeMillis() / 50.0);
            } else {
                tickForClip = currentTick;
            }
        }

        float effectiveTick = (tickForClip - this.startTickOffset) * this.speed;

        float cycleTime;
        if (this.loop) {
            float modTick = ((effectiveTick % this.lengthTicks) + this.lengthTicks) % this.lengthTicks;
            cycleTime = modTick / (float) this.lengthTicks;
        } else {
            if (effectiveTick < 0 || effectiveTick >= this.lengthTicks) return;
            cycleTime = effectiveTick / (float) this.lengthTicks;
        }

        float radians = cycleTime * (float) (Math.PI * 2.0);
        float sin = (float) Math.sin(radians);
        float cos = (float) Math.cos(radians);
        float sin2 = (float) Math.sin(radians * 2.0);
        float cos2 = (float) Math.cos(radians * 2.0);

        switch (this.clipType) {
            case IDLE -> {
                targetPose.bodyPitch += sin * 1.8f * this.weight;
                targetPose.bodyRoll += cos * 0.8f * this.weight;
                targetPose.headPitch += -sin * 1.2f * this.weight;
                targetPose.headRoll += cos * 1.0f * this.weight;
                targetPose.leftArmRoll += -sin * 2.2f * this.weight;
                targetPose.rightArmRoll += sin * 2.2f * this.weight;
                targetPose.leftArmPitch += cos * 1.2f * this.weight;
                targetPose.rightArmPitch += -cos * 1.2f * this.weight;
            }
            case WALK -> {
                targetPose.leftLegPitch += sin * 32.0f * this.weight;
                targetPose.rightLegPitch += -sin * 32.0f * this.weight;
                targetPose.leftArmPitch += -sin * 28.0f * this.weight;
                targetPose.rightArmPitch += sin * 28.0f * this.weight;
                targetPose.leftArmRoll += (-5.0f - Math.abs(sin) * 3.0f) * this.weight;
                targetPose.rightArmRoll += (5.0f + Math.abs(sin) * 3.0f) * this.weight;
                targetPose.bodyYaw += cos * 4.0f * this.weight;
                targetPose.bodyPitch += (3.0f + sin2 * 1.2f) * this.weight;
                targetPose.bodyRoll += sin * 1.8f * this.weight;
                targetPose.headYaw += -cos * 2.5f * this.weight;
                targetPose.headPitch += sin2 * 1.5f * this.weight;
            }
            case RUN -> {
                targetPose.bodyPitch += (20.0f + sin2 * 2.5f) * this.weight;
                targetPose.bodyYaw += cos * 7.0f * this.weight;
                targetPose.bodyRoll += sin * 3.0f * this.weight;
                targetPose.headPitch += (-18.0f + sin2 * 2.0f) * this.weight;
                targetPose.headYaw += -cos * 3.5f * this.weight;
                targetPose.leftLegPitch += sin * 58.0f * this.weight;
                targetPose.rightLegPitch += -sin * 58.0f * this.weight;
                targetPose.leftArmPitch += -sin * 65.0f * this.weight;
                targetPose.rightArmPitch += sin * 65.0f * this.weight;
                targetPose.leftArmRoll += -16.0f * this.weight;
                targetPose.rightArmRoll += 16.0f * this.weight;
            }
            case WAVE -> {
                targetPose.rightArmPitch += -145.0f * this.weight;
                targetPose.rightArmYaw += 15.0f * this.weight;
                targetPose.rightArmRoll += (20.0f + sin * 28.0f) * this.weight;
                targetPose.headYaw += -sin * 8.0f * this.weight;
                targetPose.headRoll += -sin * 4.0f * this.weight;
                targetPose.bodyPitch += sin2 * 1.0f * this.weight;
                targetPose.bodyRoll += sin * 2.5f * this.weight;
            }
            case ZOMBIE_WALK -> {
                targetPose.leftArmPitch += (-88.0f + sin * 6.0f) * this.weight;
                targetPose.rightArmPitch += (-92.0f - sin * 6.0f) * this.weight;
                targetPose.leftArmYaw += 5.0f * this.weight;
                targetPose.rightArmYaw += -5.0f * this.weight;
                targetPose.leftLegPitch += sin * 26.0f * this.weight;
                targetPose.rightLegPitch += -sin * 26.0f * this.weight;
                targetPose.bodyPitch += (10.0f + sin * 3.0f) * this.weight;
                targetPose.bodyRoll += sin * 5.5f * this.weight;
                targetPose.headPitch += 12.0f * this.weight;
                targetPose.headRoll += (8.0f + cos * 6.0f) * this.weight;
            }
            case ATTACK -> {
                float swing = cycleTime < 0.35f
                        ? -140.0f + (cycleTime / 0.35f) * 175.0f
                        : 35.0f - ((cycleTime - 0.35f) / 0.65f) * 175.0f;
                targetPose.rightArmPitch += swing * this.weight;
                targetPose.rightArmRoll += (15.0f + sin * 20.0f) * this.weight;
                targetPose.bodyYaw += (sin * 22.0f) * this.weight;
                targetPose.bodyPitch += (8.0f + sin2 * 4.0f) * this.weight;
                targetPose.leftArmPitch += (-sin * 25.0f) * this.weight;
            }
            case SNEAK_WALK -> {
                targetPose.bodyPitch += (28.0f + sin2 * 1.5f) * this.weight;
                targetPose.headPitch += -24.0f * this.weight;
                targetPose.leftLegPitch += sin * 22.0f * this.weight;
                targetPose.rightLegPitch += -sin * 22.0f * this.weight;
                targetPose.leftArmPitch += -sin * 18.0f * this.weight;
                targetPose.rightArmPitch += sin * 18.0f * this.weight;
                targetPose.leftArmRoll += -8.0f * this.weight;
                targetPose.rightArmRoll += 8.0f * this.weight;
            }
            case CHEER -> {
                targetPose.leftArmPitch += (-155.0f + sin * 22.0f) * this.weight;
                targetPose.rightArmPitch += (-155.0f + sin * 22.0f) * this.weight;
                targetPose.leftArmRoll += -30.0f * this.weight;
                targetPose.rightArmRoll += 30.0f * this.weight;
                targetPose.headPitch += (-15.0f + sin * 10.0f) * this.weight;
                targetPose.bodyPitch += (-sin * 6.0f) * this.weight;
            }
            case DANCE -> {
                targetPose.bodyRoll += sin * 12.0f * this.weight;
                targetPose.bodyYaw += cos * 15.0f * this.weight;
                targetPose.headRoll += -sin * 10.0f * this.weight;
                targetPose.headPitch += sin2 * 6.0f * this.weight;
                targetPose.leftArmPitch += (-50.0f + sin * 45.0f) * this.weight;
                targetPose.rightArmPitch += (-50.0f - sin * 45.0f) * this.weight;
                targetPose.leftArmRoll += (-20.0f + cos * 15.0f) * this.weight;
                targetPose.rightArmRoll += (20.0f + cos * 15.0f) * this.weight;
            }
            case SWIM -> {
                targetPose.bodyPitch += 72.0f * this.weight;
                targetPose.headPitch += -65.0f * this.weight;
                targetPose.leftArmPitch += (sin * 120.0f - 40.0f) * this.weight;
                targetPose.rightArmPitch += (-sin * 120.0f - 40.0f) * this.weight;
                targetPose.leftArmRoll += (-25.0f + cos * 20.0f) * this.weight;
                targetPose.rightArmRoll += (25.0f - cos * 20.0f) * this.weight;
                targetPose.leftLegPitch += (sin2 * 25.0f) * this.weight;
                targetPose.rightLegPitch += (-sin2 * 25.0f) * this.weight;
            }
            case SIT_IDLE -> {
                targetPose.leftLegPitch += -85.0f * this.weight;
                targetPose.rightLegPitch += -85.0f * this.weight;
                targetPose.leftLegYaw += -4.0f * this.weight;
                targetPose.rightLegYaw += 4.0f * this.weight;
                targetPose.bodyPitch += (3.0f + sin * 1.5f) * this.weight;
                targetPose.headPitch += (-sin * 1.2f) * this.weight;
                targetPose.leftArmPitch += (-15.0f + cos * 1.0f) * this.weight;
                targetPose.rightArmPitch += (-15.0f - cos * 1.0f) * this.weight;
            }
            case BOW_AIM -> {
                targetPose.bodyYaw += -45.0f * this.weight;
                targetPose.headYaw += 45.0f * this.weight;
                targetPose.leftArmPitch += (-85.0f + sin * 1.5f) * this.weight;
                targetPose.leftArmYaw += 40.0f * this.weight;
                targetPose.rightArmPitch += (-85.0f + cos * 1.5f) * this.weight;
                targetPose.rightArmYaw += -10.0f * this.weight;
                targetPose.rightArmRoll += 15.0f * this.weight;
            }
            case NONE -> {}
        }
    }

    public void bakeToKeyframes(FlashbackActor actor, int startTick, int endTick, int stepTicks, com.moulberry.flashback.keyframe.interpolation.InterpolationType interpolationType) {
        if (actor == null || this.clipType == ClipType.NONE || startTick >= endTick || stepTicks < 1) {
            return;
        }

        ActorPose tempPose = new ActorPose();
        for (int t = startTick; t <= endTick; t += stepTicks) {
            tempPose.setFrom(actor.getBasePose());
            this.applyToPose(tempPose, (float) t, true);

            for (ActorTrackType type : ActorTrackType.values()) {
                if (type.isBodyPose()) {
                    actor.setKeyframe(type, t, tempPose.getTrackValue(type), interpolationType);
                }
            }
        }
    }

    public ActorAnimationClip copy() {
        ActorAnimationClip copy = new ActorAnimationClip(this.name, this.clipType, this.lengthTicks);
        copy.loop = this.loop;
        copy.speed = this.speed;
        copy.startTickOffset = this.startTickOffset;
        copy.weight = this.weight;
        copy.realtimePreview = this.realtimePreview;
        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<ActorAnimationClip>, JsonDeserializer<ActorAnimationClip> {
        @Override
        public ActorAnimationClip deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            ActorAnimationClip clip = new ActorAnimationClip();
            if (obj.has("name")) clip.name = obj.get("name").getAsString();
            if (obj.has("clip_type")) clip.clipType = ClipType.valueOf(obj.get("clip_type").getAsString());
            if (obj.has("length_ticks")) clip.lengthTicks = obj.get("length_ticks").getAsInt();
            if (obj.has("loop")) clip.loop = obj.get("loop").getAsBoolean();
            if (obj.has("speed")) clip.speed = obj.get("speed").getAsFloat();
            if (obj.has("start_tick_offset")) clip.startTickOffset = obj.get("start_tick_offset").getAsInt();
            if (obj.has("weight")) clip.weight = obj.get("weight").getAsFloat();
            if (obj.has("realtime_preview")) clip.realtimePreview = obj.get("realtime_preview").getAsBoolean();
            return clip;
        }

        @Override
        public JsonElement serialize(ActorAnimationClip src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", src.name);
            obj.addProperty("clip_type", src.clipType.name());
            obj.addProperty("length_ticks", src.lengthTicks);
            obj.addProperty("loop", src.loop);
            obj.addProperty("speed", src.speed);
            obj.addProperty("start_tick_offset", src.startTickOffset);
            obj.addProperty("weight", src.weight);
            obj.addProperty("realtime_preview", src.realtimePreview);
            return obj;
        }
    }
}
