package cn.qihuang02.portaltransform.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class PTConfig {
    public static final ModConfigSpec COMMON_SPEC;
    public static final CommonConfig COMMON;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        COMMON = new CommonConfig(builder);
        COMMON_SPEC = builder.build();
    }

    public static class CommonConfig {
        public final ModConfigSpec.BooleanValue autoInsertInttoChests;
        public final ModConfigSpec.IntValue chestSearchRadius;

        public CommonConfig(ModConfigSpec.Builder builder) {
            builder.push("General");

            autoInsertInttoChests = builder
                    .comment("Whether to automatically insert items into chests")
                    .define("autoInsertInttoChests", true);

            chestSearchRadius = builder
                    .comment("The radius of the chest search")
                    .defineInRange("chestSearchRadius", 3, 0, 10);

            builder.pop();
        }
    }
}
