package fun.mntale.descend;

import io.github.retrooper.packetevents.util.folia.FoliaScheduler;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class Descend extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        Entity entity = e.getEntity();
        boolean isVoidDamage = e.getCause() == EntityDamageEvent.DamageCause.VOID;
        if (isVoidDamage) {
            World currentWorld = entity.getWorld();
            Location currentLoc = entity.getLocation();

            World.Environment env = currentWorld.getEnvironment();
            Location teleportLocation;

            if (env == World.Environment.THE_END) {
                World overworld = Bukkit.getWorlds().stream()
                        .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                        .findFirst()
                        .orElse(null);

                if (overworld == null) return;

                teleportLocation = new Location(overworld, currentLoc.getX(), 320, currentLoc.getZ());
            } else if (env == World.Environment.NORMAL) {
                teleportLocation = new Location(currentWorld, currentLoc.getX(), 320, currentLoc.getZ());
            } else if (env == World.Environment.NETHER) {
                teleportLocation = new Location(currentWorld, currentLoc.getX(), 128, currentLoc.getZ());
            } else {
                return;
            }

            currentWorld.spawnParticle(Particle.PORTAL, currentLoc, 100, 1, 1, 1, 0.1);
            currentWorld.playSound(currentLoc, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 0.7f);

                FoliaScheduler.getEntityScheduler().run(entity, this, (task) -> {
                entity.teleportAsync(teleportLocation).thenRun(() -> {
                    if (entity instanceof LivingEntity livingEntity) {
                        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10 * 20, 0, true, true, true));
                        livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7 * 20, 0, true, true, true));
                    }
                    World newWorld = teleportLocation.getWorld();
                    newWorld.spawnParticle(Particle.PORTAL, teleportLocation, 100, 1, 1, 1, 0.1);
                    newWorld.playSound(teleportLocation, Sound.BLOCK_PORTAL_TRIGGER, 0.3f, 0.4f);
                    });
                },null);
            }

            e.setCancelled(true);
        }
}

