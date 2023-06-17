package uk.co.notnull.lightblocks;

import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.LocalPlayer;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.Flags;
import com.sk89q.worldguard.protection.regions.RegionContainer;
import com.sk89q.worldguard.protection.regions.RegionQuery;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class WorldGuardHandler {
	private final boolean wgEnabled;
	private final RegionQuery query;

	public WorldGuardHandler() {
		WorldGuardPlugin worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
		wgEnabled = worldGuard != null;
		RegionContainer container = WorldGuard.getInstance().getPlatform().getRegionContainer();
		query = container.createQuery();
	}

	public boolean checkPermission(Location location, Player player) {
		if(!wgEnabled) {
			return true;
		}

		LocalPlayer localPlayer = WorldGuardPlugin.inst().wrapPlayer(player);

		return query.testState(BukkitAdapter.adapt(location), localPlayer, Flags.BUILD);
	}
}
