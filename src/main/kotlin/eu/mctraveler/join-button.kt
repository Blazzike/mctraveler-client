package eu.mctraveler

import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents
import net.fabricmc.fabric.api.client.screen.v1.Screens
import net.minecraft.client.gui.components.SpriteIconButton
import net.minecraft.client.gui.screens.TitleScreen
import net.minecraft.network.chat.Component
import net.minecraft.resources.ResourceLocation

fun initializeJoinButton() {
  ScreenEvents.AFTER_INIT.register { client, screen, scaledWidth, scaledHeight ->
    if (screen !is TitleScreen) {
      return@register
    }

    val iconButton = SpriteIconButton.builder(
      Component.literal("Join MCTraveler"),
      { _ ->
        checkNotNull(client)
        joinMcTraveler(screen, client)
      }, true

    ).width(20).sprite(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "icon/mctraveler"), 15, 15).build()

    iconButton.x = scaledWidth / 2 + 104
    iconButton.y = (scaledHeight / 4 + 48) + 24
    Screens.getButtons(screen).add(
      0, iconButton
    )


  }
}