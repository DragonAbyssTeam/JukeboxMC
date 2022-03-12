package org.jukeboxmc.item;

import org.jukeboxmc.entity.EntityType;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ItemPillagerSpawnEgg extends ItemGeneralSpawnEgg {

    public ItemPillagerSpawnEgg() {
        super( "minecraft:pillager_spawn_egg" );
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.PILLAGER;
    }
}