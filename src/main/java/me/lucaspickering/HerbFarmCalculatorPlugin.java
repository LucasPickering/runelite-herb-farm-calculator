package me.lucaspickering;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.api.events.GameTick;
import net.runelite.client.callback.ClientThread;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.game.ItemManager;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

@PluginDescriptor(name = "Herb Farming Calculator", tags = { "panel", "herb", "farming",
        "calculator" }, enabledByDefault = false)
public class HerbFarmCalculatorPlugin extends Plugin {

    @Inject
    private Client client;
    @Inject
    ClientThread clientThread;
    @Inject
    private HerbFarmCalculatorConfig config;
    @Inject
    private ItemManager itemManager;
    @Inject
    private ClientToolbar clientToolbar;
    private NavigationButton uiNavigationButton;
    private HerbFarmCalculatorPanel uiPanel;
    private boolean refreshCalculator = false;

    @Override
    protected void startUp() throws Exception {
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "herb.png");
        final HerbFarmCalculator calculator = new HerbFarmCalculator(this.client, this.config, itemManager);
        this.uiPanel = new HerbFarmCalculatorPanel(this.client, this.clientThread,
                this.itemManager,
                this.config,
                calculator);

        this.uiNavigationButton = NavigationButton.builder()
                .tooltip("Herb Farming Calculator")
                .icon(icon)
                .priority(9)
                .panel(this.uiPanel)
                .build();

        clientToolbar.addNavigation(uiNavigationButton);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {
            // Refresh the panel when the player logs in, so we can grab all
            // their personal stats/buffs. We have to defer this until the first
            // game tick after login though, otherwise we don't actually get
            // full access to stats/diaries/etc.
            refreshCalculator = true;
        }
    }

    @Subscribe
    public void onGameTick(GameTick event) {
        // Run the calculator on first game tick after login
        if (refreshCalculator) {
            refreshCalculator = false;
            this.uiPanel.refreshPanel();
        }
    }

    @Provides
    HerbFarmCalculatorConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HerbFarmCalculatorConfig.class);
    }
}
