package com.moulberry.flashback.actor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ActorPosePresets {

    private static final List<ActorPose> PRESETS = new ArrayList<>();

    static {
        // 1. T-Pose
        ActorPose tPose = new ActorPose("T-Pose");
        tPose.leftArmRoll = -85.0f;
        tPose.rightArmRoll = 85.0f;
        PRESETS.add(tPose);

        // 2. Sitting
        ActorPose sitting = new ActorPose("Sitting");
        sitting.leftLegPitch = -90.0f;
        sitting.rightLegPitch = -90.0f;
        sitting.leftLegYaw = -5.0f;
        sitting.rightLegYaw = 5.0f;
        sitting.leftArmPitch = -20.0f;
        sitting.rightArmPitch = -20.0f;
        PRESETS.add(sitting);

        // 3. Zombie
        ActorPose zombie = new ActorPose("Zombie");
        zombie.leftArmPitch = -90.0f;
        zombie.rightArmPitch = -90.0f;
        zombie.headPitch = 10.0f;
        zombie.headRoll = 5.0f;
        PRESETS.add(zombie);

        // 4. Friendly Wave
        ActorPose wave = new ActorPose("Friendly Wave");
        wave.rightArmPitch = -150.0f;
        wave.rightArmRoll = 35.0f;
        wave.rightArmYaw = 15.0f;
        wave.headPitch = -5.0f;
        wave.headRoll = -8.0f;
        PRESETS.add(wave);

        // 5. Hero Landing
        ActorPose heroLanding = new ActorPose("Hero Landing");
        heroLanding.bodyPitch = 35.0f;
        heroLanding.headPitch = -25.0f;
        heroLanding.rightArmPitch = -70.0f;
        heroLanding.rightArmRoll = 20.0f;
        heroLanding.leftArmPitch = 40.0f;
        heroLanding.leftArmRoll = -30.0f;
        heroLanding.rightLegPitch = -60.0f;
        heroLanding.leftLegPitch = 40.0f;
        PRESETS.add(heroLanding);

        // 6. Dab
        ActorPose dab = new ActorPose("Dab");
        dab.headPitch = 30.0f;
        dab.headYaw = 40.0f;
        dab.bodyYaw = 15.0f;
        dab.leftArmPitch = -60.0f;
        dab.leftArmRoll = -85.0f;
        dab.leftArmYaw = 30.0f;
        dab.rightArmPitch = -50.0f;
        dab.rightArmRoll = -45.0f;
        dab.rightArmYaw = 40.0f;
        PRESETS.add(dab);

        // 7. Military Salute
        ActorPose salute = new ActorPose("Military Salute");
        salute.rightArmPitch = -135.0f;
        salute.rightArmYaw = -35.0f;
        salute.rightArmRoll = 55.0f;
        salute.headPitch = -5.0f;
        PRESETS.add(salute);

        // 8. Crossed Arms
        ActorPose crossedArms = new ActorPose("Crossed Arms");
        crossedArms.leftArmPitch = -40.0f;
        crossedArms.leftArmYaw = 45.0f;
        crossedArms.leftArmRoll = -20.0f;
        crossedArms.rightArmPitch = -45.0f;
        crossedArms.rightArmYaw = -45.0f;
        crossedArms.rightArmRoll = 20.0f;
        crossedArms.headPitch = 5.0f;
        PRESETS.add(crossedArms);

        // 9. Thinking / Pensive
        ActorPose thinking = new ActorPose("Thinking");
        thinking.rightArmPitch = -110.0f;
        thinking.rightArmYaw = -25.0f;
        thinking.rightArmRoll = 25.0f;
        thinking.leftArmPitch = -25.0f;
        thinking.leftArmYaw = 30.0f;
        thinking.headPitch = 12.0f;
        thinking.headRoll = 10.0f;
        PRESETS.add(thinking);

        // 10. Surrender / Hands Up
        ActorPose handsUp = new ActorPose("Surrender (Hands Up)");
        handsUp.leftArmPitch = -165.0f;
        handsUp.leftArmRoll = -20.0f;
        handsUp.rightArmPitch = -165.0f;
        handsUp.rightArmRoll = 20.0f;
        handsUp.headPitch = -15.0f;
        PRESETS.add(handsUp);

        // 11. Victory / Cheer
        ActorPose victory = new ActorPose("Victory V");
        victory.leftArmPitch = -140.0f;
        victory.leftArmRoll = -45.0f;
        victory.rightArmPitch = -140.0f;
        victory.rightArmRoll = 45.0f;
        victory.headPitch = -20.0f;
        PRESETS.add(victory);

        // 12. Bowing / Respect
        ActorPose bowing = new ActorPose("Bowing");
        bowing.bodyPitch = 45.0f;
        bowing.headPitch = 20.0f;
        bowing.leftArmPitch = 15.0f;
        bowing.rightArmPitch = 15.0f;
        PRESETS.add(bowing);

        // 13. Crouch Sneak
        ActorPose crouch = new ActorPose("Crouch Sneak");
        crouch.bodyPitch = 28.0f;
        crouch.headPitch = -20.0f;
        crouch.leftLegPitch = -15.0f;
        crouch.rightLegPitch = 15.0f;
        crouch.leftArmPitch = 15.0f;
        crouch.rightArmPitch = -15.0f;
        PRESETS.add(crouch);

        // 14. Reading Book
        ActorPose reading = new ActorPose("Reading Book");
        reading.headPitch = 28.0f;
        reading.leftArmPitch = -55.0f;
        reading.leftArmYaw = 25.0f;
        reading.rightArmPitch = -55.0f;
        reading.rightArmYaw = -25.0f;
        PRESETS.add(reading);

        // 15. Flexing Muscles
        ActorPose flex = new ActorPose("Flexing Muscles");
        flex.leftArmPitch = -90.0f;
        flex.leftArmRoll = -80.0f;
        flex.leftArmYaw = -30.0f;
        flex.rightArmPitch = -90.0f;
        flex.rightArmRoll = 80.0f;
        flex.rightArmYaw = 30.0f;
        flex.headPitch = -10.0f;
        PRESETS.add(flex);

        // 16. Pointing Forward
        ActorPose pointing = new ActorPose("Pointing Forward");
        pointing.rightArmPitch = -90.0f;
        pointing.rightArmYaw = 5.0f;
        pointing.headYaw = 5.0f;
        PRESETS.add(pointing);

        // 17. Sleeping Horizontal
        ActorPose sleeping = new ActorPose("Sleeping Horizontal");
        sleeping.bodyPitch = 90.0f;
        sleeping.headPitch = -10.0f;
        sleeping.leftArmPitch = -15.0f;
        sleeping.rightArmPitch = -15.0f;
        PRESETS.add(sleeping);

        // 18. Looking at Watch
        ActorPose watch = new ActorPose("Looking at Watch");
        watch.leftArmPitch = -85.0f;
        watch.leftArmYaw = 40.0f;
        watch.leftArmRoll = 10.0f;
        watch.headPitch = 25.0f;
        watch.headYaw = -20.0f;
        PRESETS.add(watch);

        // 19. Guard / Combat Stance
        ActorPose guard = new ActorPose("Combat Stance");
        guard.bodyYaw = -25.0f;
        guard.headYaw = 25.0f;
        guard.leftArmPitch = -45.0f;
        guard.leftArmYaw = 35.0f;
        guard.rightArmPitch = -65.0f;
        guard.rightArmYaw = -15.0f;
        guard.leftLegPitch = -20.0f;
        guard.rightLegPitch = 20.0f;
        PRESETS.add(guard);

        // 20. Levitation / Floating
        ActorPose floatPose = new ActorPose("Levitation Floating");
        floatPose.bodyPitch = -15.0f;
        floatPose.leftArmRoll = -65.0f;
        floatPose.rightArmRoll = 65.0f;
        floatPose.leftArmPitch = -20.0f;
        floatPose.rightArmPitch = -20.0f;
        floatPose.leftLegPitch = 25.0f;
        floatPose.rightLegPitch = 15.0f;
        floatPose.headPitch = -20.0f;
        PRESETS.add(floatPose);
    }

    public static List<ActorPose> getPresets() {
        return Collections.unmodifiableList(PRESETS);
    }
}
