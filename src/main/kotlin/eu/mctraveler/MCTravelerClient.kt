package eu.mctraveler

import net.fabricmc.api.ModInitializer
import net.fabricmc.loader.api.FabricLoader
import org.slf4j.LoggerFactory

val isDevelopmentEnvironment = FabricLoader.getInstance().isDevelopmentEnvironment
val logger = LoggerFactory.getLogger("mctraveler-client")!!

object MCTravelerClient : ModInitializer {
  override fun onInitialize() {
    initializeConfig()
    initializeAutoLogin()
    initializeDiscordActivity()
    initializeHello()
    initializeTeleportationCrystalShortcut()
  }
}