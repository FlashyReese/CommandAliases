package me.flashyreese.mods.commandaliases.mixin;

import me.flashyreese.mods.commandaliases.command.CommandManagerExtended;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Commands.class)
public class MixinCommandManager implements CommandManagerExtended {

    @Unique
    private Commands.CommandSelection environment;

    @Unique
    private CommandBuildContext commandRegistryAccess;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void clint(Commands.CommandSelection environment, CommandBuildContext commandRegistryAccess, CallbackInfo ci) {
        this.environment = environment;
        this.commandRegistryAccess = commandRegistryAccess;
    }

    @Override
    public Commands.CommandSelection getEnvironment() {
        return this.environment;
    }

    @Override
    public CommandBuildContext getCommandRegistryAccess() {
        return this.commandRegistryAccess;
    }
}
