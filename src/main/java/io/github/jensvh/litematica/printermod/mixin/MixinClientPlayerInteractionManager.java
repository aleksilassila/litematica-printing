package io.github.jensvh.litematica.printermod.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import io.github.jensvh.litematica.printermod.interfaces.IClientPlayerInteractionManager;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class MixinClientPlayerInteractionManager implements IClientPlayerInteractionManager {
	@Shadow
	private MinecraftClient client;

    @Override
	public void rightClickBlock(BlockPos pos, Direction side, Vec3d hitVec)
	{
		interactBlock(client.player, client.world, Hand.MAIN_HAND,
			new BlockHitResult(hitVec, side, pos, false));
		interactItem(client.player, client.world, Hand.MAIN_HAND);
	}

	@Shadow
	public abstract ActionResult interactBlock(
            ClientPlayerEntity clientPlayerEntity_1, ClientWorld clientWorld_1,
            Hand hand_1, BlockHitResult blockHitResult_1);

	@Shadow
	public abstract ActionResult interactItem(PlayerEntity playerEntity_1,
                                              World world_1, Hand hand_1);
}