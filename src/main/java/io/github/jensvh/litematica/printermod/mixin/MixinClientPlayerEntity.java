package io.github.jensvh.litematica.printermod.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.authlib.GameProfile;

import fi.dy.masa.litematica.world.SchematicWorldHandler;
import io.github.jensvh.litematica.printermod.LitematicaMixinMod;
import io.github.jensvh.litematica.printermod.printer.Printer;
import io.github.jensvh.litematica.printermod.printer.UpdateChecker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.network.MessageType;
import net.minecraft.text.LiteralText;

@Mixin(ClientPlayerEntity.class)
public class MixinClientPlayerEntity extends AbstractClientPlayerEntity {
	boolean didCheckForUpdates = false;

    public MixinClientPlayerEntity(ClientWorld world, GameProfile profile) {
        super(world, profile);
	}

    @Shadow
	protected MinecraftClient client;

	protected Printer printer;

	@Inject(at = @At("RETURN"), method = "isCamera", cancellable = true)
	protected void isCamera(CallbackInfoReturnable<Boolean> cir) {
		if (printer != null && printer.lockCamera) {
			cir.setReturnValue(false); // Fix for placing correctly pistons for example
		}
	}

	@Inject(at = @At("HEAD"), method = "tick")
	public void tick(CallbackInfo ci) {
		if (!didCheckForUpdates) {
			didCheckForUpdates = true;

			checkForUpdates();
		}

		if (printer == null) {
			if (client != null && client.player != null && client.world != null) {
				printer = new Printer(client, client.player, client.world);
			}

			return;
		}

		if (SchematicWorldHandler.getSchematicWorld() == null || !LitematicaMixinMod.PRINT_MODE.getBooleanValue()) return;

		printer.print();
	}

	public void checkForUpdates() {
        new Thread(() -> {
            String version = UpdateChecker.version;
            String newVersion = UpdateChecker.getPrinterVersion();

            if (!version.equals(newVersion)) {
                client.inGameHud.addChatMessage(MessageType.SYSTEM,
                        new LiteralText("New version of Litematica Printer available in https://github.com/aleksilassila/litematica-printer/releases"),
                        null);
            }
        }).start();
	}
}