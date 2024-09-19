package eu.mctraveler

import io.github.vyfor.kpresence.ConnectionState
import io.github.vyfor.kpresence.RichClient
import io.github.vyfor.kpresence.event.ActivityUpdateEvent
import io.github.vyfor.kpresence.event.DisconnectEvent
import io.github.vyfor.kpresence.rpc.ActivityType
import io.github.vyfor.kpresence.utils.epochMillis
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import kotlin.math.max

var regionName: String = ""
var playerCount = 0
var connectionTime: Long? = null
var isInRegion = false;

val discordRichClient = RichClient(708032641261371424)

fun initializeDiscordActivity() {
  discordRichClient.on<ActivityUpdateEvent> {
    logger?.info("Updated rich presence")
  }

  discordRichClient.on<DisconnectEvent> {
    connect(shouldBlock = true)
  }

  ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
    val ip = client.connection?.serverData?.ip ?: return@register

    if (isDevelopmentEnvironment || ip.lowercase().endsWith("mctraveler.eu")) {
      disconnect()
    }
  }
}

fun mcTravelerConnect() {
  connectionTime = epochMillis()
  updateActivity()
}

fun updateIsInRegion(newIsInRegion: Boolean) {
  isInRegion = newIsInRegion
  updateActivity()
}

fun disconnect() {
  connectionTime = null
  updateActivity()
}

fun updatePlayerCount(count: Int) {
  playerCount = max(0, count - 1)
  updateActivity()
}

fun updateRegionName(name: String) {
  regionName = name
  updateActivity()
}

fun updateActivity() {
  try {
    if (connectionTime == null) {
      discordRichClient.clear()

      return
    }

    if (discordRichClient.connectionState != ConnectionState.CONNECTED) {
      discordRichClient.connect()
    }

    discordRichClient.update {
      type = ActivityType.GAME
      details = "Playing with $playerCount other player${if (playerCount == 1) "" else "s"}"
      state = "In ${if (!isInRegion) "the wilderness" else regionName}"

      timestamps {
        start = connectionTime
      }

      assets {
        largeImage = "logo"
        largeText = "play.MCTraveler.eu"
      }

      button("Visit Website", "https://mctraveler.eu/")
    }
  } catch (e: Exception) {
//    logger.error("Failed to update discord presence", e) TODO
  }
}