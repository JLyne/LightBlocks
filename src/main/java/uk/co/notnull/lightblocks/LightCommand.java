package uk.co.notnull.lightblocks;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Light;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;
import org.jetbrains.annotations.NotNull;

public class LightCommand implements CommandExecutor {
	private final LightBlocks plugin;

	public LightCommand(LightBlocks plugin) {
		this.plugin = plugin;
	}

	public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
		if(!(sender instanceof Player player)) {
			sender.sendMessage("You must be a player!");
			return true;
		}

		if(args.length > 1) {
			player.sendMessage(Component.text(command.getUsage()).color(NamedTextColor.RED));
			return false;
		}

		int lightLevel = 15;

		if(args.length == 1) {
			try {
				lightLevel = Integer.parseInt(args[0]);
			} catch(NumberFormatException e) {
				player.sendMessage(Component.text(command.getUsage()).color(NamedTextColor.RED));
				return false;
			}
		}

		if(lightLevel < 0 || lightLevel > 15) {
			player.sendMessage(Component.text(command.getUsage()).color(NamedTextColor.RED));
			return false;
		}

		ItemStack item = new ItemStack(Material.LIGHT, 1);
		Light blockData = (Light) Material.LIGHT.createBlockData();
		blockData.setLevel(lightLevel);

		BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();
		meta.setBlockData(blockData);
		item.setItemMeta(meta);

		if(!player.getInventory().addItem(item).isEmpty()) {
			player.getWorld().dropItemNaturally(player.getLocation(), item);
		}

		player.sendMessage(Component.text("You have been given a Light block").color(NamedTextColor.GREEN));

		return true;
	}
}
