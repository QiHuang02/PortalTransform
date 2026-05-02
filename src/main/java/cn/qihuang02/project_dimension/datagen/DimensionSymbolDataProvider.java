package cn.qihuang02.project_dimension.datagen;

import cn.qihuang02.project_dimension.ProjectDimension;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolDefinition;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolManager;
import cn.qihuang02.project_dimension.symbol.DimensionSymbolVector;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DimensionSymbolDataProvider implements DataProvider {
    private final PackOutput.PathProvider pathProvider;

    public DimensionSymbolDataProvider(PackOutput output) {
        this.pathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, DimensionSymbolManager.DIRECTORY);
    }

    public static List<DimensionSymbolDefinition> vanillaDimensions() {
        return List.of(
                new DimensionSymbolDefinition(Level.OVERWORLD, new DimensionSymbolVector(6, 4, 1, -2, 1, 0)),
                new DimensionSymbolDefinition(Level.NETHER, new DimensionSymbolVector(-5, 2, 8, 1, 5, -1)),
                new DimensionSymbolDefinition(Level.END, new DimensionSymbolVector(-6, 1, -2, 8, -1, 6))
        );
    }

    @Override
    public @NotNull CompletableFuture<?> run(@NotNull CachedOutput output) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (DimensionSymbolDefinition definition : vanillaDimensions()) {
            ResourceLocation dimension = definition.dimension().location();
            ResourceLocation outputId = ResourceLocation.fromNamespaceAndPath(ProjectDimension.MODID, dimension.getNamespace() + "/" + dimension.getPath());
            Path path = pathProvider.json(outputId);
            futures.add(DataProvider.saveStable(output, definition.toJson(), path));
        }
        return CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new));
    }

    @Override
    public @NotNull String getName() {
        return "Project Dimension Symbols";
    }
}
