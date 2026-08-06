package io.github.anjoismysign.permissions.director;

import io.github.anjoismysign.bloblib.manager.GenericManagerDirector;
import io.github.anjoismysign.permissions.ProfilePermissions;
import io.github.anjoismysign.permissions.director.manager.ConfigurationManager;
import org.jetbrains.annotations.NotNull;

public class PermissionsManagerDirector extends GenericManagerDirector<ProfilePermissions> {

    public PermissionsManagerDirector(ProfilePermissions plugin) {
        super(plugin);
        addManager("ConfigManager",
                new ConfigurationManager(this));
    }

    /**
     * From top to bottom, follow the order.
     */
    @Override
    public void reload() {
        getConfigManager().reload();
    }

    @NotNull
    public final ConfigurationManager getConfigManager() {
        return getManager("ConfigManager", ConfigurationManager.class);
    }
}