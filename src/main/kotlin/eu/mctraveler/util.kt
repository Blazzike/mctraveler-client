package eu.mctraveler

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.ConnectScreen
import net.minecraft.client.gui.screens.Screen
import net.minecraft.client.multiplayer.ServerData
import net.minecraft.client.multiplayer.resolver.ServerAddress

val serverIp = if (isDevelopmentEnvironment) "localhost" else "play.MCTraveler.eu"

fun joinMcTraveler(screen: Screen, minecraft: Minecraft) {
  ConnectScreen.startConnecting(
    screen,
    minecraft,
    ServerAddress.parseString(serverIp),
    ServerData("MCTraveler", serverIp, ServerData.Type.OTHER),
    false,
    null
  )
}