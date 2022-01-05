package me.lucaspickering;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum HerbPatch {
    ARDOUGNE("Ardougne"),
    CATHERBY("Catherby"),
    FALADOR("Falador"),
    FARMING_GUILD("Farming Guild"),
    HARMONY("Harmony"),
    HOSIDIUS("Hosidius"),
    PORT_PHASMATYS("Port Phasmatys"),
    TROLL_STRONGHOLD("Troll Stronghold"),
    WEISS("Weiss");

    private final String name;

    /**
     * Is this patch disease-proof?
     *
     * @param hosidiusFavor Player's favor with Hosidius (as a value [0,1000])
     *                      50+% makes the Hosidius patch disease-free
     * @return True if this patch can't be diseased, false otherwise
     */
    public boolean isDiseaseFree(int hosidiusFavor) {
        switch (this) {
            case TROLL_STRONGHOLD:
            case WEISS:
                return true;
            case HOSIDIUS:
                // Values are THOUSANDTHS, not hundreths
                // https://oldschool.runescape.wiki/w/Kourend_Favour#Hosidius_favour_rewards
                return hosidiusFavor >= 500;
            default:
                return false;
        }
    }
}
