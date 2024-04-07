package io.github.itskillerluc.recrafted_creatures.item;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.block.ThornBlock;
import io.github.itskillerluc.recrafted_creatures.capability.ExtraTickProvider;
import io.github.itskillerluc.recrafted_creatures.client.renderers.ItemRenderer;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DustColorTransitionParticle;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.joml.Vector3f;

import java.util.function.Consumer;

public class JungleStaff extends Item {
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "jungle_staff");
    public static final ResourceLocation TEXTURE = new ResourceLocation(RecraftedCreatures.MODID, "textures/item/jungle_staff.png");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private static final int WALL_WIDTH = 4;
    private static final int WALL_HEIGHT = 4;

    public JungleStaff(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public void inventoryTick(ItemStack pStack, Level pLevel, Entity pEntity, int pSlotId, boolean pIsSelected) {
        if (pStack.getOrCreateTag().contains("use")) {
            var tag = pStack.getOrCreateTag().getInt("use");
            if (tag > 0) {
                pStack.getOrCreateTag().putInt("use", --tag);
                if (!pLevel.isClientSide()) {
                    ((ServerLevel) pLevel).sendParticles(new DustParticleOptions(new Vector3f(1, 1, 0), 0.2f), pEntity.getX(), pEntity.getY(), pEntity.getZ(), 80, 1, 2, 1, 0.1f);
                }
            } else {
                pStack.getOrCreateTag().remove("use");
                if (!pLevel.isClientSide()) {
                    pLevel.getCapability(ExtraTickProvider.EXTRA_TICK_CAP).ifPresent(cap -> {
                        cap.removeChunk(pLevel.getChunkAt(NbtUtils.readBlockPos(pStack.getOrCreateTag().getCompound("chunk"))));
                    });
                }
                pStack.getOrCreateTag().remove("chunk");
            }
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        pPlayer.getCooldowns().addCooldown(this, 1200);
        if (pPlayer.isShiftKeyDown()) {
            for (int i = 0; i < WALL_HEIGHT; i++) {
                for (int j = -(WALL_WIDTH / 2); j < WALL_WIDTH / 2; j++) {
                    var dir = pPlayer.getDirection();
                    var offset = pPlayer.blockPosition().relative(dir, 3);
                    var pos = offset.relative(dir.getClockWise(), j).above(i);
                    if (pLevel.getBlockState(pos).canBeReplaced()) {
                        if (i == WALL_HEIGHT - 1) {
                            pLevel.setBlock(pos, BlockRegistry.THORN_BLOCK.get().defaultBlockState().setValue(ThornBlock.TOP, true), 3);
                        } else {
                            pLevel.setBlock(pos, BlockRegistry.THORN_BLOCK.get().defaultBlockState().setValue(ThornBlock.TOP, false), 3);
                        }
                        pPlayer.getItemInHand(pUsedHand).getOrCreateTag().putInt("use", 200);
                        if (!pLevel.isClientSide()) {
                            ((ServerLevel) pLevel).sendParticles(new DustParticleOptions(new Vector3f(1, 1, 0), 0.2f), pos.getX(), pos.getY(), pos.getZ(), 100, pPlayer.getDirection().getAxis() == Direction.Axis.Z ? 0.5 : 0, 0.5, pPlayer.getDirection().getAxis() == Direction.Axis.X ? 0.5 : 0, 0.1f);
                        }
                    }
                }
            }
        } else {
            pPlayer.getItemInHand(pUsedHand).getOrCreateTag().putInt("use", 600);
            pPlayer.getItemInHand(pUsedHand).getOrCreateTag().put("chunk", NbtUtils.writeBlockPos(pPlayer.chunkPosition().getWorldPosition()));
            if (!pLevel.isClientSide()) {
                pLevel.getCapability(ExtraTickProvider.EXTRA_TICK_CAP).ifPresent(cap -> {
                    cap.addChunk(pLevel.getChunkAt(pPlayer.blockPosition()));
                });
            }
        }
        pPlayer.getItemInHand(pUsedHand).hurtAndBreak(1, pPlayer, (p_150686_) -> {
            p_150686_.broadcastBreakEvent(pUsedHand);
        });
        return InteractionResultHolder.success(pPlayer.getItemInHand(pUsedHand));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ItemRenderer.INSTANCE;
            }
        });
    }
}
