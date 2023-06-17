package uk.co.notnull.lightblocks;

import me.ryanhamshire.GriefPrevention.Claim;
import me.ryanhamshire.GriefPrevention.ClaimPermission;
import me.ryanhamshire.GriefPrevention.GriefPrevention;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

import java.util.function.Supplier;

public class GriefPreventionHandler {
	private final GriefPrevention griefPrevention;
	private final boolean gpEnabled;

	public GriefPreventionHandler() {
		griefPrevention = (GriefPrevention) Bukkit.getPluginManager().getPlugin("GriefPrevention");
		gpEnabled = griefPrevention != null;
	}

	public boolean checkPermission(Location location, Player player, Event event) {
		if(!gpEnabled) {
			return true;
		}

		Claim claim = griefPrevention.dataStore.getClaimAt(location, false, null);

		if(claim == null) {
			return true;
		}

		Supplier<String> message = claim.checkPermission(player, ClaimPermission.Build, event);

		if(message != null) {
			player.sendMessage(Component.text(message.get()).color(NamedTextColor.RED));

			return false;
		}

		return true;
	}
}
