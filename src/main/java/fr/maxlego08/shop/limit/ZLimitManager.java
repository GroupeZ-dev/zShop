package fr.maxlego08.shop.limit;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimitType;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.api.limit.PlayerLimit;
import fr.maxlego08.shop.placeholder.LocalPlaceholder;
import fr.maxlego08.shop.save.Config;
import fr.maxlego08.shop.zcore.utils.ZUtils;
import fr.maxlego08.shop.zcore.utils.storage.Persist;
import fr.maxlego08.shop.zcore.utils.storage.Saveable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class ZLimitManager extends ZUtils implements LimiterManager, Saveable, Listener {

    public static List<ZLimit> limits = new ArrayList<>();
    private final transient ShopPlugin plugin;
    private final transient Map<UUID, ZPlayerLimit> players = new HashMap<>();

    public ZLimitManager(ShopPlugin plugin) {
        this.plugin = plugin;
    }

    private void createFolder() {
        File folder = getFolder();
        if (!folder.exists()) folder.mkdir();
    }

    private File getFolder() {
        return new File(plugin.getDataFolder(), "players/");
    }

    public void registerPlaceholders() {

        LocalPlaceholder localPlaceholder = LocalPlaceholder.getInstance();
        localPlaceholder.register("limiter_time_server_sell_", (player, string) -> placeholderLimiter(LimitType.SERVER_SELL, string, false));
        localPlaceholder.register("limiter_time_server_buy_", (player, string) -> placeholderLimiter(LimitType.SERVER_BUY, string, false));
        localPlaceholder.register("limiter_time_player_buy_", (player, string) -> placeholderLimiter(LimitType.PLAYER_BUY, string, false));
        localPlaceholder.register("limiter_time_player_sell_", (player, string) -> placeholderLimiter(LimitType.PLAYER_SELL, string, false));

        localPlaceholder.register("limiter_date_server_sell_", (player, string) -> placeholderLimiter(LimitType.SERVER_SELL, string, true));
        localPlaceholder.register("limiter_date_server_buy_", (player, string) -> placeholderLimiter(LimitType.SERVER_BUY, string, true));
        localPlaceholder.register("limiter_date_player_sell_", (player, string) -> placeholderLimiter(LimitType.PLAYER_SELL, string, true));
        localPlaceholder.register("limiter_date_player_buy_", (player, string) -> placeholderLimiter(LimitType.PLAYER_BUY, string, true));

    }

    private String placeholderLimiter(LimitType limitType, String material, boolean isDate) {
        Optional<Limit> optional = getLimit(limitType, material);
        if (optional.isPresent()) {
            Limit limit = optional.get();
            limit.update();
            if (isDate) {
                SimpleDateFormat simpleDateFormat = new SimpleDateFormat(Config.dateFormat);
                return simpleDateFormat.format(limit.getCalendar().getTime());
            }
            return limit.getFormattedTimeUntilNextTask();
        }
        return material + " not found for " + limitType.name();
    }

    @Override
    public void save(Persist persist) {

        limits.removeIf(limit -> limit.getType() == LimitType.PLAYER_BUY || limit.getType() == LimitType.PLAYER_SELL);

        persist.save(this, "limits");
        createFolder();

        this.players.values().forEach(player -> {
            File file = player.getFile(this.plugin);
            if (player.isEmpty()) file.delete();
            else persist.save(player, file, false);
        });
    }

    @Override
    public void load(Persist persist) {
        persist.loadOrSaveDefault(this, ZLimitManager.class, "limits");

        createFolder();

        File folder = getFolder();
        try (Stream<Path> s = Files.walk(Paths.get(folder.getPath()))) {
            s.skip(1).map(Path::toFile).filter(File::isFile).filter(e -> e.getName().endsWith(".json")).forEach(file -> {
                ZPlayerLimit playerLimit = persist.load(ZPlayerLimit.class, file);
                if (playerLimit == null) return;
                if (playerLimit.isEmpty()) file.delete();
                else players.put(playerLimit.getUniqueId(), playerLimit);
            });
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (this.players.containsKey(uuid)) {
            ZPlayerLimit playerLimit = this.players.get(uuid);
            runAsync(this.plugin, () -> {
                Persist persist = this.plugin.getPersist();
                File file = playerLimit.getFile(this.plugin);
                if (playerLimit.isEmpty()) {
                    this.players.remove(uuid);
                    file.delete();
                } else persist.save(playerLimit, file);
            });
        }
    }

    @Override
    public Collection<Limit> getLimits() {
        return Collections.unmodifiableList(limits);
    }

    @Override
    public Collection<Limit> getLimits(LimitType limitType) {
        List<Limit> filteredLimits = new ArrayList<>();
        for (Limit limit : limits) {
            if (limit.getType() == limitType) {
                filteredLimits.add(limit);
            }
        }
        return Collections.unmodifiableList(filteredLimits);
    }

    @Override
    public void create(Limit limit) {

        Optional<Limit> optional = getLimit(limit.getType(), limit.getMaterial());
        ZLimit zLimit = (ZLimit) limit;
        zLimit.setValid(true);

        // Supprimer l'ancienne valeur et mettre à jour le nombre
        optional.ifPresent(value -> {
            limits.remove((ZLimit) value);
            zLimit.setAmount(value.getAmount());
        });

        limits.add(zLimit);
    }

    @Override
    public Optional<Limit> getLimit(LimitType limitType, String material) {
        return limits.stream().filter(e -> e.getMaterial().equalsIgnoreCase(material) && e.getType() == limitType).map(e -> (Limit) e).findFirst();
    }

    @Override
    public Optional<PlayerLimit> getLimit(Player player) {
        return Optional.ofNullable(this.players.getOrDefault(player.getUniqueId(), null));
    }

    @Override
    public PlayerLimit getOrCreate(Player player) {
        if (!this.players.containsKey(player.getUniqueId())) {
            ZPlayerLimit playerLimit = new ZPlayerLimit(player.getUniqueId());
            this.players.put(player.getUniqueId(), playerLimit);
            return playerLimit;
        }
        return this.players.get(player.getUniqueId());
    }

    @Override
    public void deletes() {
        limits.removeIf(limit -> !limit.isValid());
    }

    @Override
    public void invalid() {
        limits.forEach(e -> e.setValid(false));
    }

    @Override
    public void reset(Limit limit) {
        this.players.values().forEach(p -> p.reset(limit));
    }

    @Override
    public void verifyPlayersLimit() {
        this.plugin.getIManager().getScheduler().runAsync(w -> {
            Iterator<Map.Entry<UUID, ZPlayerLimit>> iterator = this.players.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, ZPlayerLimit> entry = iterator.next();
                ZPlayerLimit playerLimit = entry.getValue();

                List<String> removedMaterials = new ArrayList<>();
                playerLimit.getBuyLimits().forEach((material, amount) -> {
                    Optional<Limit> optional = getLimit(LimitType.PLAYER_BUY, material);
                    if (!optional.isPresent()) {
                        removedMaterials.add(material);
                    }
                });
                playerLimit.getSellLimits().forEach((material, amount) -> {
                    Optional<Limit> optional = getLimit(LimitType.PLAYER_SELL, material);
                    if (!optional.isPresent()) {
                        removedMaterials.add(material);
                    }
                });

                removedMaterials.forEach(playerLimit::reset);

                if (playerLimit.isEmpty()) {
                    iterator.remove();
                    playerLimit.getFile(this.plugin).delete();
                }
            }
        });
    }
}
