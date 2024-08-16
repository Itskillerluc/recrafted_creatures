package io.github.itskillerluc.recrafted_creatures.blockentity;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.AskColorPacket;
import io.github.itskillerluc.recrafted_creatures.registries.BlockRegistry;
import io.github.itskillerluc.recrafted_creatures.util.ClientHelper;
import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraftforge.network.PacketDistributor;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

public class ConstructorBlockEntity extends BlockEntity {
    private static final int SCAN_CORNER_BLOCKS_RANGE = 5;
    public static final BiMap<Block, Integer> PALETTE = HashBiMap.create();
    public static final int MAX_SIZE_PER_AXIS = 48;
    public static final String AUTHOR_TAG = "author";
    private ResourceLocation structureName;
    private String author = "";
    private String metaData = "";
    private BlockPos structurePos = new BlockPos(0, 1, 0);
    private BlockPos corner;
    private Vec3i structureSize = Vec3i.ZERO;
    private boolean showBoundingBox = true;
    private float integrity = 1.0F;
    private long seed;

    public ConstructorBlockEntity(BlockEntityType<?> pType, BlockPos pPos, BlockState pBlockState) {
        super(pType, pPos, pBlockState);
    }

    protected void saveAdditional(CompoundTag pTag) {
        super.saveAdditional(pTag);
        pTag.putString("name", this.getStructureName());
        pTag.putString("author", this.author);
        pTag.putString("metadata", this.metaData);
        pTag.putInt("posX", this.structurePos.getX());
        pTag.putInt("posY", this.structurePos.getY());
        pTag.putInt("posZ", this.structurePos.getZ());
        pTag.putInt("sizeX", this.structureSize.getX());
        pTag.putInt("sizeY", this.structureSize.getY());
        pTag.putInt("sizeZ", this.structureSize.getZ());
        pTag.put("corner", NbtUtils.writeBlockPos(corner));
        pTag.putBoolean("showboundingbox", this.showBoundingBox);
        pTag.putFloat("integrity", this.integrity);
        pTag.putLong("seed", this.seed);
    }

    public void load(CompoundTag pTag) {
        super.load(pTag);
        this.setStructureName(pTag.getString("name"));
        this.author = pTag.getString("author");
        this.metaData = pTag.getString("metadata");
        int i = Mth.clamp(pTag.getInt("posX"), -48, 48);
        int j = Mth.clamp(pTag.getInt("posY"), -48, 48);
        int k = Mth.clamp(pTag.getInt("posZ"), -48, 48);
        this.structurePos = new BlockPos(i, j, k);
        int l = Mth.clamp(pTag.getInt("sizeX"), 0, 48);
        int i1 = Mth.clamp(pTag.getInt("sizeY"), 0, 48);
        int j1 = Mth.clamp(pTag.getInt("sizeZ"), 0, 48);
        this.structureSize = new Vec3i(l, i1, j1);

        this.showBoundingBox = pTag.getBoolean("showboundingbox");
        if (pTag.contains("integrity")) {
            this.integrity = pTag.getFloat("integrity");
        } else {
            this.integrity = 1.0F;
        }

        this.seed = pTag.getLong("seed");
        corner = NbtUtils.readBlockPos(pTag.getCompound("corner"));
    }

    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    public boolean usedBy() {
        if (level != null && level.isClientSide()) {
            ClientHelper.openConstructorScreen(this);
            return true;
        } else return false;
    }

    public String getStructureName() {
        return this.structureName == null ? "" : this.structureName.toString();
    }

    public String getStructurePath() {
        return this.structureName == null ? "" : this.structureName.getPath();
    }

    public boolean hasStructureName() {
        return this.structureName != null;
    }

    public void setStructureName(@Nullable String pStructureName) {
        this.setStructureName(StringUtil.isNullOrEmpty(pStructureName) ? null : ResourceLocation.tryParse(pStructureName));
    }

    public void setStructureName(@Nullable ResourceLocation pStructureName) {
        this.structureName = pStructureName;
    }

    public void createdBy(LivingEntity pAuthor) {
        this.author = pAuthor.getName().getString();
    }

    public BlockPos getStructurePos() {
        return this.structurePos;
    }

    public void setStructurePos(BlockPos pStructurePos) {
        this.structurePos = pStructurePos;
    }

    public Vec3i getStructureSize() {
        return this.structureSize;
    }

    public void setStructureSize(Vec3i pStructureSize) {
        this.structureSize = pStructureSize;
    }

    public String getMetaData() {
        return this.metaData;
    }

    public void setMetaData(String pMetaData) {
        this.metaData = pMetaData;
    }

    public float getIntegrity() {
        return this.integrity;
    }

    public void setIntegrity(float pIntegrity) {
        this.integrity = pIntegrity;
    }

    public long getSeed() {
        return this.seed;
    }

    public void setSeed(long pSeed) {
        this.seed = pSeed;
    }

    public boolean detectSize() {
        BlockPos blockpos = this.getBlockPos();
        int i = 80;
        BlockPos blockpos1 = new BlockPos(blockpos.getX() - 80, this.level.getMinBuildHeight(), blockpos.getZ() - 80);
        BlockPos blockpos2 = new BlockPos(blockpos.getX() + 80, this.level.getMaxBuildHeight() - 1, blockpos.getZ() + 80);
        Stream<BlockPos> stream = this.getRelatedCorners(blockpos1, blockpos2);
        return calculateEnclosingBoundingBox(blockpos, stream).filter((p_155790_) -> {
            if (blockpos.getX() == p_155790_.minX()) {
                if (blockpos.getY() == p_155790_.minY()) {
                    if (blockpos.getZ() == p_155790_.minZ()) {
                        corner = new BlockPos(p_155790_.maxX(), p_155790_.maxY(), p_155790_.maxZ());
                    } else {
                        corner = new BlockPos(p_155790_.maxX(), p_155790_.maxY(), p_155790_.minZ());
                    }
                } else {
                    if (blockpos.getZ() == p_155790_.minZ()) {
                        corner = new BlockPos(p_155790_.maxX(), p_155790_.minY(), p_155790_.maxZ());
                    } else {
                        corner = new BlockPos(p_155790_.maxX(), p_155790_.minY(), p_155790_.minZ());
                    }
                }
            } else {
                if (blockpos.getY() == p_155790_.minY()) {
                    if (blockpos.getZ() == p_155790_.minZ()) {
                        corner = new BlockPos(p_155790_.minX(), p_155790_.maxY(), p_155790_.maxZ());
                    } else {
                        corner = new BlockPos(p_155790_.minX(), p_155790_.maxY(), p_155790_.minZ());
                    }
                } else {
                    if (blockpos.getZ() == p_155790_.minZ()) {
                        corner = new BlockPos(p_155790_.minX(), p_155790_.minY(), p_155790_.maxZ());
                    } else {
                        corner = new BlockPos(p_155790_.minX(), p_155790_.minY(), p_155790_.minZ());
                    }
                }
            }
            int j = p_155790_.maxX() - p_155790_.minX();
            int k = p_155790_.maxY() - p_155790_.minY();
            int l = p_155790_.maxZ() - p_155790_.minZ();
            if (j > 1 && k > 1 && l > 1) {
                this.structurePos = new BlockPos(p_155790_.minX() - blockpos.getX() + 1, p_155790_.minY() - blockpos.getY() + 1, p_155790_.minZ() - blockpos.getZ() + 1);
                this.structureSize = new Vec3i(j - 1, k - 1, l - 1);
                this.setChanged();
                BlockState blockstate = this.level.getBlockState(blockpos);
                this.level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                return true;
            } else {
                return false;
            }
        }).isPresent();
    }

    private Stream<BlockPos> getRelatedCorners(BlockPos pMinPos, BlockPos pMaxPos) {
        return BlockPos.betweenClosedStream(pMinPos, pMaxPos).filter((p_272561_) -> this.level.getBlockState(p_272561_).is(BlockRegistry.CONSTRUCTOR.get())).map(this.level::getBlockEntity).filter((p_155802_) -> p_155802_ instanceof ConstructorBlockEntity).map((p_155785_) -> (ConstructorBlockEntity)p_155785_).map(BlockEntity::getBlockPos);
    }

    private static Optional<BoundingBox> calculateEnclosingBoundingBox(BlockPos pPos, Stream<BlockPos> pRelatedCorners) {
        Iterator<BlockPos> iterator = pRelatedCorners.iterator();
        if (!iterator.hasNext()) {
            return Optional.empty();
        } else {
            BlockPos blockpos = iterator.next();
            BoundingBox boundingbox = new BoundingBox(blockpos);
            if (iterator.hasNext()) {
                iterator.forEachRemaining(boundingbox::encapsulate);
            } else {
                boundingbox.encapsulate(pPos);
            }

            return Optional.of(boundingbox);
        }
    }


    /**
     * Saves the template, either updating the local version or writing it to disk.
     *
     * @return true if the template was successfully saved.
     */
    public boolean saveStructure(ServerPlayer player, AtomicBoolean condition) {
        if (!this.level.isClientSide && this.structureName != null) {
            BlockPos blockpos = this.getBlockPos().offset(this.structurePos);
            ServerLevel serverlevel = (ServerLevel)this.level;
            StructureTemplateManager structuretemplatemanager = serverlevel.getStructureManager();

            StructureTemplate structuretemplate;
            try {
                structuretemplate = structuretemplatemanager.getOrCreate(this.structureName);
            } catch (ResourceLocationException resourcelocationexception1) {
                return false;
            }

            structuretemplate.fillFromWorld(this.level, blockpos, this.structureSize, false, Blocks.AIR);
            structuretemplate.setAuthor(this.author);

            for (int i = 0; i < structuretemplate.palettes.size(); i++) {
                NetworkChannel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new AskColorPacket(structuretemplate.palettes.get(i).blocks().stream().map(StructureTemplate.StructureBlockInfo::state).toList(), structureName, i));
            }

            return true;
        } else if (structureName == null) {
            player.sendSystemMessage(Component.translatableWithFallback("gui.constructor.invalid_name", "Invalid name. Name can only contain [a-z0-9_-] characters."), true);
            return false;
        } else {
            return false;
        }
    }

    public static RandomSource createRandom(long pSeed) {
        return pSeed == 0L ? RandomSource.create(Util.getMillis()) : RandomSource.create(pSeed);
    }

    public boolean getShowBoundingBox() {
        return this.showBoundingBox;
    }

    public void setShowBoundingBox(boolean pShowBoundingBox) {
        this.showBoundingBox = pShowBoundingBox;
    }

    public void setCorner(BlockPos corner) {
        this.corner = corner;
    }

    public BlockPos getCorner() {
        return corner;
    }

    public static enum UpdateType {
        UPDATE_DATA,
        SAVE_AREA,
        LOAD_AREA,
        SCAN_AREA;
    }
}
