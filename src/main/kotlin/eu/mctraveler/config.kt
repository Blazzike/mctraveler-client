package eu.mctraveler

import com.google.gson.Gson
import net.minecraft.client.Minecraft

class McTravelerConfig {
  @JvmField
  var isAutoJoinEnabled = true

  fun save() {
    configFile.writeText(toJson())
  }

  private fun toJson(): String {
    return Gson().toJson(this)
  }
}

val configFile = Minecraft.getInstance().gameDirectory.resolve("config/mctraveler-config.json")
var config: McTravelerConfig? = null

fun initializeConfig() {
  if (!configFile.exists()) {
    config = McTravelerConfig()
    config!!.save()

    return
  }

  config = Gson().fromJson(configFile.readText(), McTravelerConfig::class.java)
  if (config == null) {
    configFile.delete()
    initializeConfig()
  }
}