package eu.mctraveler

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

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
  PayloadTypeRegistry.playS2C().register(SetAutoLoginEnabledPayload.TYPE, SetAutoLoginEnabledPayload.STREAM_CODEC)
  ClientPlayNetworking.registerGlobalReceiver(
    SetAutoLoginEnabledPayload.TYPE
  ) { payload: SetAutoLoginEnabledPayload, _: ClientPlayNetworking.Context ->
    config!!.isAutoJoinEnabled = payload.enabled
    config!!.save()
    logger.info("Received auto-login enabled payload ${payload.enabled}")
  }
}