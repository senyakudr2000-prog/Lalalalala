package dev.whisper.hiddenexperiment;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

@Mod(HiddenExperiment.MOD_ID)
public final class HiddenExperiment {
    public static final String MOD_ID = "hiddenexperiment";

    public HiddenExperiment() {
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide() || !player.isAlive()) return;
        if (!(player.level() instanceof ServerLevel level)) return;

        if (player.tickCount % 36 == 0) {
            double x = player.getX() + (level.random.nextDouble() - 0.5) * 10.0;
            double y = player.getY() + 0.5 + level.random.nextDouble() * 2.5;
            double z = player.getZ() + (level.random.nextDouble() - 0.5) * 10.0;
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 18, 0.35, 0.55, 0.35, 0.08);
        }

        if (player.tickCount % 100 == 0 && level.random.nextFloat() < 0.42f) {
            double x = player.getX() + (level.random.nextDouble() - 0.5) * 14.0;
            double z = player.getZ() + (level.random.nextDouble() - 0.5) * 14.0;
            BlockPos strikePos = BlockPos.containing(x, player.getY(), z);
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(strikePos.getX() + 0.5, level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, strikePos.getX(), strikePos.getZ()), strikePos.getZ() + 0.5);
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
            level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.AMBIENT, 0.7f, 0.65f + level.random.nextFloat() * 0.7f);
        }
    }

    @SubscribeEvent
    public void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        BlockPos pos = event.getPos();
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 10, 0.3, 0.3, 0.3, 0.04);
        if (level.random.nextFloat() < 0.035f) {
            level.sendParticles(ParticleTypes.PORTAL, pos.getX() + 0.5, pos.getY() + 0.7, pos.getZ() + 0.5, 45, 0.45, 0.7, 0.45, 0.18);
            level.playSound(null, pos, SoundEvents.ENDERMAN_STARE, SoundSource.BLOCKS, 0.8f, 0.75f);
        }
    }

    @SubscribeEvent
    public void onDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        level.sendParticles(ParticleTypes.CRIT, player.getX(), player.getY() + 1.0, player.getZ(), 24, 0.45, 0.6, 0.45, 0.15);
        if (level.random.nextFloat() < 0.18f) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 65, 0.8, 0.9, 0.8, 0.2);
            level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.9f, 0.8f + level.random.nextFloat() * 0.4f);
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event) {
        Entity killer = event.getSource().getEntity();
        if (!(killer instanceof Player player) || player.level().isClientSide()) return;
        if (!(player.level() instanceof ServerLevel level)) return;
        var dead = event.getEntity();
        level.sendParticles(ParticleTypes.SOUL, dead.getX(), dead.getY() + 0.4, dead.getZ(), 28, 0.5, 0.6, 0.5, 0.08);
        if (level.random.nextFloat() < 0.12f) {
            var creature = EntityType.ENDERMITE.create(level);
            if (creature != null) {
                creature.moveTo(dead.getX() + 1.5, dead.getY(), dead.getZ() + 1.5, level.random.nextFloat() * 360f, 0);
                level.addFreshEntity(creature);
            }
        }
    }
}
