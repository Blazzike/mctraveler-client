package eu.mctraveler

import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket
import net.minecraft.resources.ResourceLocation

fun initializeHello() {
  ClientPlayConnectionEvents.JOIN.register { _, _, client ->
    val ip = client.connection?.serverData?.ip ?: return@register

    if (isDevelopmentEnvironment || ip.lowercase().endsWith("mctraveler.eu")) {
      mcTravelerConnect()
      val mods =
        FabricLoader.getInstance().allMods.map { "${it.metadata.id}@${it.metadata.version.friendlyString}" }.toList()
      val version = FabricLoader.getInstance().getModContainer("mctraveler-client").orElse(null)?.metadata?.version
      val payloadVersion = version?.friendlyString ?: "unknown"
      val friendlyByteBuf = FriendlyByteBuf(Unpooled.buffer())
      friendlyByteBuf
        .writeUtf(payloadVersion)
        .writeCollection(mods, FriendlyByteBuf::writeUtf)

      friendlyByteBuf
        .writeBoolean(config!!.isAutoJoinEnabled)
        .writeBoolean(didAutoJoin)

      client.connection?.send(
        ServerboundCustomPayloadPacket(
          ResourceLocation("mctraveler-client", "hello"),
          friendlyByteBuf
        )
      )

      didAutoJoin = false
    }
  }
}