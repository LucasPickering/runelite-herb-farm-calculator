package me.lucaspickering;

import java.util.List;
import javax.inject.Inject;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.runelite.api.Client;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.game.ItemManager;
import net.runelite.client.ui.PluginPanel;

public class HerbFarmCalculatorPanel extends PluginPanel {

  private final Client client;
  private final ClientThread clientThread;
  private final ItemManager itemManager;
  private final HerbFarmCalculator calculator;
  private JPanel resultsPanel;

  @Inject
  public HerbFarmCalculatorPanel(Client client, ClientThread clientThread,
      ItemManager itemManager,
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
    // Run the calculator when the panel is first revealed
    this.refreshResults();
  }

  /**
   * Run the calculator and update the panel to show the results
   */
  private void refreshResults() {
    // Defer running the calculator onto the client thread
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
        this.revalidate();
        this.repaint();
      });
    });
  }
}
