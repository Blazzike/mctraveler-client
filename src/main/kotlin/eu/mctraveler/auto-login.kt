package eu.mctraveler

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.client.multiplayer.ClientPacketListener
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.network.protocol.game.ClientboundAwardStatsPacket
import net.minecraft.network.protocol.game.ServerPacketListener
import net.minecraft.network.protocol.status.ClientboundStatusResponsePacket
import net.minecraft.network.protocol.status.ServerboundStatusRequestPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.stats.ServerStatsCounter

var didAutoJoin = false

@JvmRecord
data class SetAutoLoginEnabledPayload(
  val enabled: Boolean,
) : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<SetAutoLoginEnabledPayload> {
    return TYPE
  }

  companion object {
    // We have a type that wraps the resource location.
    val TYPE: CustomPacketPayload.Type<SetAutoLoginEnabledPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "set-auto-join-enabled"))

    // And we have a stream codec, here using RFBB (RegistryFriendlyByteBuf) because item stacks require it.
    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, SetAutoLoginEnabledPayload> = StreamCodec.composite(
      ByteBufCodecs.BOOL,
      SetAutoLoginEnabledPayload::enabled,
    ) { enabled: Boolean ->
      SetAutoLoginEnabledPayload(
        enabled
      )
    }
  }
}

fun initializeAutoLogin() {
  ServerStatsCounter
  PayloadTypeRegistry.playS2C().register(SetAutoLoginEnabledPayload.TYPE, SetAutoLoginEnabledPayload.STREAM_CODEC)
  ClientPlayNetworking.registerGlobalReceiver(
    SetAutoLoginEnabledPayload.TYPE
  ) { payload: SetAutoLoginEnabledPayload, _: ClientPlayNetworking.Context ->
    config!!.isAutoJoinEnabled = payload.enabled
    config!!.save()
    logger.info("Received auto-login enabled payload ${payload.enabled}")
  }


  var hasAttemptedAutoJoin = false
  ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
    if (screen !is TitleScreen) {
      return@register
  }

    if (hasAttemptedAutoJoin) {
      return@register
    }

    if (!config!!.isAutoJoinEnabled) {
      return@register
    }
    
    hasAttemptedAutoJoin = true
    didAutoJoin = true
    joinMcTraveler(screen, Minecraft.getInstance())
  }
}