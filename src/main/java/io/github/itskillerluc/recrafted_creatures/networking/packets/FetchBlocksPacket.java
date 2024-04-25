package io.github.itskillerluc.recrafted_creatures.networking.packets;

import com.google.common.collect.Lists;
import com.mojang.datafixers.util.Pair;
import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.entity.Beaver;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.GameData;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.processBlockInfos;

public class FetchBlocksPacket {
    private final ResourceLocation structure;
    private final int entity;
    public FetchBlocksPacket(ResourceLocation structure, int entity) {
        this.structure = structure;
        this.entity = entity;
    }

    public static FetchBlocksPacket decoder(FriendlyByteBuf buffer) {
        return new FetchBlocksPacket(buffer.readResourceLocation(), buffer.readInt());
    }

    public void encoder(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(structure);
        buffer.writeInt(entity);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        List<ItemStack> itemStacks = new ArrayList<>();
        if (ctx.get().getSender() == null) return;
        StructureTemplateManager structureManager = ctx.get().getSender().serverLevel().getStructureManager();
        var structure = structureManager.get(this.structure);
        structure.ifPresent(structureTemplate -> {
            itemStacks.addAll(getStacks(ctx.get().getSender().serverLevel(), new StructurePlaceSettings(), structureTemplate));
        });
        if (ctx.get().getSender().serverLevel().getEntity(entity) instanceof Beaver beaver) {
            beaver.requiredMaterials = itemStacks;
            List<ItemStack> neededList = new ArrayList<>();
            for (ItemStack requiredMaterial : beaver.requiredMaterials) {
                neededList.add(requiredMaterial.copyWithCount(Math.max(0, requiredMaterial.getCount() - beaver.materials.stream().filter(item -> item.is(requiredMaterial.getItem())).findFirst().orElse(new ItemStack(Items.AIR, 0)).getCount())));
            }
            neededList.removeIf(item -> item.getCount() <= 0);

            NetworkChannel.CHANNEL.send(PacketDistributor.PLAYER.with(() -> ctx.get().getSender()), new SetBlocksPacket(beaver.requiredMaterials.stream()
                    .map(material -> {
                        var count = Math.max(0, material.getCount() - beaver.materials.stream().filter(item -> item.is(material.getItem())).findFirst().orElse(new ItemStack(Items.AIR, 0)).getCount());
                        return material.copyWithCount(count);
                    })
                    .filter(material -> material.getCount() > 0)
                    .toList()));
        }
    }

    public static List<ItemStack> getStacks(ServerLevelAccessor pServerLevel, StructurePlaceSettings pSettings, StructureTemplate template) {
        if (template.palettes.isEmpty()) {
            return List.of();
        } else {
            List<StructureTemplate.StructureBlockInfo> list = pSettings.getRandomPalette(template.palettes, BlockPos.ZERO).blocks();
            if (!list.isEmpty() && template.getSize().getX() >= 1 && template.getSize().getY() >= 1 && template.getSize().getZ() >= 1) {
                BoundingBox boundingbox = pSettings.getBoundingBox();

                List<BlockState> stateList = new ArrayList<>();

                for (StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : processBlockInfos(pServerLevel, BlockPos.ZERO, BlockPos.ZERO, pSettings, list, template)) {
                    BlockPos blockpos = structuretemplate$structureblockinfo.pos();
                    if (boundingbox == null || boundingbox.isInside(blockpos)) {
                        BlockState blockstate = structuretemplate$structureblockinfo.state().mirror(pSettings.getMirror()).rotate(pSettings.getRotation());
                        stateList.add(blockstate);
                    }
                }

                Map<BlockState, Long> counts = stateList.stream().collect(Collectors.groupingBy(e -> e, Collectors.counting()));

                List<ItemStack> result = new ArrayList<>();
                for (Map.Entry<BlockState, Long> blockStateLongEntry : counts.entrySet()) {
                    result.add(new ItemStack(blockStateLongEntry.getKey().getBlock().asItem(), blockStateLongEntry.getValue().intValue()));
                }
                return result;
            }
        }
        return List.of();
    }
}
