package me.lucaspickering;

import com.google.inject.Provides;
import lombok.extern.slf4j.Slf4j;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.ClientToolbar;
import net.runelite.client.ui.NavigationButton;
import net.runelite.client.util.ImageUtil;

import javax.inject.Inject;
import java.awt.image.BufferedImage;

@Slf4j
@PluginDescriptor(
        name = "Herb Farming Calculator",
        tags = {"panel"}
)
public class HerbFarmCalculatorPlugin extends Plugin {

    @Inject
    private Client client;
    @Inject
    private HerbFarmCalculatorConfig config;
    @Inject
    private ClientToolbar clientToolbar;
    private NavigationButton uiNavigationButton;

    @Override
    protected void startUp() throws Exception {
        final BufferedImage icon = ImageUtil.loadImageResource(getClass(), "herb.png");
        final HerbFarmCalculatorPanel uiPanel = new HerbFarmCalculatorPanel(this, this.config);

        this.uiNavigationButton = NavigationButton.builder()
                .tooltip("Herb Farming Calculator")
                .icon(icon)
                .priority(9)
                .panel(uiPanel)
                .build();

        clientToolbar.addNavigation(uiNavigationButton);
    }

    @Override
    protected void shutDown() throws Exception {
        log.info("Herb Farming Calculator stopped");
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
