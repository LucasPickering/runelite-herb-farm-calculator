package me.lucaspickering;

import lombok.extern.slf4j.Slf4j;
import net.runelite.client.ui.PluginPanel;

import javax.inject.Inject;
import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class HerbFarmCalculatorPanel extends PluginPanel {
    private final HerbFarmCalculatorPlugin plugin;
    private final HerbFarmCalculatorConfig config;


    private final List<JCheckBox> patchCheckBoxes = new ArrayList<>();

    @Inject
    public HerbFarmCalculatorPanel(HerbFarmCalculatorPlugin plugin, HerbFarmCalculatorConfig config) {
        super();
        this.plugin = plugin;
        this.config = config;


        JPanel uiOptionsPanel = new JPanel(new BorderLayout());
        for (HerbPatch patch : HerbPatch.values()) {
//            Label
            JLabel uiLabel = new JLabel(patch.getName());
            uiLabel.setForeground(Color.WHITE);
            uiOptionsPanel.add(uiLabel);

//            Checkbox
            JCheckBox uiCheckBox = new JCheckBox();
            uiOptionsPanel.add(uiCheckBox);
            this.patchCheckBoxes.add(uiCheckBox);
        }
        this.add(uiOptionsPanel);
        this.add(Box.createRigidArea(new Dimension(0, 5)));
    }
}
