package zombieinfection;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.RegisterCommandsEvent;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

@Mod.EventBusSubscriber
public class InfectCommand {
	@SubscribeEvent
	public static void registerCommands(RegisterCommandsEvent event) {
		LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("infection");
		for (ZombieType type : ZombieType.values()) {
			command.requires((user) -> {
				return user.hasPermission(2);
			}).then(Commands.literal("set").then(Commands.literal(type.getName()).executes((arguemnts) -> {
				if (arguemnts.getSource().isPlayer()) {
					ServerPlayer user = arguemnts.getSource().getPlayer();
					if (user instanceof Infectable infectable) {
						if (infectable.getInfection() == type)
							return 0;

						infectable.setInfection(type);
						arguemnts.getSource().sendSuccess(() -> Component.translatable("command.infect.success.self", type.getName()), true);
						return 15;
					}

					arguemnts.getSource().sendFailure(Component.translatable("command.infect.fail", user.getDisplayName()));
				}
				return 0;
			})));
			
			command.requires((user) -> {
				return user.hasPermission(2);
			}).then(Commands.literal("set").then(Commands.literal(type.getName()).then(Commands.argument("targets", EntityArgument.players()).executes((arguemnts) -> {
				for(ServerPlayer target : EntityArgument.getPlayers(arguemnts, "targets")) {
					if (target instanceof Infectable infectable) {
						if (infectable.getInfection() == type)
							return 0;

						infectable.setInfection(type);
						arguemnts.getSource().sendSuccess(() -> Component.translatable("command.infect.success.other", target.getDisplayName(), type.getName()), true);
						return 15;
					}

					arguemnts.getSource().sendFailure(Component.translatable("command.infect.fail", target.getDisplayName()));
				}
				
				return 0;
			}))));
		}

		event.getDispatcher().register(command);
	}
}
