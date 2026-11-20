package com.senyakudr.afterlight;

import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(Afterlight.MOD_ID)
public final class Afterlight {
    public static final String MOD_ID = "afterlight";

    private static final List<String> WHISPERS = List.of(
        "You heard that, didn't you?",
        "Something moved just out of sight.",
        "The silence feels different now.",
        "For a moment, you weren't alone.",
        "Keep looking. It was there."
    );

    public Afterlight() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        Level level = player.level();

        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        if (player.isSpectator() || player.isCreative() || player.tickCount % 20 != 0) {
            return;
        }

        RandomSource random = serverLevel.getRandom();
        // On average, one quiet anomaly every four minutes per active player.
        if (random.nextInt(240) != 0) {
            return;
        }

        int kind = random.nextInt(3);
        double side = random.nextBoolean() ? 1.0 : -1.0;
        double x = player.getX() - player.getLookAngle().x * 3.0
                + player.getLookAngle().z * side * 1.5;
        double y = player.getY() + 0.8;
        double z = player.getZ() - player.getLookAngle().z * 3.0
                - player.getLookAngle().x * side * 1.5;
        BlockPos soundPos = BlockPos.containing(x, y, z);

        if (kind == 0) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, x, y, z, 12, 0.35, 0.5, 0.35, 0.01);
            serverLevel.playSound(null, soundPos, SoundEvents.AMBIENT_CAVE,
                    SoundSource.AMBIENT, 0.8F, 0.65F + random.nextFloat() * 0.2F);
        } else if (kind == 1) {
            serverLevel.sendParticles(ParticleTypes.PORTAL, x, y + 0.5, z, 24, 0.45, 0.8, 0.45, 0.05);
            serverLevel.playSound(null, soundPos, SoundEvents.AMBIENT_CAVE,
                    SoundSource.AMBIENT, 0.55F, 1.4F);
        } else {
            double behindX = player.getX() - player.getLookAngle().x * 2.0;
            double behindZ = player.getZ() - player.getLookAngle().z * 2.0;
            serverLevel.sendParticles(ParticleTypes.ASH, behindX, player.getY() + 1.0, behindZ,
                    18, 0.3, 0.6, 0.3, 0.015);
            player.displayClientMessage(Component.literal(
                    WHISPERS.get(random.nextInt(WHISPERS.size()))), true);
        }
    }
}
