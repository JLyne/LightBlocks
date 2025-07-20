package uk.co.notnull.lightblocks;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.block.data.type.Light;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockDataMeta;

import static com.mojang.brigadier.arguments.IntegerArgumentType.integer;
import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;

@SuppressWarnings("UnstableApiUsage")
public final class LightCommand {
	public LightCommand(Commands commands) {
		commands.register(
				literal("light")
						.requires(source -> source.getSender() instanceof Player
								&& source.getSender().hasPermission("light.give"))
						.executes(ctx -> onGive((Player) ctx.getSource().getSender(), 15))
						.then(argument("level", integer(0, 15))
									  .executes(ctx -> onGive((Player) ctx.getSource().getSender(),
															  ctx.getArgument("level", Integer.class))))
						.build(),
				"Gives you a light block of the specified level");
	}

	public int onGive(Player sender, int level) {
		ItemStack item = ItemStack.of(Material.LIGHT);
		Light blockData = (Light) Material.LIGHT.createBlockData();
		blockData.setLevel(level);

		BlockDataMeta meta = (BlockDataMeta) item.getItemMeta();
		meta.setBlockData(blockData);
		item.setItemMeta(meta);

		if(!sender.getInventory().addItem(item).isEmpty()) {
			sender.getWorld().dropItemNaturally(sender.getLocation(), item);
		}

		sender.sendMessage(Component.text("You have been given a Light block").color(NamedTextColor.GREEN));

		return Command.SINGLE_SUCCESS;
	}
}
