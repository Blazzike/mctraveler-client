package eu.mctraveler.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.scores.DisplaySlot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

import static eu.mctraveler.Discord_activityKt.*;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin extends ClientCommonPacketListenerImpl {
    private String previousRegionName;

    protected ClientPacketListenerMixin(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie, Minecraft minecraft1) {
        super(minecraft, connection, commonListenerCookie);
    }

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
        if (p.getSlot() != DisplaySlot.SIDEBAR) {
            return;
        }

        updateIsInRegion(p.getObjectiveName() != null);
    }


    @Inject(method = "handlePlayerInfoUpdate", at = @At("HEAD"))
    private void handlePlayerInfoUpdate(ClientboundPlayerInfoUpdatePacket p, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }

        updatePlayerCount(player.connection.getOnlinePlayers().size());
    }

    @Inject(method = "handlePlayerInfoRemove", at = @At("HEAD"))
    private void handlePlayerInfoRemove(ClientboundPlayerInfoRemovePacket p, CallbackInfo ci) {
        LocalPlayer player = this.minecraft.player;
        if (player == null) {
            return;
        }

        updatePlayerCount(player.connection.getOnlinePlayers().size());
    }
}

