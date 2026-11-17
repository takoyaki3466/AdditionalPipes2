package com.takoy3466.additional_pipes2.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.takoy3466.additional_pipes2.AdditionalPipes2;
import com.takoy3466.additional_pipes2.client.screen.AdvancedWoodScreen;
import com.takoy3466.additional_pipes2.client.screen.DistributorScreen;
import com.takoy3466.additional_pipes2.client.screen.TeleportPipeScreen;
import com.takoy3466.additional_pipes2.registry.ModMenus;
import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import org.lwjgl.glfw.GLFW;

/** クライアント専用の登録 (GUI 画面・キー割り当て)。サーバーではこのクラスは読み込まれない。 */
@EventBusSubscriber(modid = AdditionalPipes2.MODID, value = Dist.CLIENT)
public final class APClient {
    private static final String KEY_CATEGORY = "key.categories." + AdditionalPipes2.MODID;

    /** 旧 laserKeyBinding (LWJGL2 のキー 67 = F9): チャンクローダーの範囲表示の切り替え。 */
    public static final KeyMapping TOGGLE_CHUNK_LOADER_BOUNDARIES = new KeyMapping(
            "key." + AdditionalPipes2.MODID + ".toggle_chunk_loader_boundaries",
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_F9, KEY_CATEGORY);

    private APClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.TELEPORT_PIPE.get(), TeleportPipeScreen::new);
        event.register(ModMenus.DISTRIBUTION_PIPE.get(), DistributorScreen::new);
        event.register(ModMenus.ADVANCED_WOODEN_PIPE.get(), AdvancedWoodScreen::new);
    }

    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TOGGLE_CHUNK_LOADER_BOUNDARIES);
    }
}
