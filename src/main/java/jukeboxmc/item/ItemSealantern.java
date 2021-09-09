package jukeboxmc.item;

import org.jukeboxmc.block.BlockSealantern;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemSealantern extends Item {

    public ItemSealantern() {
        super ( "minecraft:sealantern" );
    }

    @Override
    public BlockSealantern getBlock() {
        return new BlockSealantern();
    }
}