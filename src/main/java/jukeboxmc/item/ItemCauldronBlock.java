package jukeboxmc.item;

import org.jukeboxmc.block.BlockCauldron;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemCauldronBlock extends Item {

    public ItemCauldronBlock() {
        super ( "minecraft:item.cauldron" );
    }

    @Override
    public BlockCauldron getBlock() {
        return new BlockCauldron();
    }
}