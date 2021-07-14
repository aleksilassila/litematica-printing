package io.github.jensvh.litematica.printermod.printer;

import net.minecraft.block.*;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Property;
import net.minecraft.util.math.Direction;

public class PrinterUtils {
    protected static int getBlockHalf(BlockState state) {
    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals("type") || prop.getName().equals("half")) {
				return state.get(prop).toString().equals("top") ? 1 : 0;
			}
		}

    	return -1;
	}

	protected static Direction getFacingDirection(BlockState state) {
		Direction dir = null;

		if (state.getBlock() instanceof PillarBlock) {
			return null;
		}

		if (!containsProperty(state, "facing")) return null;

    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("facing")) {
				dir = (Direction)state.get(prop);
			}
		}

		if (!shouldntGetOpposite(state)) {
			dir = dir.getOpposite();
		}

		if (shouldRotate(state)) {
			dir = dir.rotateYCounterclockwise();
		}

    	return dir;
	}

	protected static boolean isWaterLogged(BlockState state) {
    	if (!(state.getBlock() instanceof Waterloggable)) return false;
    	if (!containsProperty(state, "waterlogged")) return false;

		for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("waterlogged")) {
				return (Boolean) state.get(prop);
			}
		}

		return false;
	}

	protected static boolean isTorchOnWall(BlockState state) {
    	if (state.getBlock() instanceof WallTorchBlock) return true;
    	if (!(state.getBlock() instanceof TorchBlock)) return false;

		for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("facing")) {
				return true;
			}
		}

    	return false;
	}

	protected static boolean isLeverOnWall(BlockState state) {
    	if (!(state.getBlock() instanceof LeverBlock) && !(state.getBlock() instanceof AbstractButtonBlock)) {
    		return false;
		}

    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase("face")) {
				if (state.get(prop).toString().equalsIgnoreCase("wall")) {
					return true;
				}

				break;
			}
		}

    	return false;
	}

	private static boolean containsProperty(BlockState state, String name) {
    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equalsIgnoreCase(name)) {
				return true;
			}
		}

    	return false;
	}

	protected static boolean shouldntGetOpposite(BlockState state) {
		return state.getBlock() instanceof StairsBlock
				|| state.getBlock() instanceof GrindstoneBlock
				|| state.getBlock() instanceof LeverBlock
				|| state.getBlock() instanceof AbstractButtonBlock
				|| state.getBlock() instanceof ObserverBlock;
	}

	protected static boolean shouldClickBlock(BlockState state, BlockState targetState) {
    	Block block = state.getBlock();

    	if (!(block instanceof RepeaterBlock) && !(block instanceof ComparatorBlock)) return false;
    	if (block.getClass() != targetState.getBlock().getClass()) return false;

    	for (Property<?> prop : state.getProperties()) {
			if (prop.getName().equals("delay") || prop.getName().equals("mode")) {
				if (!targetState.get(prop).equals(state.get(prop))) return true;
			}
		}

    	return false;
	}

	protected static boolean shouldRotate(BlockState state) {
		return state.getBlock() instanceof AnvilBlock;
	}

	protected static Direction.Axis availableAxis(BlockState state) {
    	if (state.getBlock() instanceof PillarBlock) {
    		for (Property<?> prop : state.getProperties()) {
    			if (state.get(prop) instanceof Direction.Axis) {
    				return (Direction.Axis) state.get(prop);
				}
			}
		}

    	return null;
	}

	protected static boolean isFlowingBlock(BlockState state) {
		if (state.getMaterial().equals(Material.WATER) || state.getMaterial().equals(Material.LAVA)) {
			for (Property<?> prop : state.getProperties()) {
				if (prop instanceof IntProperty) {
					if ((Integer) state.get(prop) > 0) return true;
				}
			}
		}

		return false;
	}
}