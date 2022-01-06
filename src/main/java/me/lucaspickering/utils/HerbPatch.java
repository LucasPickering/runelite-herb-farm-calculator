package me.lucaspickering.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.runelite.api.Client;
import net.runelite.api.Varbits;

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
     * @param client RuneLite client, used to fetch dynamic data
     * @return True if this patch can't be diseased, false otherwise
     */
    public boolean isDiseaseFree(Client client) {
        switch (this) {
            case TROLL_STRONGHOLD:
            case WEISS:
                return true;
            case HOSIDIUS:
                // Values are THOUSANDTHS, not hundreths
                // https://oldschool.runescape.wiki/w/Kourend_Favour#Hosidius_favour_rewards
                return client.getVar(Varbits.KOUREND_FAVOR_HOSIDIUS) >= 500;
            default:
                return false;
        }
    }
}
