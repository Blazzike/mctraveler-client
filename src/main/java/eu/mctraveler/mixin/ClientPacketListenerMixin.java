package eu.mctraveler.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoRemovePacket;
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundSetDisplayObjectivePacket;
import net.minecraft.network.protocol.game.ClientboundSetObjectivePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static eu.mctraveler.Discord_activityKt.*;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {
    @Unique
    private String previousRegionName;

    @Inject(method = "handleAddObjective", at = @At("HEAD"))
    private void handleAddObjective(ClientboundSetObjectivePacket p, CallbackInfo ci) {
        if (!Objects.equals(p.getObjectiveName(), "region")) {
            return;
        }

        String regionName = p.getDisplayName().getString();
        if (regionName.equals(previousRegionName)) {
            return;
        }

        updateRegionName(regionName);
        previousRegionName = regionName;
    }

    @Inject(method = "handleSetDisplayObjective", at = @At("HEAD"))
    private void handleSetDisplayObjective(ClientboundSetDisplayObjectivePacket p, CallbackInfo ci) {
        if (p.getSlot() != 1) {
            return;
        }

        updateIsInRegion(p.getObjectiveName() != null);
    }


    @Inject(method = "handlePlayerInfoUpdate", at = @At("HEAD"))
    private void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket p, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        updatePlayerCount(player.connection.getOnlinePlayers().size());
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("HEAD"))
    private void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket p, CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        updatePlayerCount(player.connection.getOnlinePlayers().size());
    }
}

