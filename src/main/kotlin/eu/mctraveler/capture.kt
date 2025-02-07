package eu.mctraveler

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.Minecraft
import net.minecraft.client.Screenshot
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.ServerboundCustomPayloadPacket
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation

@JvmRecord
data class CaptureInitPayload(
  val uuid: String,
) : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<CaptureInitPayload> {
    return TYPE
  }

  companion object {
    val TYPE: CustomPacketPayload.Type<CaptureInitPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "capture-init"))

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CaptureInitPayload> = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CaptureInitPayload::uuid,
    ) { uuid: String ->
      CaptureInitPayload(
        uuid,
      )
    }
  }
}

@JvmRecord
data class CaptureUploadInitPayload(
  val uuid: String,
  val byteCount: Int,
) : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<CaptureUploadInitPayload> {
    return TYPE
  }

  companion object {
    val TYPE: CustomPacketPayload.Type<CaptureUploadInitPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "capture-upload-init"))

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CaptureUploadInitPayload> = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CaptureUploadInitPayload::uuid,
      ByteBufCodecs.VAR_INT,
      CaptureUploadInitPayload::byteCount,
    ) { uuid: String, byteCount: Int ->
      CaptureUploadInitPayload(
        uuid,
        byteCount,
      )
    }
  }
}

@JvmRecord
data class CaptureUploadChunkPayload(
  val uuid: String,
  val bytes: ByteArray,
) : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<CaptureUploadChunkPayload> {
    return TYPE
  }

  companion object {
    val TYPE: CustomPacketPayload.Type<CaptureUploadChunkPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "capture-upload-chunk"))

    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, CaptureUploadChunkPayload> = StreamCodec.composite(
      ByteBufCodecs.STRING_UTF8,
      CaptureUploadChunkPayload::uuid,
      ByteBufCodecs.BYTE_ARRAY,
      CaptureUploadChunkPayload::bytes,
    ) { uuid: String, bytes: ByteArray ->
      CaptureUploadChunkPayload(
        uuid,
        bytes,
      )
    }
  }
}

fun initializeCapture() {
  PayloadTypeRegistry.playS2C().register(CaptureInitPayload.TYPE, CaptureInitPayload.STREAM_CODEC)
  PayloadTypeRegistry.playC2S().register(CaptureUploadInitPayload.TYPE, CaptureUploadInitPayload.STREAM_CODEC)
  PayloadTypeRegistry.playC2S().register(CaptureUploadChunkPayload.TYPE, CaptureUploadChunkPayload.STREAM_CODEC)
  ClientPlayNetworking.registerGlobalReceiver(
    CaptureInitPayload.TYPE
  ) { payload: CaptureInitPayload, context: ClientPlayNetworking.Context ->
    val uuid = payload.uuid
    val nativeImage = Screenshot.takeScreenshot(Minecraft.getInstance().mainRenderTarget)
    val bytes = nativeImage.asByteArray()

    context.player().connection.send(
      ServerboundCustomPayloadPacket(
        CaptureUploadInitPayload(
          uuid,
          bytes.size,
        )
      )
    )

    val chunkSize = 1024 * 16
    for (i in bytes.indices step chunkSize) {
      val chunk = bytes.copyOfRange(i, (i + chunkSize).coerceAtMost(bytes.size))
      context.player().connection.send(
        ServerboundCustomPayloadPacket(
          CaptureUploadChunkPayload(
            uuid,
            chunk,
          )
        )
      )
    }
  }
}