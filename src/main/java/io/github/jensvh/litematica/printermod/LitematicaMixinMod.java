package io.github.jensvh.litematica.printermod;

import java.util.List;

import com.google.common.collect.ImmutableList;
import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.config.Hotkeys;
import fi.dy.masa.malilib.config.IConfigBase;
import fi.dy.masa.malilib.config.options.ConfigBoolean;
import fi.dy.masa.malilib.config.options.ConfigDouble;
import fi.dy.masa.malilib.config.options.ConfigHotkey;
import fi.dy.masa.malilib.config.options.ConfigInteger;
import fi.dy.masa.malilib.hotkeys.KeyCallbackToggleBooleanConfigWithMessage;
import net.fabricmc.api.ModInitializer;

public class LitematicaMixinMod implements ModInitializer {

	// Config settings
	public static final ConfigDouble        PRINTING_DELAY          = new ConfigDouble(     "printingDelay", 0.2, 0.0, 1.0, "Delay between printing blocks.\nDo not set to 0 if you are playing on a server.");
    public static final ConfigInteger       PRINTING_RANGE          = new ConfigInteger(    "printingRange", 2, 1, 6, "Printing block place range\nLower values are recommended for servers.");
    public static final ConfigBoolean 		PRINT_WATER = new ConfigBoolean( "printWater", false, "Whether or not the printer should place water\n source blocks or make blocks waterlogged.");
    public static final ConfigBoolean 		PRINT_MODE 				= new ConfigBoolean( "printingMode", false, "Autobuild / print loaded selection.\nBe aware that some servers and anticheat plugins do not allow printing.");
    
	public static final ImmutableList<IConfigBase> betterConfigList = ImmutableList.of(
			Configs.Generic.AREAS_PER_WORLD,
			//BETTER_RENDER_ORDER,
			Configs.Generic.CHANGE_SELECTED_CORNER,
			Configs.Generic.EASY_PLACE_MODE,
			Configs.Generic.EASY_PLACE_HOLD_ENABLED,
			Configs.Generic.EXECUTE_REQUIRE_TOOL,
			Configs.Generic.FIX_RAIL_ROTATION,
			Configs.Generic.LOAD_ENTIRE_SCHEMATICS,
			Configs.Generic.PASTE_IGNORE_INVENTORY,
			Configs.Generic.PICK_BLOCK_ENABLED,
			Configs.Generic.PLACEMENT_RESTRICTION,
			Configs.Generic.RENDER_MATERIALS_IN_GUI,
			Configs.Generic.RENDER_THREAD_NO_TIMEOUT,
			Configs.Generic.TOOL_ITEM_ENABLED,

			Configs.Generic.PASTE_REPLACE_BEHAVIOR,
			Configs.Generic.SELECTION_CORNERS_MODE,

			Configs.Generic.PASTE_COMMAND_INTERVAL,
			Configs.Generic.PASTE_COMMAND_LIMIT,
			Configs.Generic.PASTE_COMMAND_SETBLOCK,
			Configs.Generic.PICK_BLOCKABLE_SLOTS,
			Configs.Generic.TOOL_ITEM,

			PRINTING_DELAY,
            PRINTING_RANGE,
            PRINT_WATER,
            PRINT_MODE
	);
	
	// Hotkeys
	public static final ConfigHotkey TOGGLE_PRINTING_MODE 			= new ConfigHotkey("togglePrintingMode", "M,O", "Allows quickly toggling on/off Printing mode");
    
	public static final List<ConfigHotkey> betterHotkeyList = ImmutableList.of(
            Hotkeys.ADD_SELECTION_BOX,
            Hotkeys.CLONE_SELECTION,
            Hotkeys.DELETE_SELECTION_BOX,
            Hotkeys.EASY_PLACE_ACTIVATION,
            Hotkeys.EASY_PLACE_TOGGLE,
            Hotkeys.EXECUTE_OPERATION,
            Hotkeys.INVERT_GHOST_BLOCK_RENDER_STATE,
            Hotkeys.INVERT_OVERLAY_RENDER_STATE,
            Hotkeys.LAYER_MODE_NEXT,
            Hotkeys.LAYER_MODE_PREVIOUS,
            Hotkeys.LAYER_NEXT,
            Hotkeys.LAYER_PREVIOUS,
            Hotkeys.LAYER_SET_HERE,
            Hotkeys.NUDGE_SELECTION_NEGATIVE,
            Hotkeys.NUDGE_SELECTION_POSITIVE,
            Hotkeys.MOVE_ENTIRE_SELECTION,
            Hotkeys.OPEN_GUI_AREA_SETTINGS,
            Hotkeys.OPEN_GUI_LOADED_SCHEMATICS,
            Hotkeys.OPEN_GUI_MAIN_MENU,
            Hotkeys.OPEN_GUI_MATERIAL_LIST,
            Hotkeys.OPEN_GUI_PLACEMENT_SETTINGS,
            Hotkeys.OPEN_GUI_SCHEMATIC_PLACEMENTS,
            Hotkeys.OPEN_GUI_SCHEMATIC_PROJECTS,
            Hotkeys.OPEN_GUI_SCHEMATIC_VERIFIER,
            Hotkeys.OPEN_GUI_SELECTION_MANAGER,
            Hotkeys.OPEN_GUI_SETTINGS,
            Hotkeys.OPERATION_MODE_CHANGE_MODIFIER,
            Hotkeys.PICK_BLOCK_FIRST,
            Hotkeys.PICK_BLOCK_LAST,
            Hotkeys.PICK_BLOCK_TOGGLE,
            Hotkeys.RENDER_INFO_OVERLAY,
            Hotkeys.RENDER_OVERLAY_THROUGH_BLOCKS,
            Hotkeys.RERENDER_SCHEMATIC,
            Hotkeys.SAVE_AREA_AS_IN_MEMORY_SCHEMATIC,
            Hotkeys.SAVE_AREA_AS_SCHEMATIC_TO_FILE,
            Hotkeys.SCHEMATIC_REBUILD_BREAK_ALL,
            Hotkeys.SCHEMATIC_REBUILD_BREAK_ALL_EXCEPT,
            Hotkeys.SCHEMATIC_REBUILD_BREAK_DIRECTION,
            Hotkeys.SCHEMATIC_REBUILD_REPLACE_ALL,
            Hotkeys.SCHEMATIC_REBUILD_REPLACE_DIRECTION,
            Hotkeys.SCHEMATIC_VERSION_CYCLE_MODIFIER,
            Hotkeys.SCHEMATIC_VERSION_CYCLE_NEXT,
            Hotkeys.SCHEMATIC_VERSION_CYCLE_PREVIOUS,
            Hotkeys.SELECTION_GRAB_MODIFIER,
            Hotkeys.SELECTION_GROW_HOTKEY,
            Hotkeys.SELECTION_GROW_MODIFIER,
            Hotkeys.SELECTION_NUDGE_MODIFIER,
            Hotkeys.SELECTION_MODE_CYCLE,
            Hotkeys.SELECTION_SHRINK_HOTKEY,
            Hotkeys.SET_AREA_ORIGIN,
            Hotkeys.SET_SELECTION_BOX_POSITION_1,
            Hotkeys.SET_SELECTION_BOX_POSITION_2,
            Hotkeys.TOGGLE_ALL_RENDERING,
            Hotkeys.TOGGLE_AREA_SELECTION_RENDERING,
            Hotkeys.TOGGLE_INFO_OVERLAY_RENDERING,
            Hotkeys.TOGGLE_OVERLAY_RENDERING,
            Hotkeys.TOGGLE_OVERLAY_OUTLINE_RENDERING,
            Hotkeys.TOGGLE_OVERLAY_SIDE_RENDERING,
            Hotkeys.TOGGLE_PLACEMENT_BOXES_RENDERING,
            Hotkeys.TOGGLE_PLACEMENT_RESTRICTION,
            Hotkeys.TOGGLE_SCHEMATIC_BLOCK_RENDERING,
            Hotkeys.TOGGLE_SCHEMATIC_RENDERING,
            Hotkeys.TOGGLE_TRANSLUCENT_RENDERING,
            Hotkeys.TOGGLE_VERIFIER_OVERLAY_RENDERING,
            Hotkeys.TOOL_ENABLED_TOGGLE,
            Hotkeys.TOOL_PLACE_CORNER_1,
            Hotkeys.TOOL_PLACE_CORNER_2,
            Hotkeys.TOOL_SELECT_ELEMENTS,
            Hotkeys.TOOL_SELECT_MODIFIER_BLOCK_1,
            Hotkeys.TOOL_SELECT_MODIFIER_BLOCK_2,
            Hotkeys.UNLOAD_CURRENT_SCHEMATIC,
            TOGGLE_PRINTING_MODE
    );

	@Override
	public void onInitialize() {
		TOGGLE_PRINTING_MODE.getKeybind().setCallback(new KeyCallbackToggleBooleanConfigWithMessage(PRINT_MODE));
	}
}
