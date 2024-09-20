package eu.mctraveler.mixin;

import eu.mctraveler.MCTravelerClientKt;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.numbers.NumberFormat;
import net.minecraft.world.scores.PlayerScoreEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Objects;

@Mixin(PlayerScoreEntry.class)
public class PlayerScoreEntryMixin {
    @Final
    @Shadow private NumberFormat numberFormatOverride;
    @Final
    @Shadow private int value;

    @Overwrite
    public MutableComponent formatValue(NumberFormat numberFormat) {
        if (!MCTravelerClientKt.isOnMcTraveler()) {
            return Objects.requireNonNullElse(this.numberFormatOverride, numberFormat).format(this.value);
        }

        return Component.literal("");
    }
}
