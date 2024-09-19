package eu.mctraveler

import com.mojang.blaze3d.platform.InputConstants
import io.netty.buffer.Unpooled
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.FriendlyByteBuf
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket
import net.minecraft.resources.ResourceLocation
import org.lwjgl.glfw.GLFW

fun initializeTeleportationCrystalShortcut() {
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
        ResourceLocation("mctraveler-client", "open-teleportation-crystal"),
        FriendlyByteBuf(Unpooled.buffer()),
      )
    )
  }

  var isPressed = false
  ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: Minecraft ->
    if (keyMapping.isDown && !isPressed) {
      onPress()
    }

    isPressed = keyMapping.isDown
  })
}