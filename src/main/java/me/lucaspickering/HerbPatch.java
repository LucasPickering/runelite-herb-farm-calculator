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
        switch (this) {
            case TROLL_STRONGHOLD:
            case WEISS:
                return true;
            // TODO Hosidius
            default:
                return false;
        }
    }
}
