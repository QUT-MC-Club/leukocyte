package xyz.nucleoid.leukocyte.roles;

import dev.gegy.roles.api.PlayerRolesApi;
import dev.gegy.roles.api.Role;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;

import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;

import java.util.Collection;
import java.util.Comparator;

import java.util.stream.Stream;

public interface RoleAccessor {
    RoleAccessor INSTANCE = FabricLoader.getInstance().isModLoaded("player_roles") ? new PlayerRoles() :
            FabricLoader.getInstance().isModLoaded("luckperms") ? new LPGroups() : new None();

    Stream<String> getAllRoles();

    boolean hasRole(ServerPlayerEntity player, String role);

    final class None implements RoleAccessor {
        None() {
        }

        @Override
        public Stream<String> getAllRoles() {
            return Stream.empty();
        }

        @Override
        public boolean hasRole(ServerPlayerEntity player, String role) {
            return false;
        }
    }

    final class PlayerRoles implements RoleAccessor {
        PlayerRoles() {
        }

        @Override
        public Stream<String> getAllRoles() {
            return PlayerRolesApi.provider().stream().map(Role::getId);
        }

        @Override
        public boolean hasRole(ServerPlayerEntity player, String roleId) {
            var role = PlayerRolesApi.provider().get(roleId);
            if (role != null) {
                return PlayerRolesApi.lookup().byPlayer(player).has(role);
            } else {
                return false;
            }
        }
    }
    
    final class LPGroups implements RoleAccessor {
        LPGroups() {
        }

        @Override
        public Stream<String> getAllRoles() {
        Collection<Group> groups = LuckPermsProvider.get().getGroupManager().getLoadedGroups();

        return groups.stream()
        .sorted(Comparator.comparingInt(group -> group.getWeight().orElse(0)))
        .map(Group::getName);
        }

        @Override
        public boolean hasRole(ServerPlayerEntity player, String roleId) {
        return PermissionAccessor.INSTANCE.hasPermission(player, "group." + roleId);
        }
    }
}

