package eu.mctraveler.mixin;

import net.minecraft.client.User;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(User.class)
public class UserMixin {
    @Final
    @Shadow private String accessToken;

    @Overwrite
    public String getAccessToken() {
        String accessToken = System.getenv("MCTRAVELER_ACCESS_TOKEN");
        if (accessToken == null) {
            accessToken = this.accessToken;
        }

        return accessToken;
    }
}
