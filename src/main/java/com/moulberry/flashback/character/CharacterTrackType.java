package com.moulberry.flashback.character;

public enum CharacterTrackType {
    // World Transform
    WORLD_POS_X("Position X", Category.WORLD_TRANSFORM, 0.0f, 0.1f),
    WORLD_POS_Y("Position Y", Category.WORLD_TRANSFORM, 0.0f, 0.1f),
    WORLD_POS_Z("Position Z", Category.WORLD_TRANSFORM, 0.0f, 0.1f),
    WORLD_ROT_PITCH("Pitch (X)", Category.WORLD_TRANSFORM, 0.0f, 1.0f),
    WORLD_ROT_YAW("Yaw (Y)", Category.WORLD_TRANSFORM, 0.0f, 1.0f),
    WORLD_ROT_ROLL("Roll (Z)", Category.WORLD_TRANSFORM, 0.0f, 1.0f),
    WORLD_SCALE_X("Scale X", Category.WORLD_TRANSFORM, 1.0f, 0.05f),
    WORLD_SCALE_Y("Scale Y", Category.WORLD_TRANSFORM, 1.0f, 0.05f),
    WORLD_SCALE_Z("Scale Z", Category.WORLD_TRANSFORM, 1.0f, 0.05f),

    // Head
    HEAD_PITCH("Head Pitch", Category.HEAD, 0.0f, 1.0f),
    HEAD_YAW("Head Yaw", Category.HEAD, 0.0f, 1.0f),
    HEAD_ROLL("Head Roll", Category.HEAD, 0.0f, 1.0f),

    // Body / Torso
    BODY_PITCH("Body Pitch", Category.BODY, 0.0f, 1.0f),
    BODY_YAW("Body Yaw", Category.BODY, 0.0f, 1.0f),
    BODY_ROLL("Body Roll", Category.BODY, 0.0f, 1.0f),

    // Left Arm
    LEFT_ARM_PITCH("Left Arm Pitch", Category.LEFT_ARM, 0.0f, 1.0f),
    LEFT_ARM_YAW("Left Arm Yaw", Category.LEFT_ARM, 0.0f, 1.0f),
    LEFT_ARM_ROLL("Left Arm Roll", Category.LEFT_ARM, 0.0f, 1.0f),

    // Right Arm
    RIGHT_ARM_PITCH("Right Arm Pitch", Category.RIGHT_ARM, 0.0f, 1.0f),
    RIGHT_ARM_YAW("Right Arm Yaw", Category.RIGHT_ARM, 0.0f, 1.0f),
    RIGHT_ARM_ROLL("Right Arm Roll", Category.RIGHT_ARM, 0.0f, 1.0f),

    // Left Leg
    LEFT_LEG_PITCH("Left Leg Pitch", Category.LEFT_LEG, 0.0f, 1.0f),
    LEFT_LEG_YAW("Left Leg Yaw", Category.LEFT_LEG, 0.0f, 1.0f),
    LEFT_LEG_ROLL("Left Leg Roll", Category.LEFT_LEG, 0.0f, 1.0f),

    // Right Leg
    RIGHT_LEG_PITCH("Right Leg Pitch", Category.RIGHT_LEG, 0.0f, 1.0f),
    RIGHT_LEG_YAW("Right Leg Yaw", Category.RIGHT_LEG, 0.0f, 1.0f),
    RIGHT_LEG_ROLL("Right Leg Roll", Category.RIGHT_LEG, 0.0f, 1.0f);

    public enum Category {
        WORLD_TRANSFORM("World Transform"),
        HEAD("Head"),
        BODY("Torso"),
        LEFT_ARM("Left Arm"),
        RIGHT_ARM("Right Arm"),
        LEFT_LEG("Left Leg"),
        RIGHT_LEG("Right Leg");

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
    private final float step;

    CharacterTrackType(String displayName, Category category, float defaultValue, float step) {
        this.displayName = displayName;
        this.category = category;
        this.defaultValue = defaultValue;
        this.step = step;
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

    public float getStep() {
        return step;
    }

    public boolean isWorldTransform() {
        return this.category == Category.WORLD_TRANSFORM;
    }

    public boolean isBodyPose() {
        return this.category != Category.WORLD_TRANSFORM;
    }
}
