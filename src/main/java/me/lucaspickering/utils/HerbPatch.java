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
    WEISS("Weiss"),
    CIVITAS_ILLA_FORTIS("Civitas illa Fortis");

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
            case CIVITAS_ILLA_FORTIS:
                // Disease free for civitas illa is based on achieving champion rank with the fortis colosseum. 
                // https://oldschool.runescape.wiki/w/Fortis_Colosseum#Glory
                // Glory is stored in VarPlayer value 4130.
                return client.getVarpValue(4130) >= 16000;
            case HOSIDIUS:
                // Disease free is now based on the completion of the Kourend easy diary.
                return client.getVarbitValue(Varbits.DIARY_KOUREND_EASY) == 1;
            default:
                return false;
        }
    }
}
