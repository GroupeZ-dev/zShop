package fr.maxlego08.shop.zcore.enums;

public enum Permission {
	
	ZSHOP_HELP,
	ZSHOP_RELOAD,
	ZSHOP_CONVERT,
	ZSHOP_LOGS,
	ZSHOP_SELL_ALL,
	ZSHOP_SELL_HAND,
	ZSHOP_SELL_HAND_ALL,
	ZSHOP_RESET_LIMIT,
    ZSHOP_RESET_LIMIT_ALL,
    ZSHOP_RESET_LIMIT_PLAYER,
    ZSHOP_RESET_LIMIT_SERVER

	;

	private final String permission;

	private Permission() {
		this.permission = this.name().toLowerCase().replace("_", ".");
	}

	public String getPermission() {
		return permission;
	}

}
