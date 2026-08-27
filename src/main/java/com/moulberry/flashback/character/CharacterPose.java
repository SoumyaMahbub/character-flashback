package com.moulberry.flashback.character;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;

public class CharacterPose {

    public String name = "Default Pose";

    public float headPitch = 0.0f;
    public float headYaw = 0.0f;
    public float headRoll = 0.0f;

    public float bodyPitch = 0.0f;
    public float bodyYaw = 0.0f;
    public float bodyRoll = 0.0f;

    public float leftArmPitch = 0.0f;
    public float leftArmYaw = 0.0f;
    public float leftArmRoll = 0.0f;

    public float rightArmPitch = 0.0f;
    public float rightArmYaw = 0.0f;
    public float rightArmRoll = 0.0f;

    public float leftLegPitch = 0.0f;
    public float leftLegYaw = 0.0f;
    public float leftLegRoll = 0.0f;

    public float rightLegPitch = 0.0f;
    public float rightLegYaw = 0.0f;
    public float rightLegRoll = 0.0f;

    public CharacterPose() {}

    public CharacterPose(String name) {
        this.name = name;
    }

    public void reset() {
        this.headPitch = 0.0f;
        this.headYaw = 0.0f;
        this.headRoll = 0.0f;
        this.bodyPitch = 0.0f;
        this.bodyYaw = 0.0f;
        this.bodyRoll = 0.0f;
        this.leftArmPitch = 0.0f;
        this.leftArmYaw = 0.0f;
        this.leftArmRoll = 0.0f;
        this.rightArmPitch = 0.0f;
        this.rightArmYaw = 0.0f;
        this.rightArmRoll = 0.0f;
        this.leftLegPitch = 0.0f;
        this.leftLegYaw = 0.0f;
        this.leftLegRoll = 0.0f;
        this.rightLegPitch = 0.0f;
        this.rightLegYaw = 0.0f;
        this.rightLegRoll = 0.0f;
    }

    public float getTrackValue(CharacterTrackType trackType) {
        return switch (trackType) {
            case HEAD_PITCH -> headPitch;
            case HEAD_YAW -> headYaw;
            case HEAD_ROLL -> headRoll;
            case BODY_PITCH -> bodyPitch;
            case BODY_YAW -> bodyYaw;
            case BODY_ROLL -> bodyRoll;
            case LEFT_ARM_PITCH -> leftArmPitch;
            case LEFT_ARM_YAW -> leftArmYaw;
            case LEFT_ARM_ROLL -> leftArmRoll;
            case RIGHT_ARM_PITCH -> rightArmPitch;
            case RIGHT_ARM_YAW -> rightArmYaw;
            case RIGHT_ARM_ROLL -> rightArmRoll;
            case LEFT_LEG_PITCH -> leftLegPitch;
            case LEFT_LEG_YAW -> leftLegYaw;
            case LEFT_LEG_ROLL -> leftLegRoll;
            case RIGHT_LEG_PITCH -> rightLegPitch;
            case RIGHT_LEG_YAW -> rightLegYaw;
            case RIGHT_LEG_ROLL -> rightLegRoll;
            default -> 0.0f;
        };
    }

    public void setTrackValue(CharacterTrackType trackType, float value) {
        switch (trackType) {
            case HEAD_PITCH -> headPitch = value;
            case HEAD_YAW -> headYaw = value;
            case HEAD_ROLL -> headRoll = value;
            case BODY_PITCH -> bodyPitch = value;
            case BODY_YAW -> bodyYaw = value;
            case BODY_ROLL -> bodyRoll = value;
            case LEFT_ARM_PITCH -> leftArmPitch = value;
            case LEFT_ARM_YAW -> leftArmYaw = value;
            case LEFT_ARM_ROLL -> leftArmRoll = value;
            case RIGHT_ARM_PITCH -> rightArmPitch = value;
            case RIGHT_ARM_YAW -> rightArmYaw = value;
            case RIGHT_ARM_ROLL -> rightArmRoll = value;
            case LEFT_LEG_PITCH -> leftLegPitch = value;
            case LEFT_LEG_YAW -> leftLegYaw = value;
            case LEFT_LEG_ROLL -> leftLegRoll = value;
            case RIGHT_LEG_PITCH -> rightLegPitch = value;
            case RIGHT_LEG_YAW -> rightLegYaw = value;
            case RIGHT_LEG_ROLL -> rightLegRoll = value;
            default -> {}
        }
    }

    public CharacterPose copy() {
        CharacterPose copy = new CharacterPose(this.name);
        copy.setFrom(this);
        return copy;
    }

    public void setFrom(CharacterPose other) {
        this.name = other.name;
        this.headPitch = other.headPitch;
        this.headYaw = other.headYaw;
        this.headRoll = other.headRoll;
        this.bodyPitch = other.bodyPitch;
        this.bodyYaw = other.bodyYaw;
        this.bodyRoll = other.bodyRoll;
        this.leftArmPitch = other.leftArmPitch;
        this.leftArmYaw = other.leftArmYaw;
        this.leftArmRoll = other.leftArmRoll;
        this.rightArmPitch = other.rightArmPitch;
        this.rightArmYaw = other.rightArmYaw;
        this.rightArmRoll = other.rightArmRoll;
        this.leftLegPitch = other.leftLegPitch;
        this.leftLegYaw = other.leftLegYaw;
        this.leftLegRoll = other.leftLegRoll;
        this.rightLegPitch = other.rightLegPitch;
        this.rightLegYaw = other.rightLegYaw;
        this.rightLegRoll = other.rightLegRoll;
    }

    public static class TypeAdapter implements JsonSerializer<CharacterPose>, JsonDeserializer<CharacterPose> {
        @Override
        public CharacterPose deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            CharacterPose pose = new CharacterPose();
            if (obj.has("name")) pose.name = obj.get("name").getAsString();
            if (obj.has("headPitch")) pose.headPitch = obj.get("headPitch").getAsFloat();
            if (obj.has("headYaw")) pose.headYaw = obj.get("headYaw").getAsFloat();
            if (obj.has("headRoll")) pose.headRoll = obj.get("headRoll").getAsFloat();
            if (obj.has("bodyPitch")) pose.bodyPitch = obj.get("bodyPitch").getAsFloat();
            if (obj.has("bodyYaw")) pose.bodyYaw = obj.get("bodyYaw").getAsFloat();
            if (obj.has("bodyRoll")) pose.bodyRoll = obj.get("bodyRoll").getAsFloat();
            if (obj.has("leftArmPitch")) pose.leftArmPitch = obj.get("leftArmPitch").getAsFloat();
            if (obj.has("leftArmYaw")) pose.leftArmYaw = obj.get("leftArmYaw").getAsFloat();
            if (obj.has("leftArmRoll")) pose.leftArmRoll = obj.get("leftArmRoll").getAsFloat();
            if (obj.has("rightArmPitch")) pose.rightArmPitch = obj.get("rightArmPitch").getAsFloat();
            if (obj.has("rightArmYaw")) pose.rightArmYaw = obj.get("rightArmYaw").getAsFloat();
            if (obj.has("rightArmRoll")) pose.rightArmRoll = obj.get("rightArmRoll").getAsFloat();
            if (obj.has("leftLegPitch")) pose.leftLegPitch = obj.get("leftLegPitch").getAsFloat();
            if (obj.has("leftLegYaw")) pose.leftLegYaw = obj.get("leftLegYaw").getAsFloat();
            if (obj.has("leftLegRoll")) pose.leftLegRoll = obj.get("leftLegRoll").getAsFloat();
            if (obj.has("rightLegPitch")) pose.rightLegPitch = obj.get("rightLegPitch").getAsFloat();
            if (obj.has("rightLegYaw")) pose.rightLegYaw = obj.get("rightLegYaw").getAsFloat();
            if (obj.has("rightLegRoll")) pose.rightLegRoll = obj.get("rightLegRoll").getAsFloat();
            return pose;
        }

        @Override
        public JsonElement serialize(CharacterPose src, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", src.name);
            obj.addProperty("headPitch", src.headPitch);
            obj.addProperty("headYaw", src.headYaw);
            obj.addProperty("headRoll", src.headRoll);
            obj.addProperty("bodyPitch", src.bodyPitch);
            obj.addProperty("bodyYaw", src.bodyYaw);
            obj.addProperty("bodyRoll", src.bodyRoll);
            obj.addProperty("leftArmPitch", src.leftArmPitch);
            obj.addProperty("leftArmYaw", src.leftArmYaw);
            obj.addProperty("leftArmRoll", src.leftArmRoll);
            obj.addProperty("rightArmPitch", src.rightArmPitch);
            obj.addProperty("rightArmYaw", src.rightArmYaw);
            obj.addProperty("rightArmRoll", src.rightArmRoll);
            obj.addProperty("leftLegPitch", src.leftLegPitch);
            obj.addProperty("leftLegYaw", src.leftLegYaw);
            obj.addProperty("leftLegRoll", src.leftLegRoll);
            obj.addProperty("rightLegPitch", src.rightLegPitch);
            obj.addProperty("rightLegYaw", src.rightLegYaw);
            obj.addProperty("rightLegRoll", src.rightLegRoll);
            return obj;
        }
    }
}
