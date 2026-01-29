package fr.maxlego08.shop.command.commands;

import fr.maxlego08.shop.ShopPlugin;
import fr.maxlego08.shop.command.VCommand;
import fr.maxlego08.shop.zcore.enums.Permission;
import fr.maxlego08.shop.zcore.utils.commands.CommandType;

public class CommandShop extends VCommand {

    public CommandShop(ShopPlugin plugin) {
        super(plugin);
        this.setPermission(Permission.ZSHOP_HELP);
        this.addSubCommand(new CommandShopReload(plugin));
        this.addSubCommand(new CommandShopLogs(plugin));
        this.addSubCommand(new CommandShopConvert(plugin));
        this.addSubCommand(new CommandShopResetLimit(plugin));
    }

    @Override
    protected CommandType perform(ShopPlugin plugin) {
        syntaxMessage();
        return CommandType.SUCCESS;
    }

}
