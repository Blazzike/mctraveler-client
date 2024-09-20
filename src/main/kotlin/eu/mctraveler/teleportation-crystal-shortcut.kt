package eu.mctraveler

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

class OpenTeleportationCrystalPayload : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<OpenTeleportationCrystalPayload> {
    return TYPE
  }

  companion object {
    // We have a type that wraps the resource location.
    val TYPE: CustomPacketPayload.Type<OpenTeleportationCrystalPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "open-teleportation-crystal"))

    // And we have a stream codec, here using RFBB (RegistryFriendlyByteBuf) because item stacks require it.
    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, OpenTeleportationCrystalPayload> =
      object : StreamCodec<RegistryFriendlyByteBuf, OpenTeleportationCrystalPayload> {
        override fun encode(buf: RegistryFriendlyByteBuf, value: OpenTeleportationCrystalPayload) {
        }

        override fun decode(buf: RegistryFriendlyByteBuf): OpenTeleportationCrystalPayload {
          return OpenTeleportationCrystalPayload()
        }
      }

  }
}

fun initializeTeleportationCrystalShortcut() {
  PayloadTypeRegistry.playC2S()
    .register(OpenTeleportationCrystalPayload.TYPE, OpenTeleportationCrystalPayload.STREAM_CODEC)

  val keyMapping = KeyMapping(
    "mctraveler.controls.teleportationCrystalShortcut",
    InputConstants.Type.KEYSYM,
    GLFW.GLFW_KEY_BACKSLASH,
    "mctraveler.controls.category"
  )

  KeyBindingHelper.registerKeyBinding(
    keyMapping
  )

  fun onPress() {
    logger.info("Sending open teleportation crystal packet")
    Minecraft.getInstance().connection?.send(
      ServerboundCustomPayloadPacket(
        OpenTeleportationCrystalPayload()
      )
    )
  }

  var isPressed = false
  ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: Minecraft ->
    if (!isOnMcTraveler) {
      return@EndTick
    }

    if (keyMapping.isDown && !isPressed) {
      onPress()
    }

    isPressed = keyMapping.isDown
  })
}