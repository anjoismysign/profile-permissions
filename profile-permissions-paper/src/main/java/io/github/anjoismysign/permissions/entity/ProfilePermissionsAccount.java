package io.github.anjoismysign.permissions.entity;

public interface ProfilePermissionsAccount {

    void setPermission(String permission, boolean value);

    void unsetPermission(String permission);

}
