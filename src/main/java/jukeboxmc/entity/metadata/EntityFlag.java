package jukeboxmc.entity.metadata;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public enum EntityFlag {

    ON_FIRE( 0 ),
    SNEAKING( 1 ),
    RIDING(2 ),
    SPRINTING( 3 ),
    ACTION( 4 ),
    INVISIBLE( 5 ),
    TEMPTED( 6 ),
    IN_LOVE( 7 ),
    SADDLED( 8 ),
    POWERED( 9 ),
    IGNITED( 10 ),
    BABY( 11 ),
    CONVERTING( 12 ),
    CRITICAL( 13 ),
    SHOW_NAMETAG( 14 ),
    SHOW_ALWAYS_NAMETAG( 15 ),
    IMMOBILE( 16 ),
    SILENT( 17 ),
    WALL_CLIMBING( 18 ),
    CAN_CLIMB( 19 ),
    CAN_SWIM( 20 ),
    CAN_FLY( 21 ),
    CAN_WALK( 22 ),
    RESTING( 23 ),
    SITTING( 24 ),
    ANGRY( 25 ),
    INTERESTED( 26 ),
    CHARGED( 27 ),
    TAMED( 28 ),
    ORPHANED( 29 ),
    LEASHED( 30 ),
    SHEARED( 31 ),
    GLIDING( 32 ),
    ELDER( 33 ),
    MOVING( 34 ),
    BREATHING( 35 ),
    CHESTED( 36 ),
    STACKABLE( 37 ),
    SHOW_BOTTOM( 38 ),
    STANDING( 39 ),
    SHAKING( 40 ),
    IDLING( 41 ),
    CASTING( 42 ),
    CHARGING( 43 ),
    WASD_CONTROLLED( 44 ),
    CAN_POWER_JUMP( 45 ),
    LINGERING( 46 ),
    HAS_COLLISION( 47 ),
    HAS_GRAVITY( 48 ),
    FIRE_IMMUNE( 49 ),
    DANCING( 50 ),
    ENCHANTED( 51 ),
    RETURN_TRIDENT( 52 ),
    CONTAINER_IS_PRIVATE( 53 ),
    IS_TRANSFORMING( 54 ),
    DAMAGE_NEARBY_MOBS( 55 ),
    SWIMMING( 56 ),
    BRIBED( 57 ),
    IS_PREGNANT( 58 ),
    LAYING_EGG( 59 ),
    RIDER_CAN_PICK( 60 ),
    TRANSITION_SITTING( 61 ),
    EATING( 62 ),
    LAYING_DOWN( 63 ),
    SNEEZING( 64 ),
    TRUSTING( 65 ),
    ROLLING( 66 ),
    SCARED( 67 ),
    IN_SCAFFOLDING( 68 ),
    OVER_SCAFFOLDING( 69 ),
    FALL_THROUGH_SCAFFOLDING( 70 ),
    BLOCKING( 71 ),
    TRANSITION_BLOCKING( 72 ),
    BLOCKED_USING_SHIELD( 73 ),
    BLOCKED_USING_DAMAGED_SHIELD( 74 ),
    SLEEPING( 75 ),
    WANTS_TO_WAKE( 76 ),
    TRADE_INTEREST( 77 ),
    DOOR_BREAKER( 78 ),
    BREAKING_OBSTRUCTION( 79 ),
    DOOR_OPENER( 80 ),
    IS_ILLAGER_CAPTAIN( 81 ),
    STUNNED( 82 ),
    ROARING( 83 ),
    DELAYED_ATTACK( 84 ),
    IS_AVOIDING_MOBS( 85 ),
    IS_AVOIDING_BLOCK( 86 ),
    FACING_TARGET_TO_RANGE_ATTACK( 87 ),
    HIDDEN_WHEN_INVISIBLE( 88 ),
    IS_IN_UI( 89 ),
    STALKING( 90 ),
    EMOTING( 91 ),
    CELEBRATING( 92 ),
    ADMIRING( 93 ),
    CELEBRATING_SPECIAL( 64 ),
    RAM_ATTACK( 96 );

    private int id;

    EntityFlag( int id ) {
        this.id = id;
    }

    public int getId() {
        return this.id;
    }
}