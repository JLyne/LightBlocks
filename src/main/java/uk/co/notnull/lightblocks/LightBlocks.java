package uk.co.notnull.lightblocks;

import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@SuppressWarnings("UnstableApiUsage")
public final class LightBlocks extends JavaPlugin implements Listener {
	private GriefPreventionHandler griefPreventionHandler;
	private WorldGuardHandler worldGuardHandler;

	@Override
	public void onEnable() {
		getServer().getPluginManager().registerEvents(this, this);

		LifecycleEventManager<Plugin> manager = getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> new LightCommand(event.registrar()));

		Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> {
			for (Player player : Bukkit.getOnlinePlayers()) {
				if (player.getGameMode() != GameMode.CREATIVE && holdingLightBlockTool(player)) {
					highlightLightBlocks(player);
				}
			}
		}, 0L, 20L);
	}

		@EventHandler
	public void onPluginEnable(PluginEnableEvent event) {
		switch(event.getPlugin().getName()) {
			case "GriefPrevention":
				getLogger().info("Initialising GriefPrevention handler");
				griefPreventionHandler = new GriefPreventionHandler();
				break;

			case "WorldGuard":
				getLogger().info("Initialising WorldGuard handler");
				worldGuardHandler = new WorldGuardHandler();
				break;
		}
	}

	@EventHandler
	public void onPluginDisable(PluginDisableEvent event) {
		switch(event.getPlugin().getName()) {
			case "GriefPrevention":
				if(griefPreventionHandler != null) {
					getLogger().info("Disabling GriefPrevention handler");
					griefPreventionHandler = null;
				}
				break;

			case "WorldGuard":
				if(worldGuardHandler != null) {
					getLogger().info("Disabling WorldGuard handler");
					worldGuardHandler = null;
				}
				break;
		}
	}

	@EventHandler()
	public void onLeftClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.LEFT_CLICK_BLOCK && event.getAction() != Action.LEFT_CLICK_AIR) {
			return;
		}

		if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.LIGHT) {
			return;
		}

		if (!canBreak(block, event.getPlayer())) {
			return;
		}

		ItemStack item = event.getItem();
		Location location = block.getLocation();

		block.breakNaturally(item != null ? item : new ItemStack(Material.LIGHT, 1), true);
		location.getWorld().dropItemNaturally(location, new ItemStack(Material.LIGHT, 1));

		event.setCancelled(true);
	}

	@EventHandler()
	public void onRightClick(PlayerInteractEvent event) {
		if (event.getAction() != Action.RIGHT_CLICK_BLOCK && event.getAction() != Action.RIGHT_CLICK_AIR) {
			return;
		}

		Block block = event.getClickedBlock();

		if (block == null || block.getType() != Material.LIGHT) {
			return;
		}

		if(!worldGuardHandler.checkPermission(block.getLocation(), event.getPlayer())) {
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setUseItemInHand(Event.Result.DENY);
			return;
		}

		if(!griefPreventionHandler.checkPermission(block.getLocation(), event.getPlayer(), event)) {
			event.setUseInteractedBlock(Event.Result.DENY);
			event.setUseItemInHand(Event.Result.DENY);
		}

		Light blockData = (Light) event.getClickedBlock().getBlockData();
		int level = blockData.getLevel();
		blockData.setLevel(level == blockData.getMaximumLevel() ? blockData.getMinimumLevel() : level + 1);
		event.getClickedBlock().setBlockData(blockData);
	}

	@EventHandler(ignoreCancelled = true)
	private void onBlockPlace(BlockPlaceEvent event) {
		Block placed = event.getBlockPlaced();

		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && placed.getType() == Material.LIGHT) {
			event.getPlayer().spawnParticle(Particle.BLOCK_MARKER, placed.getLocation().add(0.5, 0.5, 0.5), 1, placed.getBlockData());
		}
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	private void onBlockPlaced(BlockPlaceEvent event) {
		if (event.getPlayer().getGameMode() != GameMode.CREATIVE && event.getBlockReplacedState().getType() == Material.LIGHT) {
			Location location = event.getBlock().getLocation();
			location.getWorld().dropItemNaturally(location, new ItemStack(Material.LIGHT, 1));
		}
	}

	private List<Location> getLightBlocksAroundPlayer(Player player) {
		World world = player.getWorld();
		List<Location> locations = new ArrayList<>();

		Location playerLocation = player.getLocation();
		int baseX = playerLocation.getBlockX();
		int baseY = playerLocation.getBlockY();
		int baseZ = playerLocation.getBlockZ();

		for (int x = baseX - 16; x <= baseX + 16; x++) {
			for (int z = baseZ - 16; z <= baseZ + 16; z++) {
				int minY = Math.max(baseY - 16, world.getMinHeight());
				int maxY = Math.min(baseY + 16, world.getMaxHeight());

				for (int y = minY; y <= maxY; y++) {
					Block block = world.getBlockAt(x, y, z);

					if (block.getType() == Material.LIGHT) {
						locations.add(block.getLocation().add(0.5, 0.5, 0.5));
					}
				}
			}
		}

		return locations;
	}

	private void highlightLightBlocks(Player player) {
		for (Location location : getLightBlocksAroundPlayer(player)) {
			player.spawnParticle(Particle.BLOCK_MARKER, location, 1, location.getBlock().getBlockData());
		}
	}

	private boolean canBreak(Block block, Player player) {
		BlockBreakEvent event = new BlockBreakEvent(block, player);
		Bukkit.getPluginManager().callEvent(event);

		return !event.isCancelled();
	}

	private boolean holdingLightBlockTool(Player player) {
		return player.getEquipment().getItemInMainHand().getType() == Material.LIGHT
				|| player.getEquipment().getItemInOffHand().getType() == Material.LIGHT;
	}
}
