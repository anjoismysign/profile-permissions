package io.github.anjoismysign.permissions.entity;

import com.google.common.collect.Maps;
import io.github.anjoismysign.bloblib.entities.Cleanable;
import io.github.anjoismysign.bloblib.entities.PlayerDecorator;
import io.github.anjoismysign.bloblib.entities.PlayerDecoratorAware;
import io.github.anjoismysign.permissions.ProfilePermissions;
import io.github.anjoismysign.permissions.event.PermissionsAssignEvent;
import io.github.anjoismysign.psa.crud.Crudable;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public final class PermissionsProfile implements Crudable, Cleanable, PlayerDecoratorAware, ProfilePermissionsAccount {
    private final String identification;
    private final Map<String, Boolean> permissions;
    private transient PlayerDecorator playerDecorator;
    private transient PermissionAttachment attachment;

    public PermissionsProfile(String identification) {
        this.identification = identification;
        this.permissions = Maps.newHashMap();
    }

    @Override
    public void setPermission(String permission, boolean value) {
        permissions.put(permission, value);
        if (attachment == null) {
            return;
        }
        attachment.setPermission(permission, value);
    }

    @Override
    public void unsetPermission(String permission) {
        permissions.remove(permission);
        if (attachment == null) {
            return;
        }
        attachment.unsetPermission(permission);
    }

    @Override
    public String getIdentification() {
        return identification;
    }

    @Override
    public void cleanup() {
        Runnable syncRunnable = () -> {
            if (!playerDecorator.isValid()){
                return;
            }
            @Nullable var player = player();
            if (player != null){
                attachment = player.addAttachment(ProfilePermissions.getInstance());
            }
        };
        if (Bukkit.isPrimaryThread()){
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(ProfilePermissions.getInstance(), syncRunnable);
        }
    }

    @Override
    public void setPlayerDecorator(@NotNull PlayerDecorator playerDecorator) {
        Runnable syncRunnable = () -> {
          if (!playerDecorator.isValid()){
              return;
          }
            @Nullable var player = player();
            if (player != null){
                attachment = player.addAttachment(ProfilePermissions.getInstance());
                permissions.forEach(attachment::setPermission);
                PermissionsAssignEvent event = new PermissionsAssignEvent(player);
                Bukkit.getPluginManager().callEvent(event);
            }
        };
        if (Bukkit.isPrimaryThread()){
            syncRunnable.run();
        } else {
            Bukkit.getScheduler().runTask(ProfilePermissions.getInstance(), syncRunnable);
        }
        if (this.playerDecorator != null){
            return;
        }
        this.playerDecorator = playerDecorator;
    }

    @Nullable
    public Player player(){
        return playerDecorator == null ? null : playerDecorator.lookup();
    }
}
