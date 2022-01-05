package me.lucaspickering;

import java.awt.BorderLayout;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.util.QuantityFormatter;

/**
 * A visual representation for an herb's results.
 */
public class UIHerbSlot extends JPanel {

  // Ripped from hiscores plugin
  private static final Border EMPTY_BORDER = BorderFactory.createEmptyBorder(7, 7, 7, 7);

  private static final Border GREEN_BORDER = new CompoundBorder(
      BorderFactory.createMatteBorder(0, 4, 0, 0, (ColorScheme.PROGRESS_COMPLETE_COLOR).darker()),
      EMPTY_BORDER);

  private static final Border RED_BORDER = new CompoundBorder(
      BorderFactory.createMatteBorder(0, 4, 0, 0, (ColorScheme.PROGRESS_ERROR_COLOR).darker()),
      EMPTY_BORDER);

  private static final Dimension ICON_SIZE = new Dimension(32, 32);

  private final ItemManager itemManager;
  private final HerbCalculatorResult result;

  public UIHerbSlot(ItemManager itemManager, HerbCalculatorResult result) {
    this.itemManager = itemManager;
    this.result = result;

    // An empty border to provide some padding
    this.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    this.setLayout(new BorderLayout());

    // An inner panel that will hold all the contents
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    innerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    innerPanel.setBorder(RED_BORDER);
    this.add(innerPanel);

    // Add the icon
    JLabel icon = new JLabel();
    itemManager.getImage(result.getHerb().getGrimyHerbItem()).addTo(icon);
    icon.setMinimumSize(ICON_SIZE);
    icon.setMaximumSize(ICON_SIZE);
    icon.setPreferredSize(ICON_SIZE);
    innerPanel.add(icon, BorderLayout.LINE_START);

    // Create a sub-panel for all the info
    JPanel infoPanel = new JPanel(new BorderLayout());
    infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    innerPanel.add(infoPanel, BorderLayout.CENTER);

    // Add the herb name
    JLabel nameLabel = new JLabel(result.getHerb().getName());
    infoPanel.add(nameLabel, BorderLayout.PAGE_START);

    // Add profit
    JLabel profitLabel = new JLabel(QuantityFormatter.formatNumber(result.getProfit()) + " gp");
    infoPanel.add(profitLabel, BorderLayout.LINE_START);

    // Add yield
    JLabel yieldLabel = new JLabel(String.format("%.3f herbs", result.getExpectedYield()));
    infoPanel.add(yieldLabel, BorderLayout.CENTER);

    // Add XP
    JLabel xpLabel = new JLabel(String.format("%.1f XP", result.getExpectedXp()));
    infoPanel.add(xpLabel, BorderLayout.LINE_END);
  }
}
