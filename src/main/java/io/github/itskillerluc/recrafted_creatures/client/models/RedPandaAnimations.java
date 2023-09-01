package io.github.itskillerluc.recrafted_creatures.client.models;

import net.minecraft.client.animation.AnimationChannel;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.client.animation.Keyframe;
import net.minecraft.client.animation.KeyframeAnimations;

public class RedPandaAnimations {
    public static final AnimationDefinition RED_PANDA_SLEEP = AnimationDefinition.Builder.withLength(4f).looping()
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2f, KeyframeAnimations.posVec(0f, 0.13f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("body",
                    new AnimationChannel(AnimationChannel.Targets.SCALE,
                            new Keyframe(0f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2f, KeyframeAnimations.scaleVec(1f, 1.03f, 1f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.scaleVec(1f, 1f, 1f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("tail",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(-2.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2f, KeyframeAnimations.degreeVec(-5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.degreeVec(-2.5f, 0f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, -1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, -1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("head",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(9.85f, -1.73f, 9.85f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(2f, KeyframeAnimations.degreeVec(11.31f, -1.97f, 11.08f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.degreeVec(9.85f, -1.73f, 9.85f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg3",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, 1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg3",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(17.5f, 0f, -90f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.degreeVec(17.5f, 0f, -90f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg2",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, 1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg2",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, -90f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.degreeVec(0f, 0f, -90f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg1",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 1f, -0.02f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, 1f, -0.02f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg1",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(17.5f, 0f, 90f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.degreeVec(17.5f, 0f, 90f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg0",
                    new AnimationChannel(AnimationChannel.Targets.POSITION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, 1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.posVec(0f, 1f, 0f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("leg0",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.degreeVec(0f, 0f, 90f),
                                    AnimationChannel.Interpolations.CATMULLROM),
                            new Keyframe(4f, KeyframeAnimations.degreeVec(0f, 0f, 90f),
                                    AnimationChannel.Interpolations.CATMULLROM)))
            .addAnimation("root",
                    new AnimationChannel(AnimationChannel.Targets.ROTATION,
                            new Keyframe(0f, KeyframeAnimations.posVec(0f, -4f, 0f),
                                    AnimationChannel.Interpolations.LINEAR))).build();
}
