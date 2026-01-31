package io.github.anjoismysign.permissions;

import io.github.anjoismysign.bloblib.api.BlobLibMessageAPI;
import io.github.anjoismysign.bloblib.managers.BlobPlugin;
import io.github.anjoismysign.bloblib.managers.cruder.AccountCruder;
import io.github.anjoismysign.permissions.director.PermissionsManagerDirector;
import io.github.anjoismysign.permissions.entity.PermissionsAccount;
import io.github.anjoismysign.permissions.entity.PermissionsProfile;
import io.github.anjoismysign.permissions.entity.ProfilePermissionsAccount;
import io.github.anjoismysign.skeramidcommands.command.Command;
import io.github.anjoismysign.skeramidcommands.command.CommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.BukkitCommandTarget;
import io.github.anjoismysign.skeramidcommands.commandtarget.LogicCommandParameters;
import io.github.anjoismysign.skeramidcommands.server.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ProfilePermissions extends BlobPlugin {

    private static ProfilePermissions INSTANCE;

    private PermissionsManagerDirector director;
    private AccountCruder<PermissionsAccount, PermissionsProfile> accountCruder;

    public static ProfilePermissions getInstance() {
        return INSTANCE;
    }

    @Override
    public void onEnable() {
        INSTANCE = this;
        director = new PermissionsManagerDirector(this);
        Bukkit.getScheduler().runTask(this, () -> {
            accountCruder = new AccountCruder<>(this, PermissionsAccount.class, PermissionsProfile.class);
        });
        Command mainCommand = BukkitAdapter.getInstance().ofBukkitCommand("profilepermissions");
        Command setCommand = mainCommand.child("set");
        var onlinePlayers = BukkitCommandTarget.ONLINE_PLAYERS();
        var bool = LogicCommandParameters.BOOLEAN();
        setCommand.setParameters(
                onlinePlayers,
                new CommandTarget<String>() {
                    private void resolveRecursive(Permission perm, Set<String> accumulator) {
                        if (!accumulator.add(perm.getName())) {
                            return;
                        }
                        perm.getChildren().keySet().forEach(childName -> {
                            Permission childPerm = Bukkit.getServer().getPluginManager().getPermission(childName);
                            if (childPerm != null) {
                                resolveRecursive(childPerm, accumulator);
                            } else {
                                accumulator.add(childName);
                            }
                        });
                    }

                    @Override
                    public List<String> get() {
                        Set<String> allNodes = new HashSet<>();
                        for (Permission perm : Bukkit.getServer().getPluginManager().getPermissions()) {
                            resolveRecursive(perm, allNodes);
                        }
                        return new ArrayList<>(allNodes);
                    }

                    @Override
                    public @Nullable String parse(String s) {
                        return s;
                    }
                },
                bool);
        setCommand.onExecute(((permissionMessenger, args) -> {
            CommandSender sender = BukkitAdapter.getInstance().of(permissionMessenger);
            Player player = onlinePlayers.parse(args[0]);
            if (player == null) {
                BlobLibMessageAPI.getInstance()
                        .getMessage("Player.Not-Found", sender)
                        .toCommandSender(sender);
                return;
            }
            String permission = args[1];
            boolean value = Boolean.TRUE.equals(bool.parse(args[2]));
            var account = getAccount(player);
            if (account == null){
                return;
            }
            account.setPermission(permission, value);
        }));
    }

    @Override
    public void onDisable() {
        super.onDisable();
        accountCruder.shutdown();
    }

    @NotNull
    public PermissionsManagerDirector getManagerDirector() {
        return director;
    }

    @NotNull
    public AccountCruder<PermissionsAccount, PermissionsProfile> getAccountCruder() {
        return accountCruder;
    }

    @Nullable
    public ProfilePermissionsAccount getAccount(@NotNull Player player) {
        return accountCruder.getAccount(player);
    }
}
