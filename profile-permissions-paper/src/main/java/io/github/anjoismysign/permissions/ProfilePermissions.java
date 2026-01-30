package io.github.anjoismysign.permissions;

import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.cruder.ProfileCruder;
import io.github.anjoismysign.permissions.director.PermissionsManagerDirector;
import io.github.anjoismysign.permissions.entity.PermissionsAccount;
import io.github.anjoismysign.permissions.entity.PermissionsProfile;
import io.github.anjoismysign.permissions.entity.ProfilePermissionsAccount;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class ProfilePermissions extends BlobPlugin {

    private static ProfilePermissions INSTANCE;

    private PermissionsManagerDirector director;
    private ProfileCruder<PermissionsAccount, PermissionsProfile> profileCruder;

    public static ProfilePermissions getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        director = new PermissionsManagerDirector(this);
        Bukkit.getScheduler().runTask(this, () -> {
            profileCruder = new ProfileCruder<>(this, PermissionsAccount.class, PermissionsAccount::new);
        });
    }

    @Override
    public void onDisable() {
        super.onDisable();
        profileCruder.shutdown();
    }

    @NotNull
    public PermissionsManagerDirector getManagerDirector() {
        return director;
    }

    @NotNull
    public ProfileCruder<PermissionsAccount, PermissionsProfile> getProfileCruder() {
        return profileCruder;
    }

    @NotNull
    public ProfilePermissionsAccount getAccount(@NotNull Player player){
        return profileCruder.getAccount(player);
    }
}
