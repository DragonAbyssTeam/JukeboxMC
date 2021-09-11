package org.jukeboxmc.block;

import org.jukeboxmc.block.direction.BlockFace;
import org.jukeboxmc.block.direction.TorchFacing;
import org.jukeboxmc.block.type.UpdateReason;
import org.jukeboxmc.item.Item;
import org.jukeboxmc.item.ItemTorch;
import org.jukeboxmc.math.AxisAlignedBB;
import org.jukeboxmc.math.Vector;
import org.jukeboxmc.player.Player;
import org.jukeboxmc.world.World;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class BlockTorch extends Block {

    public BlockTorch() {
        super( "minecraft:torch" );
    }

    @Override
    public boolean placeBlock( Player player, World world, Vector blockPosition, Vector placePosition, Vector clickedPosition, Item itemIndHand, BlockFace blockFace ) {
        Block block = world.getBlock( blockPosition );

        if ( !block.isTransparent() && blockFace != BlockFace.DOWN ) {
            this.setTorchFacing( blockFace.opposite().torchFacing() );
            world.setBlock( placePosition, this );
            return true;
        }

        if ( !world.getBlock( placePosition.subtract( 0, 1, 0 ) ).isTransparent() ) {
            this.setTorchFacing( TorchFacing.TOP );
            world.setBlock( placePosition, this );
            return true;
        }
        return false;
    }

    @Override
    public long onUpdate( UpdateReason updateReason ) {
        if ( updateReason == UpdateReason.NEIGHBORS ) {
            BlockFace blockFace = this.getTorchFacing().toBlockFace();
            if ( !this.getSide( blockFace.opposite() ).isSolid() ) {
                this.world.setBlock( this.getLocation(), BlockType.AIR.getBlock() );
                return -1;
            }
        }
        return super.onUpdate( updateReason );
    }

    @Override
    public ItemTorch toItem() {
        return new ItemTorch();
    }

    @Override
    public BlockType getBlockType() {
        return BlockType.TORCH;
    }

    @Override
    public boolean isSolid() {
        return false;
    }

    @Override
    public boolean isTransparent() {
        return true;
    }

    @Override
    public boolean canPassThrough() {
        return true;
    }

    @Override
    public AxisAlignedBB getBoundingBox() {
        float size = 0.15f;
        switch ( this.getTorchFacing() ) {
            case EAST:
                return new AxisAlignedBB(
                        this.location.getX(),
                        this.location.getY() + 0.2f,
                        this.location.getZ() + 0.5f - size,
                        this.location.getX() + size * 2f,
                        this.location.getY() + 0.8f,
                        this.location.getZ() + 0.5f + size
                );
            case WEST:
                return new AxisAlignedBB(
                        this.location.getX() + 1.0f - size * 2f,
                        this.location.getY() + 0.2f,
                        this.location.getZ() + 0.5f - size,
                        this.location.getX() + 1f,
                        this.location.getY() + 0.8f,
                        this.location.getZ() + 0.5f + size
                );
            case SOUTH:
                return new AxisAlignedBB(
                        this.location.getX() + 0.5f - size,
                        this.location.getY() + 0.2f,
                        this.location.getZ(),
                        this.location.getX() + 0.5f + size,
                        this.location.getY() + 0.8f,
                        this.location.getZ() + size * 2f
                );
            case NORTH:
                return new AxisAlignedBB(
                        this.location.getX() + 0.5f - size,
                        this.location.getY() + 0.2f,
                        this.location.getZ() + 1f - size * 2f,
                        this.location.getX() + 0.5f + size,
                        this.location.getY() + 0.8f,
                        this.location.getZ() + 1f
                );
        }
        size = 0.1f;

        return new AxisAlignedBB(
                this.location.getX() + 0.5f - size,
                this.location.getY() + 0.0f,
                this.location.getZ() + 0.5f - size,
                this.location.getX() + 0.5f + size,
                this.location.getY() + 0.6f,
                this.location.getZ() + 0.5f + size
        );
    }

    public void setTorchFacing( TorchFacing torchFacing ) {
        this.setState( "torch_facing_direction", torchFacing.name().toLowerCase() );
    }

    public TorchFacing getTorchFacing() {
        return this.stateExists( "torch_facing_direction" ) ? TorchFacing.valueOf( this.getStringState( "torch_facing_direction" ) ) : TorchFacing.TOP;
    }
}