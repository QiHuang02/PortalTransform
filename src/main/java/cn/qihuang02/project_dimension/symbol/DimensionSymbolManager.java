package cn.qihuang02.project_dimension.symbol;

import cn.qihuang02.project_dimension.ProjectDimension;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DimensionSymbolManager extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "project_dimension/dimension_symbols";
    private static final Gson GSON = new Gson();
    public static final DimensionSymbolManager INSTANCE = new DimensionSymbolManager();

    private volatile Map<ResourceKey<Level>, DimensionSymbolVector> symbols = Collections.emptyMap();

    private DimensionSymbolManager() {
        super(GSON, DIRECTORY);
    }

    public DimensionSymbolVector getBase(ResourceKey<Level> dimension) {
        DimensionSymbolVector vector = symbols.get(dimension);
        if (vector == null) {
            ProjectDimension.LOGGER.debug("维度 {} 未声明象征向量，使用零向量", dimension.location());
            return DimensionSymbolVector.ZERO;
        }
        return vector;
    }

    public Map<ResourceKey<Level>, DimensionSymbolVector> snapshot() {
        return symbols;
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> resources, @NotNull ResourceManager resourceManager, @NotNull ProfilerFiller profiler) {
        // reload 线程先构建临时表，完成后一次性替换不可变快照，避免命令读取到半更新状态。
        Map<ResourceKey<Level>, DimensionSymbolVector> loaded = new HashMap<>();

        resources.forEach((resourceId, element) -> {
            try {
                if (!element.isJsonObject()) {
                    throw new JsonParseException("根节点必须是对象");
                }
                DimensionSymbolDefinition definition = DimensionSymbolDefinition.fromJson((JsonObject) element);
                loaded.put(definition.dimension(), definition.symbols());
            } catch (RuntimeException ex) {
                ProjectDimension.LOGGER.warn("跳过无效维度象征定义 {}：{}", resourceId, ex.getMessage());
            }
        });

        symbols = Map.copyOf(loaded);
        ProjectDimension.LOGGER.info("已加载 {} 个维度象征定义", symbols.size());
    }
}
