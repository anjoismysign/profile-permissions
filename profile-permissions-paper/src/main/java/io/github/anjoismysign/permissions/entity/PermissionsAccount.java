package io.github.anjoismysign.permissions.entity;

import io.github.anjoismysign.bloblib.storage.AccountCrudable;

public class PermissionsAccount extends AccountCrudable<PermissionsProfile> {
    public PermissionsAccount(String identification){
        super(identification);
    }
}
