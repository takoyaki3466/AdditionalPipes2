package com.takoy3466.additional_pipes2.registry;

import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.menu.AdvancedWoodMenu;
import com.takoy3466.additional_pipes2.menu.DistributorMenu;
import com.takoy3466.additional_pipes2.menu.TeleportPipeMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** GUI メニューの登録 (旧 GuiHandler の GUI 番号 1 - 3 に対応)。 */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, AdditionalPipes2.MODID);

    /** 旧 GUI_PIPE_TP (1)。 */
    public static final DeferredHolder<MenuType<?>, MenuType<TeleportPipeMenu>> TELEPORT_PIPE =
            MENUS.register("teleport_pipe", () -> IMenuTypeExtension.create(TeleportPipeMenu::createClient));
    /** 旧 GUI_PIPE_DIST (2)。 */
    public static final DeferredHolder<MenuType<?>, MenuType<DistributorMenu>> DISTRIBUTION_PIPE =
            MENUS.register("distribution_pipe", () -> IMenuTypeExtension.create(DistributorMenu::createClient));
    /** 旧 GUI_PIPE_WOODEN_ADV (3)。 */
    public static final DeferredHolder<MenuType<?>, MenuType<AdvancedWoodMenu>> ADVANCED_WOODEN_PIPE =
            MENUS.register("advanced_wooden_pipe", () -> IMenuTypeExtension.create(AdvancedWoodMenu::createClient));

    private ModMenus() {
    }

    public static void register(IEventBus modEventBus) {
        MENUS.register(modEventBus);
    }
}
