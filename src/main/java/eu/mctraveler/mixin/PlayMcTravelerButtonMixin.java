package eu.mctraveler.mixin;

import net.minecraft.client.gui.components.SpriteIconButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.mctraveler.UtilKt.joinMcTraveler;

@Mixin(TitleScreen.class)
public class PlayMcTravelerButtonMixin extends Screen {
    protected PlayMcTravelerButtonMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("RETURN"), method = "createNormalMenuOptions")
    private void createNormalMenuOptions(int i, int j, CallbackInfo ci) {
        SpriteIconButton spriteIconButton = this.addRenderableWidget(SpriteIconButton.builder(Component.literal("Join MCTraveler"), (button) -> {
            assert this.minecraft != null;
            joinMcTraveler(this, this.minecraft);
        }, true).width(20).sprite(ResourceLocation.fromNamespaceAndPath("mctraveler-client", "icon/mctraveler"), 15, 15).build());

        spriteIconButton.setPosition(this.width / 2 + 104, i + j);
    }
}
