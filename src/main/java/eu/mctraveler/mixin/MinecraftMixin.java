package eu.mctraveler.mixin;

import eu.mctraveler.McTravelerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static eu.mctraveler.Auto_loginKt.setDidAutoJoin;
import static eu.mctraveler.ConfigKt.getConfig;
import static eu.mctraveler.UtilKt.joinMcTraveler;

@Mixin(TitleScreen.class)
public abstract class MinecraftMixin {
    @Inject(method = "", at = @At("RETURN"))
    private void onGameLoadFinished(CallbackInfo ci) {

    }
}
