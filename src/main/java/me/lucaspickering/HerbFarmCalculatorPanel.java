package me.lucaspickering;

import java.util.List;
import java.util.StringJoiner;

import javax.inject.Inject;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.Color;
import java.text.DecimalFormat;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

public class HerbFarmCalculatorPanel extends PluginPanel {

  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("+0%");

  private final Client client;
  private final ClientThread clientThread;
  private final ItemManager itemManager;
  private final HerbFarmCalculatorConfig config;
  private final HerbFarmCalculator calculator;
  private JPanel uiPanel;

  @Inject
  public HerbFarmCalculatorPanel(Client client, ClientThread clientThread,
      ItemManager itemManager,
      HerbFarmCalculatorConfig config,
      HerbFarmCalculator calculator) {
    super();
    this.client = client;
    this.clientThread = clientThread;
    this.itemManager = itemManager;
    this.config = config;
    this.calculator = calculator;

    setBorder(new EmptyBorder(10, 10, 10, 10));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
  }

  @Override
  public void onActivate() {
    super.onActivate();
    // Run the calculator whenever the panel is revealed
    this.refreshPanel();
  }

  /**
   * Run the calculator and update the panel to show the results. Called on
   * startup, and login.
   */
  public void refreshPanel() {
    // Defer running the calculator onto the client thread
    clientThread.invokeLater(() -> {
      List<HerbCalculatorResult> results = this.calculator.calculate();

      // Move UI updating onto the AWT thread
      SwingUtilities.invokeLater(() -> {
        this.renderResults(results);
      });
    });
  }

  /**
   * Draw new results into the UI. This should be called *after* running the
   * calculator, in the AWT thread.
   *
   * @param results
   */
  private void renderResults(List<HerbCalculatorResult> results) {
    // Clear previous info
    if (this.uiPanel != null) {
      this.remove(this.uiPanel);
    }
    this.uiPanel = new JPanel();
    this.uiPanel.setLayout(new BoxLayout(this.uiPanel, BoxLayout.Y_AXIS));
    this.add(uiPanel);

    // ===== Render summary panel =====
    JPanel infoPanel = new JPanel();
    infoPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    infoPanel.setLayout(new GridLayout(0, 1));
    infoPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
    this.uiPanel.add(infoPanel);

    // If player isn't logged in, show a warning so they know that results won't
    // be accurate
    if (this.client.getGameState() != GameState.LOGGED_IN) {
      JLabel notLoggedInWarning = new JLabel("Log in for more accurate results");
      notLoggedInWarning.setForeground(ColorScheme.PROGRESS_ERROR_COLOR);
      infoPanel.add(notLoggedInWarning);
    }

    // Farming level
    JLabel farmingLevelLabel = new JLabel(String.format("Farming level: %d", calculator.getFarmingLevel()));
    farmingLevelLabel.setForeground(Color.WHITE);
    infoPanel.add(farmingLevelLabel);

    // Add a label for each patch in use
    for (HerbPatch patch : this.config.patches()) {
      // Generate a label for the patch that includes name, yield buff, xp buff
      // TODO load these from the calculator
      double yieldBonus = 0.10;
      double xpBonus = 0.10;
      StringBuilder text = new StringBuilder(patch.getName());
      StringJoiner buffLabels = new StringJoiner(", ");
      if (yieldBonus != 0.0) {
        buffLabels.add(PCT_FORMAT.format(yieldBonus) + " yield");
      }
      if (xpBonus != 0.0) {
        buffLabels.add(PCT_FORMAT.format(xpBonus) + " XP");
      }

      if (buffLabels.length() > 0) {
        text.append(" (");
        text.append(buffLabels);
        text.append(")");
      }

      JLabel patchLabel = new JLabel(text.toString());
      patchLabel.setForeground(ColorScheme.LIGHT_GRAY_COLOR);
      patchLabel.setFont(FontManager.getRunescapeSmallFont());
      infoPanel.add(patchLabel);
    }

    // ===== Render calculator results =====
    JPanel resultsPanel = new JPanel();
    resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
    this.uiPanel.add(resultsPanel);

    // Add each patch
    for (HerbCalculatorResult result : results) {
      UIHerbSlot slot = new UIHerbSlot(this.calculator.getFarmingLevel(), this.itemManager, result);
      resultsPanel.add(slot);
    }

    this.revalidate();
    this.repaint();
  }
}
