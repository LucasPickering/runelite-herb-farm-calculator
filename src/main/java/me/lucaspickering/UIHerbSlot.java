package me.lucaspickering;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.text.DecimalFormat;
import java.awt.Dimension;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.Color;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;

import me.lucaspickering.utils.HerbResult;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;

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

  private static DecimalFormat GP_FORMAT = new DecimalFormat("+#,###;-#,###");

  public UIHerbSlot(int farmingLevel, ItemManager itemManager, HerbResult result) {
    // An empty border to provide some padding
    this.setBorder(BorderFactory.createEmptyBorder(5, 0, 0, 0));
    this.setLayout(new BorderLayout());

    // An inner panel that will hold all the contents
    JPanel innerPanel = new JPanel();
    innerPanel.setLayout(new BorderLayout());
    innerPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    innerPanel.setBorder(farmingLevel >= result.getHerb().getLevel() ? GREEN_BORDER : RED_BORDER);
    this.add(innerPanel);

    // Add the icon
    JLabel icon = new JLabel();
    itemManager.getImage(result.getHerb().getGrimyHerbItem()).addTo(icon);
    icon.setMinimumSize(ICON_SIZE);
    icon.setMaximumSize(ICON_SIZE);
    icon.setPreferredSize(ICON_SIZE);
    innerPanel.add(icon, BorderLayout.LINE_START);

    // Create a sub-sub-panel for all the text info
    JPanel infoPanel = new JPanel(new GridLayout(2, 2));
    infoPanel.setBorder(new EmptyBorder(0, 5, 0, 0));
    infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    innerPanel.add(infoPanel, BorderLayout.CENTER);

    // Add the herb name
    JLabel nameLabel = new JLabel(result.getHerb().getName());
    nameLabel.setForeground(Color.WHITE);
    infoPanel.add(nameLabel);

    // Add profit
    JLabel profitLabel = new JLabel(GP_FORMAT.format(result.getProfit()) + " gp");
    profitLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    profitLabel.setForeground(UIHerbSlot.getProfitColor(result.getProfit()));
    infoPanel.add(profitLabel);

    // Add yield
    JLabel yieldLabel = new JLabel(String.format("%.1f herbs", result.getExpectedYield()));
    yieldLabel.setFont(FontManager.getRunescapeSmallFont());
    yieldLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    infoPanel.add(yieldLabel);

    // Add XP
    JLabel xpLabel = new JLabel(String.format("%.1f XP", result.getExpectedXp()));
    xpLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    xpLabel.setFont(FontManager.getRunescapeSmallFont());
    xpLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
    infoPanel.add(xpLabel);
  }

  private static Color getProfitColor(double profit) {
    if (profit > 0.0) {
      return ColorScheme.PROGRESS_COMPLETE_COLOR;
    }
    if (profit < 0.0) {
      return ColorScheme.PROGRESS_ERROR_COLOR;
    }
    return Color.WHITE;
  }
}
