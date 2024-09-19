package eu.mctraveler.mixin;

import eu.mctraveler.McTravelerConfigModel;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.mctraveler.Auto_loginKt.setDidAutoJoin;
import static eu.mctraveler.ConfigKt.getConfig;
import static eu.mctraveler.UtilKt.joinMcTraveler;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Shadow
    @Nullable
    public Screen screen;

    @Shadow
    public static Minecraft getInstance() {
        return null;
    }

    @Inject(method = "onGameLoadFinished", at = @At("HEAD"))
    private void onGameLoadFinished(CallbackInfo ci) {
        McTravelerConfigModel config = getConfig();
        if (config == null || !config.isAutoJoinEnabled) {
            return;
        }

        assert this.screen != null;
        Minecraft instance = getInstance();
        assert instance != null;
        setDidAutoJoin(true);
        joinMcTraveler(this.screen, instance);
    }
}
