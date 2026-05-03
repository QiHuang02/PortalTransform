package cn.qihuang02.project_dimension.datagen;

import cn.qihuang02.project_dimension.symbol.DimensionSymbolManager;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import com.google.gson.JsonObject;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class DimensionSymbolDataProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public DimensionSymbolDataProvider(PackOutput output) {
        ResourceLocation registryLocation = DimensionSymbolManager.DIMENSION_SYMBOL_REGISTRY_KEY.location();
        this.pathProvider = output.createPathProvider(
                PackOutput.Target.DATA_PACK,
                registryLocation.getNamespace() + "/" + registryLocation.getPath()
        );
    }

    public static Map<ResourceLocation, DimensionSymbolVector> vanillaEntries() {
        Map<ResourceLocation, DimensionSymbolVector> entries = new LinkedHashMap<>();
        entries.put(Level.OVERWORLD.location(), new DimensionSymbolVector(6, 4, 1, -2, 1, 0));
        entries.put(Level.NETHER.location(), new DimensionSymbolVector(-5, 2, 8, 1, 5, -1));
        entries.put(Level.END.location(), new DimensionSymbolVector(-6, 1, -2, 8, -1, 6));
        return entries;
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (var entry : vanillaEntries().entrySet()) {
            ResourceLocation dimId = entry.getKey();
            DimensionSymbolVector vector = entry.getValue();

            JsonObject json = new JsonObject();
            json.addProperty("dimension", dimId.toString());
            json.add("symbols", DimensionSymbolVector.RAW_CODEC.encodeStart(JsonOps.INSTANCE, vector).getOrThrow());
            ResourceLocation outputId = ResourceLocation.fromNamespaceAndPath(
                    dimId.getNamespace(),
                    dimId.getPath()
            );
            Path path = pathProvider.json(outputId);
            futures.add(DataProvider.saveStable(output, json, path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "Dimension Symbols: " + DimensionSymbolManager.DIMENSION_SYMBOL_REGISTRY_KEY.location();
    }
}
