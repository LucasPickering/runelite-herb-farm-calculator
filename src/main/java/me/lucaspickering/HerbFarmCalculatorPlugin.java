package me.lucaspickering;

import com.google.inject.Provides;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
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

// TODO consistent formatting/linting
// TODO reload results on login

@PluginDescriptor(name = "Herb Farming Calculator", tags = { "panel" }, // TODO tags
        enabledByDefault = false)
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

    @Override
    protected void startUp() throws Exception {
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "herb.png");
        final HerbFarmCalculator calculator = new HerbFarmCalculator(this.client, this.config, itemManager);
        final HerbFarmCalculatorPanel uiPanel = new HerbFarmCalculatorPanel(this.client, this.clientThread,
                this.itemManager,
                calculator);

        this.uiNavigationButton = NavigationButton.builder()
                .tooltip("Herb Farming Calculator")
                .icon(icon)
                .priority(9)
                .panel(uiPanel)
                .build();

        clientToolbar.addNavigation(uiNavigationButton);
    }

    @Subscribe
    public void onGameStateChanged(GameStateChanged gameStateChanged) {
        if (gameStateChanged.getGameState() == GameState.LOGGED_IN) {

        }
    }

    @Provides
    HerbFarmCalculatorConfig provideConfig(ConfigManager configManager) {
        return configManager.getConfig(HerbFarmCalculatorConfig.class);
    }
}
