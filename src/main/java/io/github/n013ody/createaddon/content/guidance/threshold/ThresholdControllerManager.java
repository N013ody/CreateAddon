package io.github.n013ody.createaddon.content.guidance.threshold;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class ThresholdControllerManager {
    private static final Map<UUID, BlockPos> pendingPlayers = new HashMap<>();

    public static void markPending(Player player, BlockPos pos) {
        pendingPlayers.put(player.getUUID(), pos);
    }

    public static boolean isPending(Player player) {
        return pendingPlayers.containsKey(player.getUUID());
    }

    public static void handleChat(ServerPlayer player, String message) {
        BlockPos pos = pendingPlayers.remove(player.getUUID());
        if (pos == null)
            return;

        Level level = player.level();
        if (!(level.getBlockEntity(pos) instanceof ThresholdControllerBlockEntity be))
            return;

        try {
            double value = Double.parseDouble(message.trim());
            be.setThreshold(value);
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.translatable("message.createaddon.threshold.set", String.valueOf(value)), true);
        } catch (NumberFormatException e) {
            player.displayClientMessage(
                    net.minecraft.network.chat.Component.literal("搂cInvalid number"), true);
        }
    }
}

