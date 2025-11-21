package fr.maxlego08.shop.command.commands;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.api.limit.Limit;
import fr.maxlego08.shop.api.limit.LimitType;
import fr.maxlego08.shop.api.limit.LimiterManager;
import fr.maxlego08.shop.command.VCommand;
import fr.maxlego08.shop.zcore.enums.Message;
import fr.maxlego08.shop.zcore.enums.Permission;
import fr.maxlego08.shop.zcore.utils.commands.CommandType;

import java.util.Optional;

public class CommandShopResetLimit extends VCommand {

    public CommandShopResetLimit(ShopPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ZSHOP_RESET_LIMIT);
        this.addSubCommand("resetlimit");
        this.setDescription(Message.DESCRIPTION_RESET_LIMIT);
        this.addSubCommand(new CommandShopResetLimitAll(plugin));
        this.addSubCommand(new CommandShopResetLimitPlayer(plugin));
        this.addSubCommand(new CommandShopResetLimitServer(plugin));
    }

    @Override
    protected CommandType perform(ShopPlugin plugin) {
        syntaxMessage();
        return CommandType.SUCCESS;
    }
}

