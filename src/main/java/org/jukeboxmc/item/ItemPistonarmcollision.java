package org.jukeboxmc.item;

import org.jukeboxmc.block.BlockPistonarmcollision;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemPistonarmcollision extends Item {

    public ItemPistonarmcollision() {
        super ( "minecraft:piston_arm_collision" );
    }

    @Override
    public BlockPistonarmcollision getBlock() {
        return new BlockPistonarmcollision();
    }
}
