package org.jukeboxmc.item;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemWrittenBook extends Item {

    public ItemWrittenBook() {
        super ( "minecraft:written_book" );
    }

    @Override
    public int getMaxAmount() {
        return 16;
    }

}
