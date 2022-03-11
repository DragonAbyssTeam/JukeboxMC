package org.jukeboxmc.entity.passive;

import org.jukeboxmc.entity.EntityLiving;
import org.jukeboxmc.entity.EntityType;

/**
 * @author Kaooot
 * @version 1.0
 */
public class EntityIronGolem extends EntityLiving {

    @Override
    public String getName() {
        return "Iron Golem";
    }

    @Override
    public float getWidth() {
        return 1.4f;
    }

    @Override
    public float getHeight() {
        return 2.9f;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.IRON_GOLEM;
    }
}