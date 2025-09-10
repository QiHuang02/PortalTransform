package cn.qihuang02.portaltransform.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.jetbrains.annotations.NotNull;

/**
 * Common configuration settings for PortalTransform.
 */
public class PTConfig {
    public static final ForgeConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        COMMON = new CommonConfig(builder);
        COMMON_SPEC = builder.build();
    }

    public static class CommonConfig {
        public final ForgeConfigSpec.BooleanValue autoInsertIntoChests;
        public final ForgeConfigSpec.IntValue chestSearchRadius;

        public CommonConfig(ForgeConfigSpec.@NotNull Builder builder) {
            builder.push("General");

            autoInsertIntoChests = builder
                    .comment("Whether to automatically insert items into chests")
                    .define("autoInsertIntoChests", true);

            chestSearchRadius = builder
                    .comment("The radius of the chest search")
                    .defineInRange("chestSearchRadius", 3, 0, 10);

            builder.pop();
        }
    }
}

