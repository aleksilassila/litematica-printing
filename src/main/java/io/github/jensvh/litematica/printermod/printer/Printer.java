package io.github.jensvh.litematica.printermod.printer;

import java.util.Date;

import fi.dy.masa.litematica.config.Configs;
import fi.dy.masa.litematica.data.DataManager;
import fi.dy.masa.litematica.util.InventoryUtils;
import fi.dy.masa.litematica.util.ItemUtils;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import fi.dy.masa.malilib.gui.GuiBase;
import io.github.jensvh.litematica.printermod.LitematicaMixinMod;
import io.github.jensvh.litematica.printermod.interfaces.IClientPlayerInteractionManager;
import net.minecraft.block.AbstractButtonBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.LeverBlock;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;

public class Printer extends PrinterUtils {
    private final MinecraftClient client;
    private final ClientPlayerEntity playerEntity;
    private final ClientWorld clientWorld;
    private WorldSchematic worldSchematic;

	public long lastPlaced = new Date().getTime();

	public boolean lockCamera = false;

	private boolean shouldPlaceWater;

	public static class Queue {
		public static BlockPos neighbor;
		public static Direction side;
		public static Vec3d hitVec;
		public static boolean useShift;
	}

	public Printer(MinecraftClient client, ClientPlayerEntity playerEntity, ClientWorld clientWorld) {
        this.client = client;
        this.playerEntity = playerEntity;
        this.clientWorld = clientWorld;
    }

	/**
	 * @return true if block was placed.
	 */
	public boolean processBlock(BlockPos pos) {
		if (!DataManager.getRenderLayerRange().isPositionWithinRange(pos)) return false;

		BlockState currentState = clientWorld.getBlockState(pos);
		BlockState requiredState = worldSchematic.getBlockState(pos);

		// Check if block should be just clicked (repeaters etc.)
		if (shouldClickBlock(currentState, requiredState)) {
			addQueuedPacket(pos, Direction.UP, Vec3d.ofCenter(pos), null, false);

			lastPlaced = new Date().getTime();
			return true;
		}

		// FIXME water and lava
		// Check if something should be placed in target block
		if (requiredState.isAir()
				|| requiredState.getMaterial().equals(Material.WATER)
				|| requiredState.getMaterial().equals(Material.LAVA)) return false;

		// Check if target block is empty
		if (!shouldPlaceWater)
			if (!currentState.isAir()) return false;
		else {
			if (isWaterLogged(requiredState) && isWaterLogged(currentState)) return false;
			if (!isWaterLogged(requiredState) && !currentState.isAir()) return false;
		}

		System.out.println(currentState.getBlock().getName() + ": " + requiredState.getBlock().getName() + ", " + isWaterLogged(currentState) + ", " + isWaterLogged(requiredState));

		// Check if can be placed in world
		if (!requiredState.canPlaceAt(clientWorld, pos)) return false;

		// Check if player is holding right block
		Item itemInHand = playerEntity.getInventory().getMainHandStack().getItem();
		if (!itemInHand.equals(requiredItemInHand(requiredState, currentState))) {
			if (playerEntity.getAbilities().creativeMode) {
				ItemStack required = new ItemStack(requiredItemInHand(requiredState, currentState));
				BlockEntity te = clientWorld.getBlockEntity(pos);

				// The creative mode pick block with NBT only works correctly
				// if the server world doesn't have a TileEntity in that position.
				// Otherwise it would try to write whatever that TE is into the picked ItemStack.
				if (GuiBase.isCtrlDown() && te != null && clientWorld.isAir(pos))
				{
					ItemUtils.storeTEInStack(required, te);
				}

				InventoryUtils.setPickedItemToHand(required, client);
				client.interactionManager.clickCreativeStack(playerEntity.getStackInHand(Hand.MAIN_HAND),
						36 + playerEntity.getInventory().selectedSlot);

			} else {
				int slot = getBlockInventorySlot(requiredItemInHand(requiredState, currentState));

				if (slot == -1) {
					return false;
				}

				swapHandWithSlot(slot);
			}
		}

		return placeBlock(pos);
	}

    public void print() {
		lockCamera = false;
		sendQueuedPackets();

		if (new Date().getTime() < lastPlaced + 1000.0 * LitematicaMixinMod.PRINTING_DELAY.getDoubleValue()) return;

		int range = LitematicaMixinMod.PRINTING_RANGE.getIntegerValue();
		shouldPlaceWater = LitematicaMixinMod.PRINT_WATER.getBooleanValue();
		worldSchematic = SchematicWorldHandler.getSchematicWorld();

		forEachBlockInRadius:
		for (int y = -range; y < range + 1; y++) {
			for (int x = -range; x < range + 1; x++) {
				for (int z = -range; z < range + 1; z++) {
					if (processBlock(playerEntity.getBlockPos().north(x).west(z).up(y))) return;
				}
			}
		}
    }

    public void addQueuedPacket(BlockPos neighbor, Direction side, Vec3d hitVec, Direction playerShouldBeFacing, boolean useShift) {
		if (Queue.neighbor != null) return;

		lockCamera = true;

		sendLookPacket(playerShouldBeFacing);

		Queue.neighbor = neighbor;
		Queue.side = side;
		Queue.hitVec = hitVec;
		Queue.useShift = useShift;
	}

	public void sendQueuedPackets() {
		if (Queue.neighbor != null) {
			boolean wasSneaking = playerEntity.isSneaking();

			if (Queue.useShift && !wasSneaking)
				playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
			else if (!Queue.useShift && wasSneaking)
				playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));

			((IClientPlayerInteractionManager) client.interactionManager).rightClickBlock(Queue.neighbor,
					Queue.side.getOpposite(), Queue.hitVec);

			if (Queue.useShift && !wasSneaking)
				playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
			else if (!Queue.useShift && wasSneaking)
				playerEntity.networkHandler.sendPacket(new ClientCommandC2SPacket(playerEntity, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));

			Queue.neighbor = null;
			Queue.hitVec = null;
			Queue.side = null;
		}
	}

	private void swapHandWithSlot(int slot) {
		ItemStack stack = playerEntity.getInventory().getStack(slot);
		InventoryUtils.setPickedItemToHand(stack, client);
	}

	private int getBlockInventorySlot(Item item) {
    	Inventory inv = playerEntity.getInventory();

    	for (int slot = 0; slot < inv.size(); slot++) {
    		if (inv.getStack(slot).getItem().equals(item)) return slot;
		}

    	return -1;
	}

    private boolean placeBlock(BlockPos pos) {
    	BlockState state = SchematicWorldHandler.getSchematicWorld().getBlockState(pos);

		Vec3d posVec = Vec3d.ofCenter(pos);

		Direction playerShouldBeFacing = getFacingDirection(state);
		Direction.Axis axis = availableAxis(state);
		int half = getBlockHalf(state);

//    	for (Property<?> prop : state.getProperties()) {
//			System.out.println("Block " + state.getBlock().getName() + " has property " + prop.getName() + " with value " + state.get(prop).toString() + " class name " + state.get(prop).getClass().getName());
//		}

		for (Direction side : Direction.values()) {
			if (half == 1 && side.equals(Direction.DOWN)) continue;
			if (half == 0 && side.equals(Direction.UP)) continue;
			if (axis != null && side.getAxis() != axis) continue;
			if (isTorchOnWall(state) && playerShouldBeFacing != side) continue;
			if (state.getBlock() instanceof HopperBlock && playerShouldBeFacing != side.getOpposite()) continue;
			if ((state.getBlock() instanceof AbstractButtonBlock || state.getBlock() instanceof LeverBlock)
					&& isLeverOnWall(state)
					&& playerShouldBeFacing != side.getOpposite()) continue;

			BlockPos neighbor = pos.offset(side);

			if (!canBeClicked(neighbor))
				continue;

			Vec3d hitVec = posVec.add(Vec3d.of(side.getVector()).multiply(0.5));

			if (half == 1 && !side.equals(Direction.UP)) {
				hitVec = hitVec.add(0, 0.25, 0);
			} else if (half == 0 && !side.equals(Direction.DOWN)) {
				hitVec = hitVec.add(0, -0.25, 0);
			}

			addQueuedPacket(neighbor, side, hitVec, playerShouldBeFacing, true);

			lastPlaced = new Date().getTime();
			return true;
		}

		return false;
	}

	private void sendLookPacket(Direction playerShouldBeFacing) {
    	if (playerShouldBeFacing != null) {
			float yaw = playerEntity.getYaw();
			float pitch = playerEntity.getPitch();

			if (playerShouldBeFacing.getAxis().isHorizontal()) {
				yaw = playerShouldBeFacing.asRotation();
			} else {
				pitch = playerShouldBeFacing == Direction.DOWN ? 90 : -90;
			}
			
			playerEntity.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(
				yaw, pitch, playerEntity.isOnGround()));
		}
	}

    private Item requiredItemInHand(BlockState requiredState, BlockState currentState) {
		// If block should be waterlogged
		if (!currentState.isAir() && isWaterLogged(requiredState))
			return Items.WATER_BUCKET;
		else if (requiredState.getBlock().equals(Blocks.WATER))
			return Items.WATER_BUCKET;
		else
			return new ItemStack(requiredState.getBlock()).getItem();
	}

	private boolean canBeClicked(BlockPos pos)
	{
		return getOutlineShape(pos) != VoxelShapes.empty();
	}

	private boolean needsShift(BlockState state, BlockHitResult blockHitResult) {
		// FIXME needShift
//		Block block = state.getBlock();
//
//		ActionResult actionResult = state.onUse(client.world, client.player, Hand.MAIN_HAND, blockHitResult);
//		System.out.println("Result: " + actionResult.name());
//		if (actionResult.isAccepted()) {
//			System.out.println("ACCEPTED OR SOMETHING");
//			return true;
//		}



//		return block instanceof DoorBlock
//				|| block instanceof ChestBlock
//				|| block instanceof CraftingTableBlock
//				|| block instanceof AnvilBlock
//				|| block instanceof AbstractFurnaceBlock
//				|| block instanceof BarrelBlock
//				|| block instanceof ChestBlock
//				|| block instanceof BedBlock
//				|| block instanceof BellBlock
//				|| block instanceof BrewingStandBlock
//				|| block instanceof ChestBlock
//				|| block instanceof ChestBlock
//				|| block instanceof ChestBlock
//				|| block instanceof TrapdoorBlock;

		return true;
	}

	private VoxelShape getOutlineShape(BlockPos pos)
	{
		return getState(pos).getOutlineShape(clientWorld, pos);
	}

	private BlockState getState(BlockPos pos)
	{
		return clientWorld.getBlockState(pos);
	}
}