package fr.maxlego08.shop.api.limit;

import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Optional;

public interface LimiterManager {

    Collection<Limit> getLimits();

    Collection<Limit> getLimits(LimitType limitType);

    void create(Limit limit);

    Optional<Limit> getLimit(LimitType limitType, String material);

    Optional<PlayerLimit> getLimit(Player player);

    PlayerLimit getOrCreate(Player player);

    void deletes();

    void reset(Limit limit);

    void invalid();

    /**
     * Allows you to check if the players' data are valid in relation to the plugin configuration. If not, then they will be reset
     */
    void verifyPlayersLimit();
}
