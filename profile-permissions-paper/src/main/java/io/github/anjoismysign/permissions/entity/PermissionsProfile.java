package io.github.anjoismysign.permissions.entity;

import io.github.anjoismysign.psa.crud.Crudable;

import java.util.Map;

public record PermissionsProfile(String getIdentification,
                                 Map<String, Boolean> getPermissions) implements Crudable {
}
