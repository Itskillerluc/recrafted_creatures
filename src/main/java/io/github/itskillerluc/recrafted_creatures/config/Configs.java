package io.github.itskillerluc.recrafted_creatures.config;

import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeConfigSpec;

import java.util.List;

public class Configs {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec COMMON_SPEC;

    static {
        ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();
        Client.setupClientConfig(clientConfigBuilder);
        CLIENT_SPEC = clientConfigBuilder.build();

        ForgeConfigSpec.Builder serverConfigBuilder = new ForgeConfigSpec.Builder();
        Server.setupServerConfig(serverConfigBuilder);
        SERVER_SPEC = serverConfigBuilder.build();

        ForgeConfigSpec.Builder commonConfigBuilder = new ForgeConfigSpec.Builder();
        Common.setupCommonConfig(commonConfigBuilder);
        COMMON_SPEC = commonConfigBuilder.build();
    }

    public static class Client {
        public static ForgeConfigSpec.BooleanValue showPatchouliWarning;

        private static void setupClientConfig(ForgeConfigSpec.Builder builder) {
            showPatchouliWarning = builder.comment("Disable the patchouli warning when joining a world.").define("show_patchouli_warning", true);
        }
    }

    public static class Server {
        public static ForgeConfigSpec.ConfigValue<List<? extends String>> beaverStructureWhitelist;

        private static void setupServerConfig(ForgeConfigSpec.Builder builder) {
            beaverStructureWhitelist = builder.comment("Only load the structures from the modids in this list. Load all if list is empty.")
                    .defineListAllowEmpty("beaver_structure_whitelist", List.of(), object -> object instanceof String str && ResourceLocation.isValidNamespace(str));
        }
    }

    public static class Common {
        private static void setupCommonConfig(ForgeConfigSpec.Builder builder) {

        }
    }
}
