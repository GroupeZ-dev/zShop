package fr.maxlego08.shop.button.buttons;

import java.util.List;

import org.bukkit.inventory.ItemStack;

import fr.maxlego08.shop.api.button.Button;
import fr.maxlego08.shop.api.button.buttons.SlotButton;
import fr.maxlego08.shop.api.enums.ButtonType;
import fr.maxlego08.shop.api.enums.PlaceholderAction;
import fr.maxlego08.shop.api.sound.SoundOption;

public class ZButtonSlot extends ZPlaceholderButton implements SlotButton {

	private final List<Integer> slots;

	/**
	 * @param type
	 * @param itemStack
	 * @param slot
	 * @param permission
	 * @param message
	 * @param elseButton
	 * @param isPermanent
	 * @param action
	 * @param placeholder
	 * @param value
	 * @param slots
	 */
	public ZButtonSlot(ButtonType type, ItemStack itemStack, int slot, String permission, String message,
			Button elseButton, boolean isPermanent, PlaceholderAction action, String placeholder, String value,
			List<Integer> slots, boolean glow, SoundOption sound, boolean isClose) {
		super(type, itemStack, slot, permission, message, elseButton, isPermanent, action, placeholder, value, glow,
				sound, isClose);
		this.slots = slots;
	}

	@Override
	public List<Integer> getSlots() {
		return slots;
	}

}
