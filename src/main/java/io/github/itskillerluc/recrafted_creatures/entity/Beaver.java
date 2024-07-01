package io.github.itskillerluc.recrafted_creatures.entity;

import io.github.itskillerluc.duclib.client.animation.DucAnimation;
import io.github.itskillerluc.duclib.entity.Animatable;
import io.github.itskillerluc.recrafted_creatures.RecraftedCreatures;
import io.github.itskillerluc.recrafted_creatures.advancement.OwlDeliveryTrigger;
import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.client.models.BeaverModel;
import io.github.itskillerluc.recrafted_creatures.client.models.RedPandaModel;
import io.github.itskillerluc.recrafted_creatures.entity.ai.FoodSearching;
import io.github.itskillerluc.recrafted_creatures.entity.ai.MoveToFoodGoal;
import io.github.itskillerluc.recrafted_creatures.menu.BeaverMenu;
import io.github.itskillerluc.recrafted_creatures.registries.EntityRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.ItemRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.SoundRegistry;
import io.github.itskillerluc.recrafted_creatures.registries.Tags;
import io.github.itskillerluc.recrafted_creatures.util.StreamUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.*;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraftforge.common.util.Lazy;
import net.minecraftforge.network.NetworkHooks;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class Beaver extends TamableRCMob implements Animatable<BeaverModel>, MenuProvider, FoodSearching {
    private static final EntityDataSerializer<BeaverVariant> BEAVER_VARIANT_SERIALIZER = EntityDataSerializer.simpleEnum(BeaverVariant.class);
    private static final EntityDataSerializer<Mirror> MIRROR_SERIALIZER = EntityDataSerializer.simpleEnum(Mirror.class);
    private static final EntityDataSerializer<Rotation> ROTATION_SERIALIZER = EntityDataSerializer.simpleEnum(Rotation.class);
    private static final EntityDataAccessor<Boolean> IS_BUILDING = SynchedEntityData.defineId(Beaver.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Mirror> MIRROR = SynchedEntityData.defineId(Beaver.class, MIRROR_SERIALIZER);
    public static final EntityDataAccessor<Rotation> ROTATION = SynchedEntityData.defineId(Beaver.class, ROTATION_SERIALIZER);
    public static final EntityDataAccessor<BeaverVariant> VARIANT = SynchedEntityData.defineId(Beaver.class, BEAVER_VARIANT_SERIALIZER);
    public static final EntityDataAccessor<String> BUILD_NAME = SynchedEntityData.defineId(Beaver.class, EntityDataSerializers.STRING);
    public static CompletableFuture<List<ResourceLocation>> structures = CompletableFuture.completedFuture(List.of());
    public static CountDownLatch canReload = new CountDownLatch(1);
    public static final ResourceLocation LOCATION = new ResourceLocation(RecraftedCreatures.MODID, "beaver");
    public static final DucAnimation ANIMATION = DucAnimation.create(LOCATION);
    private static final int MAX_TRADE_TIME = 40;
    private static final int BUILD_TIME = 1200;
    private ItemEntity itemTarget;
    static final ResourceLocation LOOT = new ResourceLocation(RecraftedCreatures.MODID, "gameplay/beaver_barter");

    private final Lazy<Map<String, AnimationState>> animations = Lazy.of(() -> RedPandaModel.createStateMap(getAnimation()));
    private int pickupCooldown;
    private int tradeTimer;
    private int buildTimer;
    public boolean show = false;
    public BlockPos possibleBuildPos = null;
    public boolean shouldBuild = false;
    public BlockPos buildPos = null;
    public SimpleContainer inventory = new SimpleContainer(1);
    public List<ItemStack> materials = new ArrayList<>();
    public List<ItemStack> requiredMaterials = new ArrayList<>();

    public Beaver(EntityType<? extends TamableAnimal> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        moveControl = new BeaverMoveControl();
        setCanPickUpLoot(true);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new BeaverMenu(pContainerId, pPlayerInventory, List.of(), this, List.of());
    }

    @Override
    public ItemEntity getItemTarget() {
        return itemTarget;
    }

    @Override
    public void setItemTarget(ItemEntity target) {
        itemTarget = target;
    }

    class BeaverMoveControl extends MoveControl {

        public BeaverMoveControl() {
            super(Beaver.this);
        }

        @Override
        public void tick() {
            if (getMainHandItem().isEmpty() && !entityData.get(IS_BUILDING)) {
                super.tick();
            }
        }
    }


    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("variant", entityData.get(VARIANT).ordinal());
        pCompound.putInt("mirror", entityData.get(MIRROR).ordinal());
        pCompound.putInt("rotation", entityData.get(ROTATION).ordinal());
        pCompound.putString("buildName", entityData.get(BUILD_NAME));
        pCompound.putBoolean("isBuilding", entityData.get(IS_BUILDING));
        if (possibleBuildPos != null) {
            pCompound.put("possiblePos", NbtUtils.writeBlockPos(possibleBuildPos));
        }
        if (buildPos != null) {
            pCompound.put("buildPos", NbtUtils.writeBlockPos(buildPos));
        }
        pCompound.putBoolean("shouldBuild", shouldBuild);
        var listTag = new ListTag();
        for (ItemStack material : materials) {
            listTag.add(material.save(new CompoundTag()));
        }
        pCompound.put("inventory", listTag);

        var listTag2 = new ListTag();
        for (ItemStack material : requiredMaterials) {
            listTag2.add(material.save(new CompoundTag()));
        }
        pCompound.put("requiredMaterials", listTag2);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        entityData.set(VARIANT, BeaverVariant.values()[pCompound.getInt("variant")]);
        entityData.set(MIRROR, Mirror.values()[pCompound.getInt("mirror")]);
        entityData.set(ROTATION, Rotation.values()[pCompound.getInt("rotation")]);
        entityData.set(BUILD_NAME, pCompound.getString("buildName"));
        entityData.set(IS_BUILDING, pCompound.getBoolean("isBuilding"));
        if (pCompound.contains("possiblePos")) {
            possibleBuildPos = NbtUtils.readBlockPos(pCompound.getCompound("possiblePos"));
        } else {
            possibleBuildPos = null;
        }
        if (pCompound.contains("buildPos")) {
            buildPos = NbtUtils.readBlockPos(pCompound.getCompound("buildPos"));
        } else {
            buildPos = null;
        }
        shouldBuild = pCompound.getBoolean("shouldBuild");
        var listTag = pCompound.getList("inventory", Tag.TAG_COMPOUND);
        materials = new ArrayList<>();
        for (int i = 0; i < listTag.size(); i++) {
            CompoundTag tag = listTag.getCompound(i);
            materials.add(ItemStack.of(tag));
        }
        var listTag2 = pCompound.getList("requiredMaterials", Tag.TAG_COMPOUND);
        requiredMaterials = new ArrayList<>();
        for (int i = 0; i < listTag2.size(); i++) {
            CompoundTag tag = listTag2.getCompound(i);
            requiredMaterials.add(ItemStack.of(tag));
        }
    }

    public static AttributeSupplier.Builder attributes() {
        return TamableAnimal.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 14)
                .add(Attributes.MOVEMENT_SPEED, 0.2D);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(VARIANT, BeaverVariant.LIGHT);
        entityData.define(IS_BUILDING, false);
        entityData.define(MIRROR, Mirror.NONE);
        entityData.define(BUILD_NAME, "");
        entityData.define(ROTATION, Rotation.NONE);
    }

    @Override
    public boolean canTakeItem(ItemStack pItemstack) {
        if (pickupCooldown > 0 || buildPos != null) return false;
        EquipmentSlot equipmentSlot = Mob.getEquipmentSlotForItem(pItemstack);
        if (!this.getItemBySlot(equipmentSlot).isEmpty()) {
            return false;
        } else {
            return equipmentSlot == EquipmentSlot.MAINHAND && super.canTakeItem(pItemstack);
        }
    }

    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return pickupCooldown <= 0 && super.wantsToPickUp(pStack);
    }

    @Override
    public boolean canHoldItem(ItemStack pStack) {
        return true;
    }

    @Override
    protected void pickUpItem(ItemEntity pItemEntity) {
        if (this.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty()) {
            level().broadcastEntityEvent(this, (byte) 1);
            this.onItemPickup(pItemEntity);
            ItemStack itemstack = removeOneItemFromItemEntity(pItemEntity);
            this.setItemSlot(EquipmentSlot.MAINHAND, itemstack);
            this.setGuaranteedDrop(EquipmentSlot.MAINHAND);
            this.take(pItemEntity, itemstack.getCount());
        }
    }

    private static ItemStack removeOneItemFromItemEntity(ItemEntity pItemEntity) {
        ItemStack itemstack = pItemEntity.getItem();
        ItemStack itemstack1 = itemstack.split(1);
        if (itemstack.isEmpty()) {
            pItemEntity.discard();
        } else {
            pItemEntity.setItem(itemstack);
        }

        return itemstack1;
    }

    @Override
    public void handleEntityEvent(byte pId) {
        super.handleEntityEvent(pId);
        if (pId == 1) {
            playAnimation("observing_trade");
        }
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new BeaverGoal(new FloatGoal(this)));
        this.goalSelector.addGoal(2, new BeaverGoal(new SitWhenOrderedToGoal(this)));
        this.goalSelector.addGoal(3, new MoveToFoodGoal<>(this, 1, 5, item -> item.getItem().is(Tags.BEAVER_BARTERING)));
        this.goalSelector.addGoal(3, new BeaverGoal(new FollowOwnerGoal(this, 1.4D, 10.0F, 2.0F, false) {
            @Override
            public boolean canUse() {
                return super.canUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && entityData.get(COMMAND) == Command.FOLLOWING;
            }
        }));
        this.goalSelector.addGoal(7, new BeaverGoal(new BreedGoal(this, 1.0D)));
        this.goalSelector.addGoal(8, new BeaverGoal(new RandomStrollGoal(this, 1.0D)));
        this.goalSelector.addGoal(10, new BeaverGoal(new LookAtPlayerGoal(this, Player.class, 5)));
        this.goalSelector.addGoal(10, new BeaverGoal(new RandomLookAroundGoal(this)));
        this.goalSelector.addGoal(1, new MoveToBlockGoal(this, 1, 20) {

            @Override
            protected int nextStartTick(PathfinderMob pCreature) {
                return 20;
            }

            @Override
            public boolean canUse() {
                return super.canUse() && buildPos != null && shouldBuild;
            }

            @Override
            public boolean canContinueToUse() {
                return super.canContinueToUse() && buildPos != null && shouldBuild;
            }

            @Override
            protected boolean isValidTarget(LevelReader pLevel, BlockPos pPos) {
                if (buildPos == null || entityData.get(BUILD_NAME).isEmpty()) return false;
                var distanceSqr = pPos.distSqr(buildPos);
                if (distanceSqr < 25) {
                    var structure = ((ServerLevel) level()).getStructureManager().get(new ResourceLocation(entityData.get(BUILD_NAME)));
                    if (structure.isEmpty()) return false;
                    BoundingBox bb = structure.get().getBoundingBox(buildPos, entityData.get(ROTATION), new BlockPos(structure.get().getSize().getX() / 2, 0, structure.get().getSize().getZ() / 2), entityData.get(MIRROR));
                    return !bb.isInside(pPos);
                }
                return false;
            }

            @Override
            public void tick() {
                super.tick();
                if (isReachedTarget()) {
                    entityData.set(IS_BUILDING, true);
                    if (show && level().getBlockEntity(buildPos) instanceof ConstructorBlockEntity BE) {
                        BE.setShowBoundingBox(false);
                        show = false;
                        level().sendBlockUpdated(buildPos, level().getBlockState(buildPos), level().getBlockState(buildPos), 3);
                    }
                }
            }
        });
    }

    private class BeaverGoal extends Goal {
        Goal goal;

        public BeaverGoal(Goal goal) {
            this.goal = goal;
        }

        @Override
        public boolean canUse() {
            return Beaver.this.getMainHandItem().isEmpty() && buildPos == null && goal.canUse();
        }

        @Override
        public boolean canContinueToUse() {
            return goal.canContinueToUse();
        }

        @Override
        public boolean isInterruptable() {
            return goal.isInterruptable();
        }

        @Override
        public void start() {
            goal.start();
        }

        @Override
        public void stop() {
            goal.stop();
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return goal.requiresUpdateEveryTick();
        }

        @Override
        public void tick() {
            goal.tick();
        }

        @Override
        public void setFlags(EnumSet<Flag> pFlagSet) {
            goal.setFlags(pFlagSet);
        }

        @Override
        public String toString() {
            return goal.toString();
        }

        @Override
        public EnumSet<Flag> getFlags() {
            return goal.getFlags();
        }
    }

    @Override
    public ResourceLocation getModelLocation() {
        return LOCATION;
    }

    @Override
    public DucAnimation getAnimation() {
        return ANIMATION;
    }

    @Override
    public Lazy<Map<String, AnimationState>> getAnimations() {
        return animations;
    }

    @Override
    public Optional<AnimationState> getAnimationState(String animation) {
        return Optional.ofNullable(getAnimations().get().get("animation.beaver." + animation));
    }

    @Override
    public int tickCount() {
        return tickCount;
    }

    @Nullable
    @Override
    public AgeableMob getBreedOffspring(@NotNull ServerLevel pLevel, @NotNull AgeableMob pOtherParent) {
        return EntityRegistry.BEAVER.get().create(pLevel);
    }


    @Override
    public @NotNull InteractionResult mobInteract(@NotNull Player pPlayer, @NotNull InteractionHand pHand) {
        if (getOwnerUUID() != null && this.getOwnerUUID().compareTo(pPlayer.getUUID()) == 0) {
            if (pPlayer.getItemInHand(pHand).is(Items.GOLDEN_CARROT) && this.getHealth() < this.getMaxHealth()) {
                if (!pPlayer.getAbilities().instabuild) {
                    pPlayer.getItemInHand(pHand).shrink(1);
                }
                this.heal(3);
                return InteractionResult.SUCCESS;
            } else {
                if (pPlayer.getItemInHand(pHand).is(Items.GOLDEN_CARROT) && this.getAge() == 0 && !this.isInLove()) {
                    this.setInLove(pPlayer);
                    pPlayer.getItemInHand(pHand).shrink(1);
                    return InteractionResult.SUCCESS;
                } else if (!pPlayer.isShiftKeyDown()) {
                    if (!level().isClientSide()) {
                        NetworkHooks.openScreen((ServerPlayer) pPlayer, this, buf -> {
                            buf.writeCollection(structures.join(), FriendlyByteBuf::writeResourceLocation);
                            buf.writeInt(getId());
                            buf.writeCollection(requiredMaterials.stream()
                                    .map(material -> {
                                        var count = Math.max(0, material.getCount() - materials.stream().filter(item -> item.is(material.getItem())).findFirst().orElse(new ItemStack(Items.AIR, 0)).getCount());
                                        return material.copyWithCount(count);
                                    })
                                    .filter(material -> material.getCount() > 0)
                                    .toList(), (buffer, item) -> buffer.writeItemStack(item, true));
                        });
                    }
                    return InteractionResult.SUCCESS;
                }
                return super.mobInteract(pPlayer, pHand);
            }
        } else if (this.getOwner() == null && !level().isClientSide() && (pPlayer.getItemInHand(pHand).is(Items.GOLDEN_CARROT) || pPlayer.getItemInHand(pHand).is(ItemRegistry.APPLE_SLICE.get()))) {
            if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, pPlayer)) {
                this.tame(pPlayer);
                this.navigation.stop();
                this.setTarget(null);
                this.level().broadcastEntityEvent(this, (byte) 7);
                pPlayer.getItemInHand(pHand).shrink(1);
            } else {
                this.level().broadcastEntityEvent(this, (byte) 6);
            }

            return InteractionResult.SUCCESS;
        }
        if (pPlayer.isShiftKeyDown()) {
            return super.mobInteract(pPlayer, pHand);
        } else {
            return InteractionResult.FAIL;
        }
    }

    @Override
    public void tick() {
        super.tick();
        if (!inventory.isEmpty()) {
            var bool = true;
            for (ItemStack material : materials) {
                if (material.is(inventory.getItem(0).getItem())) {
                    material.setCount(material.getCount() + inventory.getItem(0).getCount());
                    inventory.removeAllItems();
                    bool = false;
                    break;
                }
            }
            if (bool) {
                materials.add(inventory.getItem(0));
                inventory.removeAllItems();
            }
        }
        if (!getMainHandItem().isEmpty()) {
            navigation.stop();
            if (tradeTimer++ > (level().isClientSide() ? MAX_TRADE_TIME - 1 : MAX_TRADE_TIME)) {
                pickupCooldown = 200;
                tradeTimer = 0;
                if (getMainHandItem().is(Tags.BEAVER_BARTERING)) {
                    if (level().isClientSide()) {
                        playAnimation("trade_likes");
                    } else {
                        getMainHandItem().shrink(1);
                        LootTable loottable = level().getServer().getLootData().getLootTable(LOOT);
                        List<ItemStack> list = loottable.getRandomItems((new LootParams.Builder((ServerLevel) level())).withParameter(LootContextParams.THIS_ENTITY, Beaver.this).create(LootContextParamSets.PIGLIN_BARTER));
                        for (ItemStack itemStack : list) {
                            spawnAtLocation(itemStack);
                        }
                    }
                } else {
                    if (level().isClientSide()) {
                        playAnimation("trade_dislikes");
                    } else {
                        spawnAtLocation(getMainHandItem());
                        setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                    }
                }
            }
        } else if (entityData.get(IS_BUILDING) && !level().isClientSide()) {
            navigation.stop();
            var structure = ((ServerLevel) level()).getStructureManager().get(new ResourceLocation(entityData.get(BUILD_NAME)));
            if (structure.isPresent()) {
                BoundingBox bb = structure.get().getBoundingBox(buildPos.offset(1, 1, 1), entityData.get(ROTATION), new BlockPos(structure.get().getSize().getX() / 2, 0, structure.get().getSize().getZ() / 2), entityData.get(MIRROR));
                if (buildTimer++ > BUILD_TIME) {
                    structure.get().placeInWorld(((ServerLevel) level()), buildPos.offset(1, 1, 1), buildPos, new StructurePlaceSettings().setMirror(entityData.get(MIRROR)).setRotation(entityData.get(ROTATION)).setRandom(level().getRandom()).setRotationPivot(new BlockPos(structure.get().getSize().getX(), 0, structure.get().getSize().getZ() / 2)), level().getRandom(), 2);
                    entityData.set(IS_BUILDING, false);
                    shouldBuild = false;
                    buildTimer = 0;
                    for (ItemStack requiredMaterial : requiredMaterials) {
                        materials.stream().filter(item -> item.is(requiredMaterial.getItem())).findFirst().ifPresent(item -> {
                            item.setCount(item.getCount() - requiredMaterial.getCount());
                        });
                    }
                    materials.removeIf(item -> item.getCount() <= 0);

                } else {
                    ((ServerLevel) level()).sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, bb.getCenter().getCenter().x(), bb.getCenter().getCenter().y(), bb.getCenter().getCenter().z(), 50, bb.getXSpan() / 4f, bb.getYSpan() / 4f, bb.getZSpan() / 4f, 0);
                }
            }
        } else if ((buildPos == null || !buildPos.equals(possibleBuildPos)) && possibleBuildPos != null && !entityData.get(BUILD_NAME).isEmpty()) {
            buildPos = possibleBuildPos;
        }
        if (buildPos != null && show && !shouldBuild && !entityData.get(IS_BUILDING) && !entityData.get(BUILD_NAME).isEmpty()) {
            if (!level().isClientSide()) {
                show = false;
                if (level().getBlockEntity(buildPos) instanceof ConstructorBlockEntity BE) {
                    BE.setShowBoundingBox(true);
                    BE.setStructureName(entityData.get(BUILD_NAME));
                    var structure = ((ServerLevel) level()).getStructureManager().get(new ResourceLocation(entityData.get(BUILD_NAME)));
                    if (structure.isPresent()) {
                        BoundingBox bb = structure.get().getBoundingBox(buildPos.offset(1, 1, 1), entityData.get(ROTATION), new BlockPos(structure.get().getSize().getX() / 2, 0, structure.get().getSize().getZ() / 2), entityData.get(MIRROR));
                        var blockpos = buildPos;
                        if (blockpos.getX() == bb.minX()) {
                            if (blockpos.getY() == bb.minY()) {
                                if (blockpos.getZ() == bb.minZ()) {
                                    BE.setCorner(new BlockPos(bb.maxX(), bb.maxY(), bb.maxZ()));
                                } else {
                                    BE.setCorner(new BlockPos(bb.maxX(), bb.maxY(), bb.minZ()));
                                }
                            } else {
                                if (blockpos.getZ() == bb.minZ()) {
                                    BE.setCorner(new BlockPos(bb.maxX(), bb.minY(), bb.maxZ()));
                                } else {
                                    BE.setCorner(new BlockPos(bb.maxX(), bb.minY(), bb.minZ()));
                                }
                            }
                        } else {
                            if (blockpos.getY() == bb.minY()) {
                                if (blockpos.getZ() == bb.minZ()) {
                                    BE.setCorner(new BlockPos(bb.minX(), bb.maxY(), bb.maxZ()));
                                } else {
                                    BE.setCorner(new BlockPos(bb.minX(), bb.maxY(), bb.minZ()));
                                }
                            } else {
                                if (blockpos.getZ() == bb.minZ()) {
                                    BE.setCorner(new BlockPos(bb.minX(), bb.minY(), bb.maxZ()));
                                } else {
                                    BE.setCorner(new BlockPos(bb.minX(), bb.minY(), bb.minZ()));
                                }
                            }
                        }
                        int j = bb.maxX() - bb.minX();
                        int k = bb.maxY() - bb.minY();
                        int l = bb.maxZ() - bb.minZ();
                        if (j >= 0 && k >= 0 && l >= 0) {
                            BE.setStructurePos(new BlockPos(bb.minX() - blockpos.getX(), bb.minY() - blockpos.getY(), bb.minZ() - blockpos.getZ()));
                            BE.setStructureSize(new Vec3i(j + 1, k + 1, l + 1));
                            level().sendBlockUpdated(blockpos, level().getBlockState(blockpos), level().getBlockState(blockpos), 3);
                        }
                    }
                }
            }
        } else {
            if (!level().isClientSide() && buildPos != null && !entityData.get(BUILD_NAME).isEmpty()) {
                if (level().getBlockEntity(buildPos) instanceof ConstructorBlockEntity BE && !entityData.get(BUILD_NAME).equals(BE.getStructureName())) {
                    BE.setShowBoundingBox(false);
                }
            }
        }
        if (this.level().isClientSide()) {
            animateWhen("idle", !isMoving(this));
            animateWhen("chop", entityData.get(IS_BUILDING));
        }
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor pLevel, DifficultyInstance pDifficulty, MobSpawnType pReason, @Nullable SpawnGroupData pSpawnData, @Nullable CompoundTag pDataTag) {
        entityData.set(VARIANT, random.nextBoolean() ? BeaverVariant.LIGHT : BeaverVariant.DARK);
        return super.finalizeSpawn(pLevel, pDifficulty, pReason, pSpawnData, pDataTag);
    }

    @Override
    public void setInSittingPose(boolean pSitting) {
        super.setInSittingPose(pSitting);
        if (pSitting) {
            this.setPose(Pose.SITTING);
        } else {
            this.setPose(Pose.STANDING);
        }
    }

    @Nullable
    @Override
    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundRegistry.BEAVER_AMBIENCE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getDeathSound() {
        return SoundRegistry.BEAVER_AMBIENCE.get();
    }

    @Nullable
    @Override
    protected SoundEvent getAmbientSound() {
        return SoundRegistry.BEAVER_AMBIENCE.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.5f;
    }

    public static void reloadStructures(ServerLevel level) {
        Beaver.structures = CompletableFuture.supplyAsync((() -> {
            try {
                canReload.await(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                LogManager.getLogger().catching(e);
            }
            canReload = new CountDownLatch(1);
            return StreamUtils.execute(() -> level.getStructureManager().listTemplates()
                .parallel().filter(template ->
                        level.getStructureManager().get(template)
                                .map(structureTemplate ->
                                        structureTemplate.palettes.stream()
                                                .allMatch(palette ->
                                                        palette.blocks().stream()
                                                                .parallel()
                                                                .allMatch(block ->
                                                                        ConstructorBlockEntity.PALETTE.containsKey(block.state().getBlock()))))
                                .orElse(false)).toList());
        }));
    }

    public enum BeaverVariant {
        LIGHT(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_normal.png"), new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_bob_normal.png")),
        DARK(new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_dark.png"), new ResourceLocation(RecraftedCreatures.MODID, "textures/entity/beaver_bob_dark.png"));

        private final ResourceLocation normal;
        private final ResourceLocation hat;

        BeaverVariant(ResourceLocation normal, ResourceLocation hat) {
            this.normal = normal;
            this.hat = hat;
        }

        public ResourceLocation getTexture(boolean bob) {
            return bob ? hat : normal;
        }
    }

    static {
        EntityDataSerializers.registerSerializer(BEAVER_VARIANT_SERIALIZER);
        EntityDataSerializers.registerSerializer(MIRROR_SERIALIZER);
        EntityDataSerializers.registerSerializer(ROTATION_SERIALIZER);
    }
}
