package com.moulberry.flashback.actor;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.RemotePlayer;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.PlayerModelPart;

public class ActorPlayer extends RemotePlayer {

    private final FlashbackActor actor;

    public ActorPlayer(ClientLevel level, FlashbackActor actor) {
        super(level, new GameProfile(actor.getId(), actor.getName()));
        this.actor = actor;
        this.noPhysics = true;
    }

    public FlashbackActor getActor() {
        return actor;
    }

    @Override
    public PlayerSkin getSkin() {
        if (actor != null && actor.getSkin() != null) {
            return actor.getSkin().getOrLoadSkin();
        }
        return super.getSkin();
    }

    @Override
    public boolean isModelPartShown(PlayerModelPart part) {
        if (actor == null) return super.isModelPartShown(part);
        return switch (part) {
            case CAPE -> true;
            case JACKET -> actor.isJacketVisible();
            case LEFT_SLEEVE -> actor.isLeftSleeveVisible();
            case RIGHT_SLEEVE -> actor.isRightSleeveVisible();
            case LEFT_PANTS_LEG -> actor.isLeftPantsVisible();
            case RIGHT_PANTS_LEG -> actor.isRightPantsVisible();
            case HAT -> actor.isHatVisible();
        };
    }

    public void updateFromActor(float tick, boolean isPlaying) {
        if (actor == null) return;

        actor.evaluate(tick, isPlaying);

        double x = actor.getEvalPosX();
        double y = actor.getEvalPosY();
        double z = actor.getEvalPosZ();
        float yaw = actor.getEvalRotYaw();
        float pitch = actor.getEvalRotPitch();

        this.setPos(x, y, z);
        this.setXRot(pitch);
        this.setYRot(yaw);
        this.setYHeadRot(yaw + actor.getEvaluatedPose().headYaw);
        this.setYBodyRot(yaw + actor.getEvaluatedPose().bodyYaw);

        this.xOld = x;
        this.yOld = y;
        this.zOld = z;
        this.xRotO = pitch;
        this.yRotO = yaw;
        this.yHeadRotO = this.getYHeadRot();
        this.yBodyRotO = this.yBodyRot;

        // Clip type specific postures
        ActorAnimationClip clip = actor.getActiveClip();
        if (clip != null && clip.getClipType() != null) {
            switch (clip.getClipType()) {
                case SNEAK_WALK -> {
                    this.setPose(Pose.CROUCHING);
                    this.setShiftKeyDown(true);
                }
                case SWIM -> {
                    this.setPose(Pose.SWIMMING);
                }
                case SIT_IDLE -> {
                    this.setPose(Pose.SITTING);
                }
                default -> {
                    this.setPose(Pose.STANDING);
                    this.setShiftKeyDown(false);
                }
            }
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isSpectator() {
        return false;
    }

    @Override
    public boolean isCreative() {
        return false;
    }
}
