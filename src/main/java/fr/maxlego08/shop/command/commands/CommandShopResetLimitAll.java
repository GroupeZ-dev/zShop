package fr.maxlego08.shop.command.commands;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.command.VCommand;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.enums.Permission;
import fr.maxlego08.shop.zcore.utils.commands.CommandType;

public class CommandShopResetLimitAll extends VCommand {
    public CommandShopResetLimitAll(ShopPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ZSHOP_RESET_LIMIT_ALL);
        this.addSubCommand("all");
        this.setDescription(Message.DESCRIPTION_RESET_LIMIT_ALL);
    }

    @Override
    protected CommandType perform(ShopPlugin plugin) {
        LimiterManager limiterManager = plugin.getLimiterManager();
        for (Limit limit : limiterManager.getLimits()){
            limiterManager.reset(limit);
        }
        message(plugin, sender, Message.RESET_LIMIT_ALL_SUCCESS);
        return CommandType.SUCCESS;
    }
}
