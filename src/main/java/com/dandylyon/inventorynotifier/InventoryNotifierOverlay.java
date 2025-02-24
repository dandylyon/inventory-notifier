package com.dandylyon.inventorynotifier;

import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.Point;
import net.runelite.api.widgets.Widget;
import net.runelite.api.widgets.ComponentID;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import net.runelite.client.ui.overlay.OverlayUtil;

import javax.inject.Inject;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class InventoryNotifierOverlay extends Overlay {
    private final Client client;
    private final ConfigManager configManager;
    private String text;
    private BufferedImage icon;

    @Inject
    public InventoryNotifierOverlay(Client client, InventoryNotifierPlugin plugin, ConfigManager configManager) {
        super(plugin);
        setPosition(OverlayPosition.DYNAMIC);
        setLayer(OverlayLayer.ABOVE_WIDGETS);
        this.client = client;
        this.configManager = configManager;
    }

    @Override
    public Dimension render(Graphics2D graphics) {
        Widget widget = getWidgetToDrawOn();
        if (widget == null || widget.isHidden()) {
            return null;
        }

        setGraphicsFont(graphics);
        drawIcon(graphics, widget);
        drawText(graphics, widget);

        return null;
    }

    private Widget getWidgetToDrawOn() {
        return client.isResized() ? getResizedWidget() : client.getWidget(ComponentID.FIXED_VIEWPORT_INVENTORY_TAB);
    }

    private Widget getResizedWidget() {
        Widget widget = client.getWidget(ComponentID.RESIZABLE_VIEWPORT_INVENTORY_TAB);
        return (widget != null && !widget.isHidden()) ? widget : client.getWidget(ComponentID.RESIZABLE_VIEWPORT_BOTTOM_LINE_INVENTORY_TAB);
    }

    private void setGraphicsFont(Graphics2D graphics) {
        FontType fontType = configManager.getConfiguration("runelite", "infoboxFontType", FontType.class);
        graphics.setFont(fontType.getFont());
    }

    private void drawIcon(Graphics2D graphics, Widget widget) {
        Point iconLocation = calculateIconLocation(widget);
        graphics.drawImage(icon, iconLocation.getX(), iconLocation.getY(), null);
    }

    private Point calculateIconLocation(Widget widget) {
        return new Point(
                (int) widget.getBounds().getCenterX() - (icon.getWidth() / 2),
                (int) widget.getBounds().getMaxY() - icon.getHeight()
        );
    }

    private void drawText(Graphics2D graphics, Widget widget) {
        Point textLocation = calculateTextLocation(graphics, widget);
        OverlayUtil.renderTextLocation(graphics, textLocation, text, Color.WHITE);
    }

    private Point calculateTextLocation(Graphics2D graphics, Widget widget) {
        return new Point(
                (int) widget.getBounds().getCenterX() - (graphics.getFontMetrics().stringWidth(text) / 2),
                (int) widget.getBounds().getMaxY()
        );
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setIcon(BufferedImage icon) {
        this.icon = icon;
    }
}