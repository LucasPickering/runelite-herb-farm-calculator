package me.lucaspickering;

import java.awt.BorderLayout;
import java.awt.Color;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.ColorScheme;
import net.runelite.client.ui.DynamicGridLayout;
import net.runelite.client.ui.PluginPanel;

public class HerbFarmCalculatorPanel extends PluginPanel {

  private final Client client;
  private final ClientThread clientThread;
  private final ItemManager itemManager;
  private final HerbFarmCalculator calculator;
  private final HerbFarmCalculatorConfig config;
  private JPanel resultsPanel;

  @Inject
  public HerbFarmCalculatorPanel(Client client, ClientThread clientThread,
      ItemManager itemManager,
      HerbFarmCalculator calculator, HerbFarmCalculatorConfig config) {
    super();
    this.client = client;
    this.clientThread = clientThread;
    this.itemManager = itemManager;
    this.calculator = calculator;
    this.config = config;

    setBorder(new EmptyBorder(10, 10, 10, 10));
    setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

    // Set up patch config panel
    JPanel patchPanel = new JPanel(new BorderLayout());
    patchPanel.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 0));
    patchPanel.setBackground(ColorScheme.DARKER_GRAY_COLOR);
    patchPanel.setLayout(new DynamicGridLayout(HerbPatch.values().length, 1, 0, 0));
    for (HerbPatch patch : HerbPatch.values()) {
      // Label
      JLabel uiLabel = new JLabel(patch.getName());
      uiLabel.setForeground(Color.WHITE);
      patchPanel.add(uiLabel, BorderLayout.LINE_START);

      // Checkbox
      JCheckBox uiCheckBox = new JCheckBox();
      uiCheckBox.setBackground(ColorScheme.MEDIUM_GRAY_COLOR);
      uiCheckBox.setSelected(this.config.patches().contains(patch)); // Set initial state
      uiCheckBox.addActionListener(
          event -> this.setPatchEnabled(patch, uiCheckBox.isSelected())); // Callback
      patchPanel.add(uiCheckBox, BorderLayout.LINE_END);
    }
    this.add(patchPanel);
  }

  @Override
  public void onActivate() {
    super.onActivate();
    // Run the calculator when the panel is first revealed
    this.refreshResults();
  }

  /**
   * Set a patch as enabled/disabled in the config
   */
  private void setPatchEnabled(HerbPatch patch, boolean enabled) {
    Set<HerbPatch> patches = this.config.patches();
    if (enabled) {
      patches.add(patch);
    } else {
      patches.remove(patch);
    }

    // Make sure to save the mutated set back to the config
    // Not *entirely* sure this is necessary, but just to be safe
    this.config.patches(patches);
  }

  /**
   * Run the calculator and update the panel to show the results
   */
  private void refreshResults() {
    // TODO figure out how to listen for config changes and call this
    // TODO do we want executor.execute instead?
    // https://github.dev/runelite/runelite/blob/master/runelite-client/src/main/java/net/runelite/client/plugins/skillcalculator/UIActionSlot.java
    clientThread.invokeLater(() -> {
      List<HerbCalculatorResult> results = this.calculator.calculate();

      // Move UI updating onto the AWT thread
      SwingUtilities.invokeLater(() -> {
        // Clear previous results and add our new ones
        if (this.resultsPanel != null) {
          this.remove(this.resultsPanel);
        }
        this.resultsPanel = new JPanel();
        this.resultsPanel.setLayout(new BoxLayout(this.resultsPanel, BoxLayout.Y_AXIS));

        for (HerbCalculatorResult result : results) {
          UIHerbSlot slot = new UIHerbSlot(this.client, this.itemManager, result);
          this.resultsPanel.add(slot);
        }
        this.add(this.resultsPanel);
      });
    });
  }
}
