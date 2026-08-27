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
import org.joml.Vector3f;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class FlashbackActor {

    private final UUID id;
    private String name;
    private boolean visible = true;
    private ActorSkin skin = new ActorSkin();

    // Outer layer visibility
    private boolean hatVisible = true;
    private boolean jacketVisible = true;
    private boolean leftSleeveVisible = true;
    private boolean rightSleeveVisible = true;
    private boolean leftPantsVisible = true;
    private boolean rightPantsVisible = true;

    // Animation tracks
    private final Map<ActorTrackType, ActorAnimationTrack> tracks = new EnumMap<>(ActorTrackType.class);

    // Saved poses and active procedural clip
    private final List<ActorPose> savedPoses = new ArrayList<>();
    private ActorAnimationClip activeClip = new ActorAnimationClip("Active Clip", ActorAnimationClip.ClipType.NONE, 20);

    // Base user/keyframed pose (unclipped)
    private final ActorPose basePose = new ActorPose("Base");

    // Runtime evaluated state
    private transient ActorPose evaluatedPose = new ActorPose("Evaluated");
    private transient float evalPosX = 0.0f;
    private transient float evalPosY = 0.0f;
    private transient float evalPosZ = 0.0f;
    private transient float evalRotPitch = 0.0f;
    private transient float evalRotYaw = 0.0f;
    private transient float evalRotRoll = 0.0f;
    private transient float evalScaleX = 1.0f;
    private transient float evalScaleY = 1.0f;
    private transient float evalScaleZ = 1.0f;
    private transient float lastEvaluatedTick = Float.NaN;

    public FlashbackActor(UUID id, String name) {
        this.id = Objects.requireNonNull(id);
        this.name = Objects.requireNonNullElse(name, "Actor");
        this.initTracks();
    }

    private void initTracks() {
        for (ActorTrackType type : ActorTrackType.values()) {
            if (!this.tracks.containsKey(type)) {
                this.tracks.put(type, new ActorAnimationTrack(type));
            }
        }
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public ActorSkin getSkin() {
        return skin;
    }

    public void setSkin(ActorSkin skin) {
        this.skin = Objects.requireNonNullElseGet(skin, ActorSkin::new);
    }

    public boolean isHatVisible() {
        return hatVisible;
    }

    public void setHatVisible(boolean hatVisible) {
        this.hatVisible = hatVisible;
    }

    public boolean isJacketVisible() {
        return jacketVisible;
    }

    public void setJacketVisible(boolean jacketVisible) {
        this.jacketVisible = jacketVisible;
    }

    public boolean isLeftSleeveVisible() {
        return leftSleeveVisible;
    }

    public void setLeftSleeveVisible(boolean leftSleeveVisible) {
        this.leftSleeveVisible = leftSleeveVisible;
    }

    public boolean isRightSleeveVisible() {
        return rightSleeveVisible;
    }

    public void setRightSleeveVisible(boolean rightSleeveVisible) {
        this.rightSleeveVisible = rightSleeveVisible;
    }

    public boolean isLeftPantsVisible() {
        return leftPantsVisible;
    }

    public void setLeftPantsVisible(boolean leftPantsVisible) {
        this.leftPantsVisible = leftPantsVisible;
    }

    public boolean isRightPantsVisible() {
        return rightPantsVisible;
    }

    public void setRightPantsVisible(boolean rightPantsVisible) {
        this.rightPantsVisible = rightPantsVisible;
    }

    public Map<ActorTrackType, ActorAnimationTrack> getTracks() {
        return tracks;
    }

    public ActorAnimationTrack getTrack(ActorTrackType type) {
        return tracks.computeIfAbsent(type, ActorAnimationTrack::new);
    }

    public List<ActorPose> getSavedPoses() {
        return savedPoses;
    }

    public ActorAnimationClip getActiveClip() {
        return activeClip;
    }

    public void setActiveClip(ActorAnimationClip activeClip) {
        this.activeClip = Objects.requireNonNullElseGet(activeClip, ActorAnimationClip::new);
    }

    public ActorPose getBasePose() {
        return basePose;
    }

    public ActorPose getEvaluatedPose() {
        return evaluatedPose;
    }

    public void setPosition(Vector3f position) {
        this.evalPosX = position.x;
        this.evalPosY = position.y;
        this.evalPosZ = position.z;
    }

    public void setRotation(float pitch, float yaw, float roll) {
        this.evalRotPitch = pitch;
        this.evalRotYaw = yaw;
        this.evalRotRoll = roll;
    }

    public void setScale(float scaleX, float scaleY, float scaleZ) {
        this.evalScaleX = scaleX;
        this.evalScaleY = scaleY;
        this.evalScaleZ = scaleZ;
    }

    public float getEvalPosX() {
        return evalPosX;
    }

    public float getEvalPosY() {
        return evalPosY;
    }

    public float getEvalPosZ() {
        return evalPosZ;
    }

    public float getEvalRotPitch() {
        return evalRotPitch;
    }

    public float getEvalRotYaw() {
        return evalRotYaw;
    }

    public float getEvalRotRoll() {
        return evalRotRoll;
    }

    public float getEvalScaleX() {
        return evalScaleX;
    }

    public float getEvalScaleY() {
        return evalScaleY;
    }

    public float getEvalScaleZ() {
        return evalScaleZ;
    }

    public void evaluate(float tick, boolean isReplayPlaying) {
        this.lastEvaluatedTick = tick;

        // Evaluate World Transform
        if (!getTrack(ActorTrackType.WORLD_POS_X).isEmpty()) this.evalPosX = getTrack(ActorTrackType.WORLD_POS_X).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_POS_Y).isEmpty()) this.evalPosY = getTrack(ActorTrackType.WORLD_POS_Y).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_POS_Z).isEmpty()) this.evalPosZ = getTrack(ActorTrackType.WORLD_POS_Z).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_ROT_PITCH).isEmpty()) this.evalRotPitch = getTrack(ActorTrackType.WORLD_ROT_PITCH).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_ROT_YAW).isEmpty()) this.evalRotYaw = getTrack(ActorTrackType.WORLD_ROT_YAW).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_ROT_ROLL).isEmpty()) this.evalRotRoll = getTrack(ActorTrackType.WORLD_ROT_ROLL).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_SCALE_X).isEmpty()) this.evalScaleX = getTrack(ActorTrackType.WORLD_SCALE_X).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_SCALE_Y).isEmpty()) this.evalScaleY = getTrack(ActorTrackType.WORLD_SCALE_Y).evaluate(tick);
        if (!getTrack(ActorTrackType.WORLD_SCALE_Z).isEmpty()) this.evalScaleZ = getTrack(ActorTrackType.WORLD_SCALE_Z).evaluate(tick);

        // Evaluate Limbs from tracks into basePose
        for (ActorTrackType type : ActorTrackType.values()) {
            if (type.isBodyPose()) {
                ActorAnimationTrack track = getTrack(type);
                if (!track.isEmpty()) {
                    float val = track.evaluate(tick);
                    this.basePose.setTrackValue(type, val);
                }
            }
        }

        // Copy clean base pose into evaluated pose
        this.evaluatedPose.setFrom(this.basePose);

        // Apply active procedural animation clip if any
        if (this.activeClip != null) {
            this.activeClip.applyToPose(this.evaluatedPose, tick, isReplayPlaying);
        }
    }

    public void setKeyframe(ActorTrackType trackType, int tick, float value, InterpolationType interpolationType) {
        getTrack(trackType).setKeyframe(tick, value, interpolationType);
        if (trackType.isBodyPose()) {
            this.basePose.setTrackValue(trackType, value);
            this.evaluatedPose.setTrackValue(trackType, value);
        }
    }

    public void removeKeyframe(ActorTrackType trackType, int tick) {
        getTrack(trackType).removeKeyframe(tick);
    }

    public void insertAllTransformKeyframes(int tick, InterpolationType interpolationType) {
        setKeyframe(ActorTrackType.WORLD_POS_X, tick, this.evalPosX, interpolationType);
        setKeyframe(ActorTrackType.WORLD_POS_Y, tick, this.evalPosY, interpolationType);
        setKeyframe(ActorTrackType.WORLD_POS_Z, tick, this.evalPosZ, interpolationType);
        setKeyframe(ActorTrackType.WORLD_ROT_PITCH, tick, this.evalRotPitch, interpolationType);
        setKeyframe(ActorTrackType.WORLD_ROT_YAW, tick, this.evalRotYaw, interpolationType);
        setKeyframe(ActorTrackType.WORLD_ROT_ROLL, tick, this.evalRotRoll, interpolationType);
        setKeyframe(ActorTrackType.WORLD_SCALE_X, tick, this.evalScaleX, interpolationType);
        setKeyframe(ActorTrackType.WORLD_SCALE_Y, tick, this.evalScaleY, interpolationType);
        setKeyframe(ActorTrackType.WORLD_SCALE_Z, tick, this.evalScaleZ, interpolationType);
    }

    public void insertAllPoseKeyframes(int tick, InterpolationType interpolationType) {
        for (ActorTrackType type : ActorTrackType.values()) {
            if (type.isBodyPose()) {
                setKeyframe(type, tick, this.basePose.getTrackValue(type), interpolationType);
            }
        }
    }

    public void insertAllKeyframes(int tick, InterpolationType interpolationType) {
        insertAllTransformKeyframes(tick, interpolationType);
        insertAllPoseKeyframes(tick, interpolationType);
    }

    public void applyPose(ActorPose pose) {
        if (pose == null) return;
        this.basePose.setFrom(pose);
        this.evaluatedPose.setFrom(pose);
    }

    public void resetLimbs(int tick, InterpolationType interpolationType) {
        this.basePose.reset();
        this.evaluatedPose.reset();
        insertAllPoseKeyframes(tick, interpolationType);
    }

    public FlashbackActor duplicate(UUID newId, String newName) {
        FlashbackActor copy = new FlashbackActor(newId, newName);
        copy.visible = this.visible;
        copy.skin = this.skin.copy();
        copy.hatVisible = this.hatVisible;
        copy.jacketVisible = this.jacketVisible;
        copy.leftSleeveVisible = this.leftSleeveVisible;
        copy.rightSleeveVisible = this.rightSleeveVisible;
        copy.leftPantsVisible = this.leftPantsVisible;
        copy.rightPantsVisible = this.rightPantsVisible;

        for (Map.Entry<ActorTrackType, ActorAnimationTrack> entry : this.tracks.entrySet()) {
            copy.tracks.put(entry.getKey(), entry.getValue().copy());
        }

        for (ActorPose pose : this.savedPoses) {
            copy.savedPoses.add(pose.copy());
        }

        copy.activeClip = this.activeClip.copy();
        copy.basePose.setFrom(this.basePose);
        copy.evaluatedPose.setFrom(this.evaluatedPose);
        copy.evalPosX = this.evalPosX;
        copy.evalPosY = this.evalPosY;
        copy.evalPosZ = this.evalPosZ;
        copy.evalRotPitch = this.evalRotPitch;
        copy.evalRotYaw = this.evalRotYaw;
        copy.evalRotRoll = this.evalRotRoll;
        copy.evalScaleX = this.evalScaleX;
        copy.evalScaleY = this.evalScaleY;
        copy.evalScaleZ = this.evalScaleZ;

        return copy;
    }

    public static class TypeAdapter implements JsonSerializer<FlashbackActor>, JsonDeserializer<FlashbackActor> {
        @Override
        public FlashbackActor deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            UUID id = UUID.fromString(obj.get("id").getAsString());
            String name = obj.get("name").getAsString();
            FlashbackActor actor = new FlashbackActor(id, name);

            if (obj.has("visible")) actor.visible = obj.get("visible").getAsBoolean();
            if (obj.has("skin")) actor.skin = context.deserialize(obj.get("skin"), ActorSkin.class);
            if (obj.has("hatVisible")) actor.hatVisible = obj.get("hatVisible").getAsBoolean();
            if (obj.has("jacketVisible")) actor.jacketVisible = obj.get("jacketVisible").getAsBoolean();
            if (obj.has("leftSleeveVisible")) actor.leftSleeveVisible = obj.get("leftSleeveVisible").getAsBoolean();
            if (obj.has("rightSleeveVisible")) actor.rightSleeveVisible = obj.get("rightSleeveVisible").getAsBoolean();
            if (obj.has("leftPantsVisible")) actor.leftPantsVisible = obj.get("leftPantsVisible").getAsBoolean();
            if (obj.has("rightPantsVisible")) actor.rightPantsVisible = obj.get("rightPantsVisible").getAsBoolean();

            if (obj.has("tracks")) {
                JsonArray tracksArray = obj.getAsJsonArray("tracks");
                for (JsonElement trackElem : tracksArray) {
                    ActorAnimationTrack track = context.deserialize(trackElem, ActorAnimationTrack.class);
                    if (track != null) {
                        actor.tracks.put(track.getTrackType(), track);
                    }
                }
            }

            if (obj.has("savedPoses")) {
                JsonArray posesArray = obj.getAsJsonArray("savedPoses");
                for (JsonElement poseElem : posesArray) {
                    ActorPose pose = context.deserialize(poseElem, ActorPose.class);
                    if (pose != null) {
                        actor.savedPoses.add(pose);
                    }
                }
            }

            if (obj.has("activeClip")) {
                actor.activeClip = context.deserialize(obj.get("activeClip"), ActorAnimationClip.class);
            }

            if (obj.has("basePose")) {
                ActorPose bp = context.deserialize(obj.get("basePose"), ActorPose.class);
                if (bp != null) {
                    actor.basePose.setFrom(bp);
                }
            }

            return actor;
        }

        @Override
        public JsonElement serialize(FlashbackActor src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("id", src.id.toString());
            obj.addProperty("name", src.name);
            obj.addProperty("visible", src.visible);
            obj.add("skin", context.serialize(src.skin));
            obj.addProperty("hatVisible", src.hatVisible);
            obj.addProperty("jacketVisible", src.jacketVisible);
            obj.addProperty("leftSleeveVisible", src.leftSleeveVisible);
            obj.addProperty("rightSleeveVisible", src.rightSleeveVisible);
            obj.addProperty("leftPantsVisible", src.leftPantsVisible);
            obj.addProperty("rightPantsVisible", src.rightPantsVisible);

            JsonArray tracksArray = new JsonArray();
            for (ActorAnimationTrack track : src.tracks.values()) {
                tracksArray.add(context.serialize(track));
            }
            obj.add("tracks", tracksArray);

            JsonArray posesArray = new JsonArray();
            for (ActorPose pose : src.savedPoses) {
                posesArray.add(context.serialize(pose));
            }
            obj.add("savedPoses", posesArray);

            obj.add("activeClip", context.serialize(src.activeClip));
            obj.add("basePose", context.serialize(src.basePose));
            return obj;
        }
    }
}
