package org.jukeboxmc.item;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemShield extends Item {

    public ItemShield() {
        super ( "minecraft:shield" );
    }

    @Override
    public int getMaxAmount() {
        return 1;
    }

}
