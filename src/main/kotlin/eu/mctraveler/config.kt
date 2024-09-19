package eu.mctraveler

import com.google.gson.Gson
import net.minecraft.client.Minecraft

class McTravelerConfigModel {
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
var config: McTravelerConfigModel? = null

fun initializeConfig() {
  if (!configFile.exists()) {
    config = McTravelerConfigModel()
    config!!.save()

    return
  }

  config = Gson().fromJson(configFile.readText(), McTravelerConfigModel::class.java)
  if (config == null) {
    configFile.delete()
    initializeConfig()
  }
}