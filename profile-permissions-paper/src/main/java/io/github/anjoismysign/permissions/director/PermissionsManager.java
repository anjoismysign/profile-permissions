package io.github.anjoismysign.permissions.director;

import io.github.anjoismysign.bloblib.entities.GenericManager;
import io.github.anjoismysign.permissions.ProfilePermissions;

public class PermissionsManager extends GenericManager<ProfilePermissions, PermissionsManagerDirector> {
    public PermissionsManager(PermissionsManagerDirector managerDirector) {
        super(managerDirector);
    }
}