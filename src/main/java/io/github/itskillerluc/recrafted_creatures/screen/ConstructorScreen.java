package io.github.itskillerluc.recrafted_creatures.screen;

import io.github.itskillerluc.recrafted_creatures.blockentity.ConstructorBlockEntity;
import io.github.itskillerluc.recrafted_creatures.networking.NetworkChannel;
import io.github.itskillerluc.recrafted_creatures.networking.packets.SaveStructurePacket;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public class ConstructorScreen extends Screen {
    private static final Component SEARCH_HINT = Component.translatableWithFallback("gui.socialInteractions.name_hint", "Put a name here").withStyle(ChatFormatting.ITALIC).withStyle(ChatFormatting.GRAY);
    final ConstructorBlockEntity constructorBlockEntity;
    EditBox nameBox;
    Button save;
    Button cancel;
    public ConstructorScreen(ConstructorBlockEntity constructorBlockEntity) {
        super(Component.translatableWithFallback("gui.constructor.title", "Constructor"));
        this.constructorBlockEntity = constructorBlockEntity;
    }

    @Override
    protected void init() {
        super.init();
        nameBox = new EditBox(this.font, this.width / 2 - 100, this.height / 2 - 30, 200, 20, SEARCH_HINT);
        save = Button.builder(Component.translatableWithFallback("gui.constructor.save", "Save"),button -> {
            NetworkChannel.CHANNEL.sendToServer(new SaveStructurePacket(nameBox.getValue(), constructorBlockEntity.getBlockPos()));
            onClose();
            if (Minecraft.getInstance().player != null) {
                Minecraft.getInstance().player.displayClientMessage(Component.translatableWithFallback("gui.constructor.saved", "Saved structure!").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD), true);
            }
        }).pos(this.width / 2 - 100, this.height / 2).size(80, 20).build();
        cancel = Button.builder(Component.translatableWithFallback("gui.constructor.cancel", "Cancel"),button -> {
            onClose();
        }).pos(this.width / 2 + 20, this.height / 2).size(80, 20).build();
        addRenderableWidget(nameBox);
        addRenderableWidget(save);
        addRenderableWidget(cancel);
    }
}
