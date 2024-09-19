package eu.mctraveler

import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.BufferUploader
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.Tesselator
import com.mojang.blaze3d.vertex.VertexFormat
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.minecraft.client.renderer.GameRenderer
import net.minecraft.network.RegistryFriendlyByteBuf
import net.minecraft.network.codec.ByteBufCodecs
import net.minecraft.network.codec.StreamCodec
import net.minecraft.network.protocol.common.custom.CustomPacketPayload
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec3
import kotlin.math.max
import kotlin.math.min

class RegionBoundary(val startX: Int, val startY: Int, val startZ: Int, val endX: Int, val endY: Int, val endZ: Int) {
  companion object {
    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RegionBoundary> =
      object : StreamCodec<RegistryFriendlyByteBuf, RegionBoundary> {
        override fun encode(buf: RegistryFriendlyByteBuf, value: RegionBoundary) {
          ByteBufCodecs.INT.encode(buf, value.startX)
          ByteBufCodecs.INT.encode(buf, value.startY)
          ByteBufCodecs.INT.encode(buf, value.startZ)
          ByteBufCodecs.INT.encode(buf, value.endX)
          ByteBufCodecs.INT.encode(buf, value.endY)
          ByteBufCodecs.INT.encode(buf, value.endZ)
        }

        override fun decode(buf: RegistryFriendlyByteBuf): RegionBoundary {
          return RegionBoundary(
            ByteBufCodecs.INT.decode(buf),
            ByteBufCodecs.INT.decode(buf),
            ByteBufCodecs.INT.decode(buf),
            ByteBufCodecs.INT.decode(buf),
            ByteBufCodecs.INT.decode(buf),
            ByteBufCodecs.INT.decode(buf),
          )
        }
      }
  }
}

@JvmRecord
data class RegionBoundariesPayload(
  val regions: List<RegionBoundary>,
) : CustomPacketPayload {
  override fun type(): CustomPacketPayload.Type<RegionBoundariesPayload> {
    return TYPE
  }

  companion object {
    // We have a type that wraps the resource location.
    val TYPE: CustomPacketPayload.Type<RegionBoundariesPayload> =
      CustomPacketPayload.Type(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "region-boundaries"))

    // And we have a stream codec, here using RFBB (RegistryFriendlyByteBuf) because item stacks require it.
    val STREAM_CODEC: StreamCodec<RegistryFriendlyByteBuf, RegionBoundariesPayload> = StreamCodec.composite(
      RegionBoundary.STREAM_CODEC.apply(ByteBufCodecs.list()),
      RegionBoundariesPayload::regions,
    ) { regions: List<RegionBoundary> ->
      RegionBoundariesPayload(
        regions
      )
    }
  }
}

var regions: List<RegionBoundary> = mutableListOf()

fun isInRegion(location: Vec3, regionBoundary: RegionBoundary): Boolean {
  val xInRange = location.x.toInt() in min(regionBoundary.startX, regionBoundary.endX)..max(regionBoundary.startX, regionBoundary.endX)
  val zInRange = location.z.toInt() in min(regionBoundary.startZ, regionBoundary.endZ)..max(regionBoundary.startZ, regionBoundary.endZ)
  val yInRange = location.y.toInt() in min(regionBoundary.startY, regionBoundary.endY)..max(regionBoundary.startY, regionBoundary.endY)

  return xInRange && zInRange && yInRange
}

fun initializeRegionShow() {
  PayloadTypeRegistry.playS2C().register(RegionBoundariesPayload.TYPE, RegionBoundariesPayload.STREAM_CODEC)
  ClientPlayNetworking.registerGlobalReceiver(
    RegionBoundariesPayload.TYPE
  ) { payload: RegionBoundariesPayload, _: ClientPlayNetworking.Context ->
    regions = payload.regions.map { RegionBoundary(it.startX, it.startY, it.startZ, it.endX + 1, it.endY, it.endZ + 1) }
    logger.info("Received region boundaries ${regions.size}")
  }

  ClientPlayConnectionEvents.DISCONNECT.register { _, client ->
    regions = listOf()
  }

  WorldRenderEvents.LAST.register { context ->
    if (regions.isEmpty()) {
      return@register
    }

    context.matrixStack()!!.pushPose()
    context.matrixStack()!!
      .translate(-context.camera().position.x, -context.camera().position.y, -context.camera().position.z)
    val matrix = context.matrixStack()!!.last().pose()
    context.matrixStack()!!.popPose()

    val color = 0xFFFF0000.toInt()
    RenderSystem.enableDepthTest()
    val tessellator = Tesselator.getInstance()

    run {
      val buffer = tessellator.begin(
        VertexFormat.Mode.DEBUG_LINES,
        DefaultVertexFormat.POSITION_COLOR
      ) // USE DEBUG_LINE_STRIP FOR LINES

      for (region in regions) {
        // Bottom square (minY)
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-right

        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-right

        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-left

        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-left

        // Top square (region.endY.toFloat())
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-right

        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-right

        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-left

        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-left

        // Connect top and bottom squares (vertical lines)
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-left
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-left

        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-right

        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-right

        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-left
      }

      RenderSystem.setShader(GameRenderer::getRendertypeLinesShader)
      BufferUploader.drawWithShader(buffer.buildOrThrow())
    }

    run {
      val buffer = tessellator.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR)
      val camera = context.camera().position
      RenderSystem.disableCull()
      RenderSystem.enableBlend()

      // render all six faces as quads
      for (region in regions) {
        // if camera not inside region, skip
        if (!isInRegion(camera, region)) {
          continue
        }

        // Define the top square
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-left


        // Define bottom square
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-left

        // Define left square
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-left

        // Define right square
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-left

        // Define front square
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.startZ.toFloat())
          .setColor(color) // Bottom-left

        // Define back square
        buffer.addVertex(matrix, region.startX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-left
        buffer.addVertex(matrix, region.endX.toFloat(), region.startY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Top-right
        buffer.addVertex(matrix, region.endX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-right
        buffer.addVertex(matrix, region.startX.toFloat(), region.endY.toFloat(), region.endZ.toFloat())
          .setColor(color) // Bottom-left
      }

      val mesh = buffer.build() ?: return@run
      RenderSystem.setShaderColor(1f, 1f, 1f, 0.1f)
      RenderSystem.setShader(GameRenderer::getPositionColorShader)
      BufferUploader.drawWithShader(mesh)
      RenderSystem.enableCull()
      RenderSystem.disableBlend()
    }

    RenderSystem.disableDepthTest()
  }
}
