package com.example.createaddon;

import com.mojang.logging.LogUtils;
import com.example.createaddon.content.guidance.threshold.ThresholdControllerManager;
import com.example.createaddon.registry.ModBlockEntities;
import com.example.createaddon.registry.ModBlocks;
import com.example.createaddon.registry.ModCreativeTabs;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.ServerChatEvent;
import org.slf4j.Logger;

@Mod(CreateAddon.MOD_ID)
public class CreateAddon {
    public static final String MOD_ID = "createaddon";

    private static final Logger LOGGER = LogUtils.getLogger();

    public CreateAddon(IEventBus modEventBus) {
        ModBlocks.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModCreativeTabs.register(modEventBus);
        if (FMLEnvironment.dist == Dist.CLIENT)
            com.example.createaddon.client.CreateAddonClient.register(modEventBus);

        NeoForge.EVENT_BUS.addListener(this::onServerChat);

        LOGGER.info("CreateAddon loaded");
    }

    private void onServerChat(ServerChatEvent event) {
        ServerPlayer player = event.getPlayer();
        if (ThresholdControllerManager.isPending(player)) {
            String message = event.getMessage().getString();
            try {
                Double.parseDouble(message.trim());
                ThresholdControllerManager.handleChat(player, message);
                event.setCanceled(true);
            } catch (NumberFormatException ignored) {
            }
        }
    }
}
