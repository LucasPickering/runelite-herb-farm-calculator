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
     * @return True if this patch can't be diseased, false otherwise
     */
    public boolean isDiseaseFree() {
        return this == HerbPatch.TROLL_STRONGHOLD || this == HerbPatch.WEISS; // TODO Hosidius
    }

    /**
     * Calculate the expected number of herbs to be harvested from this patch **assuming it is already fully grown.**
     * I.e. this does *not* take survival chance into account.
     *
     * @return Expected number of herbs yielded on average
     */
    public double calcExpectedYield() {
        return 1.0; // TODO
    }
}
