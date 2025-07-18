package fr.maxlego08.shop.placeholder;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.List;

public interface Placeholder {

    static Placeholder getPlaceholder() {
        if (PlaceHolders.PLACEHOLDER != null) return PlaceHolders.PLACEHOLDER;
        return PlaceHolders.PLACEHOLDER = (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null ? new Api() : new Local());
    }

    String setPlaceholders(Player player, String string);

    List<String> setPlaceholders(Player player, List<String> list);

    class PlaceHolders {
        static Placeholder PLACEHOLDER = null;
    }

    class Api implements Placeholder {

        public Api() {
            PlaceholderExpansion expansion = new DistantPlaceholder(LocalPlaceholder.getInstance());
            expansion.register();
        }

        @Override
        public String setPlaceholders(Player player, String string) {
            return PlaceholderAPI.setPlaceholders(player, string);
        }

        @Override
        public List<String> setPlaceholders(Player player, List<String> list) {
            return PlaceholderAPI.setPlaceholders(player, list);
        }

    }

    class Local implements Placeholder {

        @Override
        public String setPlaceholders(Player player, String string) {
            return LocalPlaceholder.getInstance().setPlaceholders(player, string);
        }

        @Override
        public List<String> setPlaceholders(Player player, List<String> list) {
            return LocalPlaceholder.getInstance().setPlaceholders(player, list);
        }

    }

}
