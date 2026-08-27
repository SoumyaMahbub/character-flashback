package com.moulberry.flashback.actor;

public enum ActorTrackType {
    WORLD_POS_X("Position X", Category.WORLD_TRANSFORM, 0.0f, -100000.0f, 100000.0f),
    WORLD_POS_Y("Position Y", Category.WORLD_TRANSFORM, 0.0f, -100000.0f, 100000.0f),
    WORLD_POS_Z("Position Z", Category.WORLD_TRANSFORM, 0.0f, -100000.0f, 100000.0f),

    WORLD_ROT_PITCH("Pitch", Category.WORLD_TRANSFORM, 0.0f, -180.0f, 180.0f),
    WORLD_ROT_YAW("Yaw", Category.WORLD_TRANSFORM, 0.0f, -360.0f, 360.0f),
    WORLD_ROT_ROLL("Roll", Category.WORLD_TRANSFORM, 0.0f, -180.0f, 180.0f),

    WORLD_SCALE_X("Scale X", Category.WORLD_TRANSFORM, 1.0f, 0.01f, 100.0f),
    WORLD_SCALE_Y("Scale Y", Category.WORLD_TRANSFORM, 1.0f, 0.01f, 100.0f),
    WORLD_SCALE_Z("Scale Z", Category.WORLD_TRANSFORM, 1.0f, 0.01f, 100.0f),

    HEAD_PITCH("Head Pitch", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    HEAD_YAW("Head Yaw", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    HEAD_ROLL("Head Roll", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),

    BODY_PITCH("Body Pitch", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    BODY_YAW("Body Yaw", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    BODY_ROLL("Body Roll", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),

    LEFT_ARM_PITCH("Left Arm Pitch", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    LEFT_ARM_YAW("Left Arm Yaw", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    LEFT_ARM_ROLL("Left Arm Roll", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),

    RIGHT_ARM_PITCH("Right Arm Pitch", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    RIGHT_ARM_YAW("Right Arm Yaw", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    RIGHT_ARM_ROLL("Right Arm Roll", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),

    LEFT_LEG_PITCH("Left Leg Pitch", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    LEFT_LEG_YAW("Left Leg Yaw", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    LEFT_LEG_ROLL("Left Leg Roll", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),

    RIGHT_LEG_PITCH("Right Leg Pitch", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    RIGHT_LEG_YAW("Right Leg Yaw", Category.BODY_POSE, 0.0f, -180.0f, 180.0f),
    RIGHT_LEG_ROLL("Right Leg Roll", Category.BODY_POSE, 0.0f, -180.0f, 180.0f);

    public enum Category {
        WORLD_TRANSFORM("World Transform"),
        BODY_POSE("Body Pose & Limbs");

        private final String displayName;

        Category(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    private final String displayName;
    private final Category category;
    private final float defaultValue;
    private final float minValue;
    private final float maxValue;

    ActorTrackType(String displayName, Category category, float defaultValue, float minValue, float maxValue) {
        this.displayName = displayName;
        this.category = category;
        this.defaultValue = defaultValue;
        this.minValue = minValue;
        this.maxValue = maxValue;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Category getCategory() {
        return category;
    }

    public float getDefaultValue() {
        return defaultValue;
    }

    public float getMinValue() {
        return minValue;
    }

    public float getMaxValue() {
        return maxValue;
    }

    public boolean isBodyPose() {
        return category == Category.BODY_POSE;
    }

    public boolean isWorldTransform() {
        return category == Category.WORLD_TRANSFORM;
    }
}
