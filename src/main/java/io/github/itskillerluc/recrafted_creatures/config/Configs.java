package io.github.itskillerluc.recrafted_creatures.config;

import net.minecraftforge.common.ForgeConfigSpec;

public class Configs {
    public static final ForgeConfigSpec CLIENT_SPEC;
    public static final ForgeConfigSpec SERVER_SPEC;

    static {
        ForgeConfigSpec.Builder clientConfigBuilder = new ForgeConfigSpec.Builder();
        Client.setupClientConfig(clientConfigBuilder);
        CLIENT_SPEC = clientConfigBuilder.build();

        ForgeConfigSpec.Builder serverConfigBuilder = new ForgeConfigSpec.Builder();
        Server.setupServerConfig(serverConfigBuilder);
        SERVER_SPEC = serverConfigBuilder.build();
    }

    public static class Client {
        public static ForgeConfigSpec.BooleanValue showPatchouliWarning;

        private static void setupClientConfig(ForgeConfigSpec.Builder builder) {
            showPatchouliWarning = builder.comment("Disable the patchouli warning when joining a world.").define("show_patchouli_warning", true);
        }
    }

    public static class Server {
        private static void setupServerConfig(ForgeConfigSpec.Builder builder) {

        }
    }
}
