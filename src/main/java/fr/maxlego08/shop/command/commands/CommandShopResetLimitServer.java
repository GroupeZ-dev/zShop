package fr.maxlego08.shop.command.commands;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimitType;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.command.VCommand;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.enums.Permission;
import fr.maxlego08.shop.zcore.utils.commands.CommandType;

import java.util.Collection;
import java.util.Optional;

public class CommandShopResetLimitServer extends VCommand {
    public CommandShopResetLimitServer(ShopPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ZSHOP_RESET_LIMIT_SERVER);
        this.addSubCommand("resetlimitserver", "resetserverlimit", "rsl");
        this.setDescription(Message.DESCRIPTION_RESET_LIMIT_SERVER);
        this.addOptionalArg("item");
    }

    @Override
    protected CommandType perform(ShopPlugin plugin) {
        LimiterManager limiterManager = plugin.getLimiterManager();
        String materialName = this.argAsString(0);
        if (materialName == null) {
            Collection<Limit> limitsBuy = limiterManager.getLimits(LimitType.SERVER_BUY);
            Collection<Limit> limitsSell = limiterManager.getLimits(LimitType.SERVER_SELL);
            for (Limit limit : limitsBuy) {
                limiterManager.reset(limit);
            }
            for (Limit limit : limitsSell) {
                limiterManager.reset(limit);
            }
            message(plugin, sender, Message.RESET_LIMIT_SERVERS_SUCCESS);

        } else {
            Optional<Limit> limitsBuy = limiterManager.getLimit(LimitType.SERVER_BUY, materialName);
            Optional<Limit> limitsSell = limiterManager.getLimit(LimitType.SERVER_SELL, materialName);
            if (limitsSell.isPresent()){
                limiterManager.reset(limitsSell.get());
                message(plugin, sender, Message.RESET_LIMIT_SERVER_ITEM_SUCCESS, "%item%", materialName, "%type%", "sell");
            }
            if (limitsBuy.isPresent()){
                limiterManager.reset(limitsBuy.get());
                message(plugin, sender, Message.RESET_LIMIT_SERVER_ITEM_SUCCESS, "%item%", materialName, "%type%", "buy");
            }
            if (limitsBuy.isEmpty() && limitsSell.isEmpty()) {
                message(plugin, sender, Message.RESET_LIMIT_NOT_FOUND, "%item%", materialName);
            }
        }
        return CommandType.SUCCESS;
    }
}
