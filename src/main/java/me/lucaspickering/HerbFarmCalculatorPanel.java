package me.lucaspickering;

import java.util.StringJoiner;
import javax.swing.BoxLayout;
import java.awt.GridLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import lombok.extern.slf4j.Slf4j;
import me.lucaspickering.utils.HerbResult;
import me.lucaspickering.utils.HerbCalculatorResult;
import me.lucaspickering.utils.HerbPatchBuffs;

import java.awt.Color;
import java.text.DecimalFormat;

import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.PluginPanel;

@Slf4j
public class HerbFarmCalculatorPanel extends PluginPanel {

  private static final DecimalFormat PCT_FORMAT = new DecimalFormat("+0%");

  private final Client client;
  private final ClientThread clientThread;
  private final ItemManager itemManager;
  private final HerbFarmCalculator calculator;
  private JPanel uiPanel;

  public HerbFarmCalculatorPanel(Client client, ClientThread clientThread,
      ItemManager itemManager,
      HerbFarmCalculatorConfig config,
      HerbFarmCalculator calculator) {
    super();
    this.client = client;
    this.clientThread = clientThread;
    this.itemManager = itemManager;
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
      HerbCalculatorResult result = this.calculator.calculate();

      // Move UI updating onto the AWT thread
      SwingUtilities.invokeLater(() -> {
        this.renderResult(result);
      });
    });
  }

  /**
   * Draw new results into the UI. This should be called *after* running the
   * calculator, in the AWT thread.
   *
   * @param result
   */
  private void renderResult(HerbCalculatorResult result) {
    log.debug("Rendering calculator result");

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
    JLabel farmingLevelLabel = new JLabel(String.format("Farming level: %d", result.getFarmingLevel()));
    farmingLevelLabel.setForeground(Color.WHITE);
    infoPanel.add(farmingLevelLabel);

    // Add a label for each patch in use
    for (HerbPatchBuffs patch : result.getPatches()) {
      // Generate a label for the patch that includes name, yield buff, xp buff
      StringBuilder text = new StringBuilder(patch.getPatch().getName());
      StringJoiner buffLabels = new StringJoiner(", ");
      if (patch.isDiseaseFree()) {
        buffLabels.add("disease-free");
      }
      if (patch.getYieldBonus() != 0.0) {
        buffLabels.add(PCT_FORMAT.format(patch.getYieldBonus()) + " yield");
      }
      if (patch.getXpBonus() != 0.0) {
        buffLabels.add(PCT_FORMAT.format(patch.getXpBonus()) + " XP");
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
    for (HerbResult herbResult : result.getHerbs()) {
      UIHerbSlot slot = new UIHerbSlot(result.getFarmingLevel(), this.itemManager, herbResult);
      resultsPanel.add(slot);
    }

    this.revalidate();
    this.repaint();
  }
}
