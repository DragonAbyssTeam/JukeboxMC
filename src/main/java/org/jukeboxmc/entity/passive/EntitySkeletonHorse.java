package org.jukeboxmc.entity.passive;

import org.jukeboxmc.entity.EntityLiving;
import org.jukeboxmc.entity.EntityType;

/**
 * @author Kaooot
 * @version 1.0
 */
public class EntitySkeletonHorse extends EntityLiving {

    @Override
    public String getName() {
        return "Skeleton Horse";
    }

    @Override
    public float getWidth() {
        return 1.4f;
    }

    @Override
    public float getHeight() {
        return 1.6f;
    }

    @Override
    public EntityType getEntityType() {
        return EntityType.SKELETON_HORSE;
    }
}