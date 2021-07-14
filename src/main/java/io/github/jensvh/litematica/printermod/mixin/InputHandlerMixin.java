package io.github.jensvh.litematica.printermod.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import fi.dy.masa.litematica.event.InputHandler;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import io.github.jensvh.litematica.printermod.LitematicaMixinMod;

@Mixin(value = InputHandler.class, remap = false)
public class InputHandlerMixin {
	
	@Redirect(method = "addHotkeys", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;"))
    private List<ConfigHotkey> moreHotkeys() {
        return LitematicaMixinMod.betterHotkeyList;
    }
	
	@Redirect(method = "addKeysToMap", at = @At(value = "FIELD", target = "Lfi/dy/masa/litematica/config/Hotkeys;HOTKEY_LIST:Ljava/util/List;"))
    private List<ConfigHotkey> moreeHotkeys() {
        return LitematicaMixinMod.betterHotkeyList;
    }

}
