package com.moulberry.flashback.character;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CharacterPosePresets {

    private static final List<CharacterPose> PRESETS = new ArrayList<>();

    static {
        // 1. Standing
        CharacterPose standing = new CharacterPose("Standing (Default)");
        PRESETS.add(standing);

        // 2. T-Pose
        CharacterPose tPose = new CharacterPose("T-Pose");
        tPose.leftArmRoll = -90.0f;
        tPose.rightArmRoll = 90.0f;
        PRESETS.add(tPose);

        // 3. Walking Step A
        CharacterPose walkA = new CharacterPose("Walk (Step A)");
        walkA.leftLegPitch = 25.0f;
        walkA.rightLegPitch = -25.0f;
        walkA.leftArmPitch = 25.0f;
        walkA.rightArmPitch = -25.0f;
        PRESETS.add(walkA);

        // 4. Walking Step B
        CharacterPose walkB = new CharacterPose("Walk (Step B)");
        walkB.leftLegPitch = -25.0f;
        walkB.rightLegPitch = 25.0f;
        walkB.leftArmPitch = -25.0f;
        walkB.rightArmPitch = 25.0f;
        PRESETS.add(walkB);

        // 5. Running
        CharacterPose running = new CharacterPose("Running");
        running.bodyPitch = 18.0f;
        running.headPitch = -15.0f;
        running.leftLegPitch = 50.0f;
        running.rightLegPitch = -50.0f;
        running.leftArmPitch = 50.0f;
        running.rightArmPitch = -50.0f;
        running.leftArmRoll = -12.0f;
        running.rightArmRoll = 12.0f;
        PRESETS.add(running);

        // 6. Sitting
        CharacterPose sitting = new CharacterPose("Sitting");
        sitting.leftLegPitch = -85.0f;
        sitting.rightLegPitch = -85.0f;
        sitting.leftLegYaw = -4.0f;
        sitting.rightLegYaw = 4.0f;
        sitting.leftArmPitch = -15.0f;
        sitting.rightArmPitch = -15.0f;
        PRESETS.add(sitting);

        // 7. Waving
        CharacterPose waving = new CharacterPose("Waving");
        waving.rightArmPitch = -145.0f;
        waving.rightArmYaw = 15.0f;
        waving.rightArmRoll = 35.0f;
        waving.headYaw = -10.0f;
        waving.headRoll = -5.0f;
        PRESETS.add(waving);

        // 8. Sneaking
        CharacterPose sneaking = new CharacterPose("Sneaking");
        sneaking.bodyPitch = 28.0f;
        sneaking.headPitch = -24.0f;
        sneaking.leftArmPitch = 15.0f;
        sneaking.rightArmPitch = 15.0f;
        sneaking.leftArmRoll = -8.0f;
        sneaking.rightArmRoll = 8.0f;
        PRESETS.add(sneaking);

        // 9. Pointing
        CharacterPose pointing = new CharacterPose("Pointing");
        pointing.rightArmPitch = -88.0f;
        pointing.rightArmYaw = -10.0f;
        pointing.headYaw = 15.0f;
        pointing.headPitch = -5.0f;
        PRESETS.add(pointing);

        // 10. Zombie Arms
        CharacterPose zombie = new CharacterPose("Zombie Arms");
        zombie.leftArmPitch = -90.0f;
        zombie.rightArmPitch = -90.0f;
        zombie.headPitch = 8.0f;
        zombie.headRoll = 5.0f;
        PRESETS.add(zombie);

        // 11. Hands on Hips
        CharacterPose hero = new CharacterPose("Hands on Hips");
        hero.leftArmPitch = 10.0f;
        hero.leftArmRoll = -35.0f;
        hero.leftArmYaw = 15.0f;
        hero.rightArmPitch = 10.0f;
        hero.rightArmRoll = 35.0f;
        hero.rightArmYaw = -15.0f;
        hero.headPitch = -8.0f;
        PRESETS.add(hero);

        // 12. Hero Landing
        CharacterPose heroLanding = new CharacterPose("Hero Landing");
        heroLanding.bodyPitch = 45.0f;
        heroLanding.headPitch = -40.0f;
        heroLanding.leftLegPitch = 60.0f;
        heroLanding.rightLegPitch = -75.0f;
        heroLanding.leftArmPitch = -80.0f;
        heroLanding.rightArmPitch = 30.0f;
        heroLanding.rightArmRoll = 20.0f;
        PRESETS.add(heroLanding);

        // 13. Crossed Arms
        CharacterPose crossedArms = new CharacterPose("Crossed Arms");
        crossedArms.leftArmPitch = -40.0f;
        crossedArms.leftArmYaw = 45.0f;
        crossedArms.leftArmRoll = -10.0f;
        crossedArms.rightArmPitch = -40.0f;
        crossedArms.rightArmYaw = -45.0f;
        crossedArms.rightArmRoll = 10.0f;
        crossedArms.headPitch = -5.0f;
        PRESETS.add(crossedArms);

        // 14. Salute
        CharacterPose salute = new CharacterPose("Salute");
        salute.rightArmPitch = -130.0f;
        salute.rightArmYaw = 30.0f;
        salute.rightArmRoll = 40.0f;
        salute.headPitch = -5.0f;
        PRESETS.add(salute);

        // 15. Victory Cheer
        CharacterPose cheer = new CharacterPose("Victory Cheer");
        cheer.leftArmPitch = -150.0f;
        cheer.leftArmRoll = -25.0f;
        cheer.rightArmPitch = -150.0f;
        cheer.rightArmRoll = 25.0f;
        cheer.headPitch = -15.0f;
        PRESETS.add(cheer);

        // 16. Facepalm
        CharacterPose facepalm = new CharacterPose("Facepalm");
        facepalm.rightArmPitch = -115.0f;
        facepalm.rightArmYaw = -25.0f;
        facepalm.rightArmRoll = 15.0f;
        facepalm.headPitch = 15.0f;
        PRESETS.add(facepalm);

        // 17. Thinking
        CharacterPose thinking = new CharacterPose("Thinking");
        thinking.rightArmPitch = -85.0f;
        thinking.rightArmYaw = -35.0f;
        thinking.leftArmPitch = -20.0f;
        thinking.leftArmRoll = -25.0f;
        thinking.headPitch = -10.0f;
        thinking.headRoll = 10.0f;
        PRESETS.add(thinking);

        // 18. Dab
        CharacterPose dab = new CharacterPose("Dab");
        dab.headPitch = 25.0f;
        dab.headYaw = 40.0f;
        dab.leftArmPitch = -135.0f;
        dab.leftArmYaw = 35.0f;
        dab.leftArmRoll = -60.0f;
        dab.rightArmPitch = -110.0f;
        dab.rightArmYaw = 45.0f;
        dab.rightArmRoll = -45.0f;
        dab.bodyYaw = 15.0f;
        PRESETS.add(dab);

        // 19. Archery Aim
        CharacterPose bow = new CharacterPose("Archery Aim");
        bow.bodyYaw = -45.0f;
        bow.headYaw = 45.0f;
        bow.leftArmPitch = -85.0f;
        bow.leftArmYaw = 40.0f;
        bow.rightArmPitch = -85.0f;
        bow.rightArmYaw = -10.0f;
        bow.rightArmRoll = 15.0f;
        PRESETS.add(bow);

        // 20. Sleeping / Laying Down
        CharacterPose sleeping = new CharacterPose("Sleeping (Laying Down)");
        sleeping.bodyPitch = 90.0f;
        sleeping.headPitch = -90.0f;
        sleeping.leftArmPitch = 0.0f;
        sleeping.rightArmPitch = 0.0f;
        sleeping.leftLegPitch = 0.0f;
        sleeping.rightLegPitch = 0.0f;
        PRESETS.add(sleeping);
    }

    public static List<CharacterPose> getPresets() {
        return Collections.unmodifiableList(PRESETS);
    }

    public static CharacterPose getPreset(String name) {
        for (CharacterPose preset : PRESETS) {
            if (preset.name.equalsIgnoreCase(name)) {
                return preset.copy();
            }
        }
        return null;
    }
}
