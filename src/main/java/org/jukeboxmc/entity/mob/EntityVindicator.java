package org.jukeboxmc.entity.mob;

import org.jukeboxmc.entity.EntityLiving;
import org.jukeboxmc.entity.EntityType;

/**
 * @author Kaooot
 * @version 1.0
 */
public class EntityVindicator extends EntityLiving {

    @Override
    public String getName() {
        return "Vindicator";
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        return 1.9f;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.VINDICATOR;
    }
}