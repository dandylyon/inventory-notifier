package com.dandylyon.inventorynotifier;

import com.google.inject.Provides;
import javax.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import java.util.Arrays;
import java.awt.image.BufferedImage;

import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.Notifier;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.ImageUtil;
import net.runelite.api.Client;
import net.runelite.api.Item;
import net.runelite.api.ItemContainer;
import net.runelite.api.InventoryID;
import net.runelite.api.events.ItemContainerChanged;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;

@Slf4j
@PluginDescriptor(
		name = "Inventory Notifier",
		description = "Notifies you when your inventory is full",
		tags = {"inventory", "notification"}
)
public class InventoryNotifierPlugin extends Plugin {
	@Inject
	private Client client;

	@Inject
	private Notifier notifier;

	@Inject
	private ConfigManager configManager; // Inject the ConfigManager
	@Inject
	private InventoryNotifierConfig config;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private InventoryNotifierOverlay overlay;

	private static final BufferedImage INVENTORY_IMAGE;

	static {
		INVENTORY_IMAGE = ImageUtil.loadImageResource(InventoryNotifierPlugin.class, "inventory_icon.png");
	}

	private static final BufferedImage INVENTORY_FULL_IMAGE;

	static {
		INVENTORY_FULL_IMAGE = ImageUtil.loadImageResource(InventoryNotifierPlugin.class, "inventory_full_icon.png");
	}
	private BufferedImage CURRENT_IMAGE = INVENTORY_IMAGE;

	private static final int INVENTORY_SIZE = 28;
	private boolean wasInventoryFull = false;

	@Provides
	InventoryNotifierConfig provideConfig(net.runelite.client.config.ConfigManager configManager) {
		return configManager.getConfig(InventoryNotifierConfig.class);
	}

	@Override
	protected void startUp() throws Exception {
		overlay = new InventoryNotifierOverlay(client, this, configManager); // Instantiate overlay
		overlayManager.add(overlay);
	}

	@Override
	protected void shutDown() throws Exception {
		overlayManager.remove(overlay); // Remove overlay on shutdown
	}

	private void updateOverlay(String text, BufferedImage image) {
		overlay.setText(text);
		overlay.setIcon(image);
	}

	private boolean isBankOpen() {
		Widget bankWidget = client.getWidget(ComponentID.BANK_POPUP);
		return bankWidget != null && !bankWidget.isHidden();
	}

	private boolean isLoginSplashVisible() {
		Widget loginClickToPlay = client.getWidget(ComponentID.LOGIN_CLICK_TO_PLAY_SCREEN_MESSAGE_OF_THE_DAY);
		return loginClickToPlay != null && !loginClickToPlay.isHidden();
	}

	@Subscribe
	public void onItemContainerChanged(ItemContainerChanged event) {
		if (event.getContainerId() == InventoryID.INVENTORY.getId()) {
			int openSpaces = getOpenInventorySpaces();
			if (openSpaces == 0 && !wasInventoryFull) {
				if (!isBankOpen() && !isLoginSplashVisible()) {
					notifier.notify("Your inventory is full!");
				}
				updateOverlay("", INVENTORY_FULL_IMAGE); // Change icon and text
				wasInventoryFull = true;
			} else if (openSpaces > 0) {
				wasInventoryFull = false;
				updateOverlay(String.valueOf(openSpaces), INVENTORY_IMAGE); // Update text and reset icon
			}
		}
	}

	private int getOpenInventorySpaces() {
		ItemContainer container = client.getItemContainer(InventoryID.INVENTORY);
		if (container != null) {
			Item[] items = container.getItems();
			int usedSpaces = (int) Arrays.stream(items).filter(item -> item.getId() != -1).count();
			return INVENTORY_SIZE - usedSpaces;
		}
		return INVENTORY_SIZE; // If container is null, assume all spaces are open
	}
}