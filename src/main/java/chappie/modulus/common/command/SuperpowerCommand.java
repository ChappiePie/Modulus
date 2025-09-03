package chappie.modulus.common.command;

import chappie.modulus.common.ability.base.Superpower;
import chappie.modulus.common.capability.PowerCap;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.Collection;

public class SuperpowerCommand {
    public static final DynamicCommandExceptionType DIDNT_EXIST = new DynamicCommandExceptionType((object) -> Component.translatable("commands.modulus.DidntExist", object));
    private static final SuggestionProvider<CommandSourceStack> SUGGEST_SUPERPOWERS = (context, builder) -> SharedSuggestionProvider.suggestResource(Superpower.REGISTRY.keySet(), builder);

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("superpower").requires((player) -> player.hasPermission(2))
                .then(Commands.argument("targets", EntityArgument.entities())
                        .then(Commands.argument("superpower", ResourceLocationArgument.id()).suggests(SUGGEST_SUPERPOWERS)
                                .executes((c) -> setSuperpower(c.getSource(), EntityArgument.getEntities(c, "targets"), getSuperpower(c, "superpower"))))
                        .then(Commands.literal("remove").executes(c -> removeSuperpower(c.getSource(), EntityArgument.getEntities(c, "targets"))))
                )
        );
    }

    private static int setSuperpower(CommandSourceStack commandSource, Collection<? extends Entity> entities, Superpower superpower) {
        int i = 0;
        for (Entity entity : entities) {
            var cap = PowerCap.getCap(entity);
            if (cap != null) {
                cap.setSuperpower(superpower);
                i++;
            }
        }
        if (i == 1)
            commandSource.sendSuccess(() -> Component.translatable("commands.modulus.superpower.set.single", superpower.getDisplayName(), (entities.iterator().next()).getDisplayName()), true);
        else {
            int finalI = i;
            commandSource.sendSuccess(() -> Component.translatable("commands.modulus.superpower.set.multiple", superpower.getDisplayName(), finalI), true);
        }
        return entities.size();
    }

    private static int removeSuperpower(CommandSourceStack commandSource, Collection<? extends Entity> entities) {
        int i = 0;
        for (Entity entity : entities) {
            var cap = PowerCap.getCap(entity);
            if (cap != null) {
                cap.setSuperpower(null);
                i++;
            }
        }
        if (i == 1)
            commandSource.sendSuccess(() -> Component.translatable("commands.modulus.superpower.removed", (entities.iterator().next()).getDisplayName()), true);
        else {
            int finalI = i;
            commandSource.sendSuccess(() -> Component.translatable("commands.modulus.superpower.removed.multiple", finalI), true);
        }
        return entities.size();
    }

    public static Superpower getSuperpower(CommandContext<CommandSourceStack> context, String key) throws CommandSyntaxException {
        ResourceLocation id = context.getArgument(key, ResourceLocation.class);
        Superpower superpower = Superpower.REGISTRY.get(id);
        if (superpower == null) {
            throw DIDNT_EXIST.create(id);
        } else {
            return superpower;
        }
    }
}