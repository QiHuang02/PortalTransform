package cn.qihuang02.project_dimension.register.item;

import cn.qihuang02.project_dimension.ProjectDimension;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterials;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ProjectDimension.MODID);

    public static final Supplier<Item> LENS = ITEMS.register("lens",
            () -> new ArmorItem(
                    ArmorMaterials.IRON,
                    ArmorItem.Type.HELMET,
                    new Item.Properties().stacksTo(1)
            ));
}
