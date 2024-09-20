package eu.mctraveler

import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

val isDevelopmentEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment
val logger = LoggerFactory.getLogger("mctraveler-client")!!
var isOnMcTraveler = isDevelopmentEnvironment

object MCTravelerClient : ModInitializer {
  override fun onInitialize() {
    initializeConfig()
    initializeAutoLogin()
    initializeDiscordActivity()
    initializeRegionShow()
    initializeJoinButton()
    initializeHello()
    initializeTeleportationCrystalShortcut()

    ClientPlayConnectionEvents.JOIN.register { _, _, client ->
      if (isDevelopmentEnvironment) {
        return@register
      }

      isOnMcTraveler = client.connection?.serverData?.ip?.lowercase()?.endsWith("mctraveler.eu") == true
    }

    ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
      if (isDevelopmentEnvironment) {
        return@register
      }

      isOnMcTraveler = false
    }
  }
}