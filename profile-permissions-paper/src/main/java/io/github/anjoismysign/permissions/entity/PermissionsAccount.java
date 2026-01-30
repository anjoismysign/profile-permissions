package io.github.anjoismysign.permissions.entity;

import com.google.common.collect.Maps;
import io.github.anjoismysign.bloblib.api.BlobLibProfileAPI;
import io.github.anjoismysign.bloblib.entities.AccountCrudable;
import io.github.anjoismysign.bloblib.entities.PlayerDecorator;
import io.github.anjoismysign.bloblib.managers.cruder.ProfileCruder;
import io.github.anjoismysign.permissions.ProfilePermissions;
import io.github.anjoismysign.psa.PostLoadable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class PermissionsAccount implements AccountCrudable<PermissionsProfile>, PostLoadable, ProfilePermissionsAccount{
    private transient @NotNull ProfilePermissions plugin;

    private final @NotNull String identification;
    private final @NotNull List<PermissionsProfile> profiles;
    private int currentProfileIndex;

    @SuppressWarnings("NotNullFieldNotInitialized")
    private transient @NotNull PermissionAttachment currentAttachment;
    private transient @NotNull PlayerDecorator playerDecorator;

    public PermissionsAccount(@NotNull String identification) {
        this.identification = identification;
        this.profiles = new ArrayList<>();
        onPostLoad();
    }

    @Override
    public void onPostLoad() {
        this.plugin = ProfilePermissions.getInstance();
        @NotNull ProfileCruder<PermissionsAccount, PermissionsProfile> manager = plugin.getProfileCruder();
        this.playerDecorator = manager.assignPlayerDecorator(identification);
        if (!profiles.isEmpty()) {
            if (currentProfileIndex < 0 || currentProfileIndex >= profiles.size()) {
                currentProfileIndex = 0;
            }
            PermissionsProfile profile = profiles.get(currentProfileIndex);
            switchToProfile(profile.getIdentification());
        } else {
            var profileAPI = BlobLibProfileAPI.getInstance();
            var provider = profileAPI.getProvider();
            var profileManagement = provider.getProfileManagement(UUID.fromString(identification));
            if (profileManagement == null) {
                return;
            }
            var profile = profileManagement.getProfiles().get(0);
            createProfile(profile.getIdentification(), true);
        }
    }

    public void createProfile(String identification,
                              boolean switchTo) {
        PermissionsProfile profile = new PermissionsProfile(identification, Maps.newHashMap());
        this.profiles.add(profile);
        if (!switchTo) {
            return;
        }
        switchToProfile(profile.getIdentification());
    }

    public void switchToProfile(String identification) {
        @Nullable PermissionsProfile profile = profiles.stream().filter(permissionsProfile -> permissionsProfile.getIdentification().equals(identification)).findFirst().orElse(null);
        if (profile == null){
            return;
        }
        Runnable syncRunnable = () -> {
            if (!playerDecorator.isValid()){
                return;
            }
            Player player = Objects.requireNonNull(playerDecorator.address().look(), "Player is not cached");
            try {
                player.removeAttachment(currentAttachment);
            } catch (IllegalArgumentException ignored){
            }
            currentAttachment = playerDecorator.getPermissible().addAttachment(plugin);
            var permissions = profile.getPermissions();
            permissions.forEach(currentAttachment::setPermission);
            this.currentProfileIndex = profiles.indexOf(profile);
        };
        if (Bukkit.isPrimaryThread()){
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(plugin, syncRunnable);
        }
    }

    @Override
    public @NotNull String getIdentification() {
        return identification;
    }

    public @NotNull List<PermissionsProfile> getProfiles() {
        return profiles;
    }

    @Override
    public void setPermission(String permission, boolean value) {
        var profile = getProfiles().get(currentProfileIndex);
        var permissions = profile.getPermissions();
        permissions.put(permission, value);
        //noinspection ConstantValue
        if (currentAttachment == null){
            return;
        }
        currentAttachment.setPermission(permission,value);
    }

    @Override
    public void unsetPermission(String permission) {
        var profile = getProfiles().get(currentProfileIndex);
        var permissions = profile.getPermissions();
        permissions.remove(permission);
        //noinspection ConstantValue
        if (currentAttachment == null){
            return;
        }
        currentAttachment.unsetPermission(permission);
    }
}
