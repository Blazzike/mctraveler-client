package eu.mctraveler

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.minecraft.resources.ResourceLocation

var didAutoJoin = false

fun initializeAutoLogin() {
  ClientPlayNetworking.registerGlobalReceiver(
    ResourceLocation("mctraveler-client", "set-auto-join-enabled")
  ) { _, _, buf, _ ->
    buf.readBoolean().let { enabled ->
      config!!.isAutoJoinEnabled = enabled
      config!!.save()
      logger.info("Received auto-login enabled payload ${enabled}")
    }
  }
}