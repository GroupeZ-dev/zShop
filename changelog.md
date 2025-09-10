# ToDo

- [ ] Add an option to disable certain click for purchase confirmation (https://discord.com/channels/511516467615760405/1216306371604250624/1217474577974825081)

# Unreleased

# 3.3.1

- Fix button load event
- Fix item translation
- Fix inventory confirm with kebab case 

# 3.3.0

- Update to gradle

# 3.2.1

- Update to zMenu 1.1.0.0

# 3.2.0

- Update to last zMenu Version
- Fix item confirm button
- Fix file loading
- Fix sell all with item in the offhand

# 3.1.8

- **Updated**: [CurrenciesAPI](https://github.com/Traqueur-dev/CurrenciesAPI) to version 1.0.5. (Bug fix + add RedisEconomy)
- **Fixed** Color message for don't have money.

# 3.1.7

- Improve price modifier
- Fix confirm button with commands and message, you need to use now a ``confirm-actions``

# 3.1.6

- Fix currencies API
- Added ``/sellinventory``, Opens an inventory and sells the content. Enable this feature with `enableSellInventoryCommand: true`

# 3.1.5

- Using the [CurrenciesAPI](https://github.com/Traqueur-dev/CurrenciesAPI) library, this allows introducing new economies like `ZESSENTIALS` and `ECOBITS`
- Fixed enchant for 1.21+

# 3.1.4

- Update to 1.21

# 3.1.3

- Update to 1.20.6

# 3.1.2

- Fix error with items amount
- Add description for some commands (/sell all, /sell hand, /sell handall)

# 3.1.1

- Fix CoinsEngine API

# 3.1.0

- Fix folia
- Add ZShopEconomyRegisterEvent
- Fix CoinsEngine

# 3.0.9

- Fix sellall message
- Add option for /sell <all/hand/handall>

# 3.0.8

- Update to zMenu 1.0.2.7
- Fix /sell-hand command
- Fix sell lore confirm

# 3.0.7

- Fix error with item lore

# 3.0.6

- Fix error with MenuItemStack cache on purchase and sell items
- Added ``economy`` configuration for each inventory. You can now choose the default economy by file 

# 3.0.5

- Fix error with lore

# 3.0.4

- Correction of price display during purchase and sale, taking into account events.
- Ajout de l'économie BeastToken
- Fix NPE with ``ShopManger#getItemButton`` method

# 3.0.3

- Add ``unstackable`` tag for ZSHOP_ITEM, allows you to choose the number of items to buy but not receive them stacked. Perfect for potions for example.

# 3.0.2

- Add inventory name for methods ``openBuy``, ``openSell`` and ``openConfirm``
- Add inventory name in ZSHOP_ITEM button, you can now specify the inventory for buy, sell and confirm 