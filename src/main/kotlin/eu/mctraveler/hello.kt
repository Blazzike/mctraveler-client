package eu.mctraveler

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

@JvmRecord
data class HelloPayload(
  val version: String,
  val mods: List<String>,
  val isAutoJoinEnabled: Boolean,
  val didAutoLogin: Boolean,
) : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<HelloPayload> {
    return TYPE
  }

  companion object {
    // We have a type that wraps the resource location.
    val TYPE: CustomPacketPayload.Type<HelloPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "hello"))

    // And we have a stream codec, here using RFBB (RegistryFriendlyByteBuf) because item stacks require it.
    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, HelloPayload> = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      HelloPayload::version,
      ByteBufCodecs.STRING_UTF8.apply(ByteBufCodecs.list()),
      HelloPayload::mods,
      ByteBufCodecs.BOOL,
      HelloPayload::isAutoJoinEnabled,
      ByteBufCodecs.BOOL,
      HelloPayload::didAutoLogin
    ) { version: String, mods: List<String>, isAutoJoinEnabled: Boolean, didAutoLogin: Boolean ->
      HelloPayload(
        version,
        mods,
        isAutoJoinEnabled,
        didAutoLogin
      )
    }
  }
}

fun initializeHello() {
  PayloadTypeRegistry.playC2S().register(HelloPayload.TYPE, HelloPayload.STREAM_CODEC)
  ClientPlayConnectionEvents.JOIN.register { _, _, client ->
    val ip = client.connection?.serverData?.ip ?: return@register

    if (isDevelopmentEnvironment || ip.lowercase().endsWith("mctraveler.eu")) {
      mcTravelerConnect()
      val mods =
        FabricLoader.getInstance().allMods.map { "${it.metadata.id}@${it.metadata.version.friendlyString}" }.toList()
      val version = FabricLoader.getInstance().getModContainer("mctraveler-client").orElse(null)?.metadata?.version
      client.connection?.send(
        ServerboundCustomPayloadPacket(
          HelloPayload(
            version?.friendlyString ?: "unknown",
            mods,
            config!!.isAutoJoinEnabled,
            didAutoJoin,
          )
        )
      )

      didAutoJoin = false
    }
  }
}