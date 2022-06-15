package me.flashyreese.mods.commandaliases.mixin;

import me.flashyreese.mods.commandaliases.command.CommandManagerExtended;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(CommandManager.class)
public class MixinCommandManager implements CommandManagerExtended {

    @Unique
    private CommandManager.RegistrationEnvironment environment;

    @Unique
    private CommandRegistryAccess commandRegistryAccess;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void clint(CommandManager.RegistrationEnvironment environment, CommandRegistryAccess commandRegistryAccess, CallbackInfo ci) {
        this.environment = environment;
        this.commandRegistryAccess = commandRegistryAccess;
    }

    @Override
    public CommandManager.RegistrationEnvironment getEnvironment() {
        return this.environment;
    }

    @Override
    public CommandRegistryAccess getCommandRegistryAccess() {
        return this.commandRegistryAccess;
    }
}
