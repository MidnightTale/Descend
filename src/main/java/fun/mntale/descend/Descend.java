package fun.mntale.descend;

import com.tcoded.folialib.FoliaLib;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public final class Descend extends JavaPlugin implements Listener {

    private FoliaLib foliaLib;

    @Override
    public void onEnable() {
        this.foliaLib = new FoliaLib(this);
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public void onDisable() {
        if (this.foliaLib != null) {
            this.foliaLib.getScheduler().cancelAllTasks();
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onEntityDamage(EntityDamageEvent e) {
        if (e.getCause() != EntityDamageEvent.DamageCause.VOID) return;

        e.setCancelled(true);
        Entity entity = e.getEntity();
        World currentWorld = entity.getWorld();
        Location currentLoc = entity.getLocation();

        Location teleportLocation = switch (currentWorld.getEnvironment()) {
            case THE_END -> {
                World overworld = Bukkit.getWorlds().stream()
                        .filter(w -> w.getEnvironment() == World.Environment.NORMAL)
                        .findFirst()
                        .orElse(null);
                if (overworld == null) yield null;
                yield new Location(overworld, currentLoc.getX(), 320, currentLoc.getZ());
            }
            case NORMAL -> new Location(currentWorld, currentLoc.getX(), 320, currentLoc.getZ());
            case NETHER -> new Location(currentWorld, currentLoc.getX(), 128, currentLoc.getZ());
            default -> null;
        };

        if (teleportLocation == null) return;

        currentWorld.spawnParticle(Particle.PORTAL, currentLoc, 100, 1, 1, 1, 0.1);
        currentWorld.playSound(currentLoc, Sound.BLOCK_PORTAL_TRIGGER, 0.5f, 0.7f);

        foliaLib.getScheduler().runAtEntity(entity, (task) -> {
            entity.teleportAsync(teleportLocation).thenRun(() -> {
                if (entity instanceof LivingEntity livingEntity) {
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.SLOW_FALLING, 10 * 20, 0, true, true, true));
                    livingEntity.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 7 * 20, 0, true, true, true));
                }
                World newWorld = teleportLocation.getWorld();
                if (newWorld != null) {
                    newWorld.spawnParticle(Particle.PORTAL, teleportLocation, 100, 1, 1, 1, 0.1);
                    newWorld.playSound(teleportLocation, Sound.BLOCK_PORTAL_TRIGGER, 0.3f, 0.4f);
                }
            });
        });
    }
}

