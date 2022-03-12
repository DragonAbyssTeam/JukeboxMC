package org.jukeboxmc.item;

import org.jukeboxmc.entity.EntityType;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemDolphinSpawnEgg extends ItemGeneralSpawnEgg {

    public ItemDolphinSpawnEgg() {
        super( "minecraft:dolphin_spawn_egg" );
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.DOLPHIN;
    }
}