package eu.mctraveler.mixin;

import eu.mctraveler.McTravelerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.mctraveler.Auto_loginKt.setDidAutoJoin;
import static eu.mctraveler.ConfigKt.getConfig;
import static eu.mctraveler.UtilKt.joinMcTraveler;

@Mixin(TitleScreen.class)
public class PlayMcTravelerButtonMixin extends Screen {
    protected PlayMcTravelerButtonMixin(Component component) {
        super(component);
    }

    @Inject(at = @At("RETURN"), method = "createNormalMenuOptions")
    private void createNormalMenuOptions(int i, int j, CallbackInfo ci) {
        this.addRenderableWidget(new ImageButton(this.width / 2 + 104, i + j, 20, 20, 0, 106, 20, new ResourceLocation("mctraveler-client", "textures/gui/widgets.png"), 256, 256, (button) -> {
            assert this.minecraft != null;
            joinMcTraveler(this, this.minecraft);
        }, Component.literal("Join MCTraveler")));

        McTravelerConfig config = getConfig();
        if (config == null || !config.isAutoJoinEnabled) {
            return;
        }

        Minecraft instance = Minecraft.getInstance();
        setDidAutoJoin(true);
        joinMcTraveler(this, instance);
    }
}
