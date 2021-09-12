package org.jukeboxmc.inventory;

import org.jukeboxmc.blockentity.BlockEntityShulkerBox;
import org.jukeboxmc.math.Location;
import org.jukeboxmc.network.packet.BlockEventPacket;
import org.jukeboxmc.player.Player;
import org.jukeboxmc.world.LevelSound;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class ShulkerBoxInventory extends ContainerInventory {

    public ShulkerBoxInventory( InventoryHolder holder ) {
        super( holder, -1, 27 );
    }

    @Override
    public BlockEntityShulkerBox getInventoryHolder() {
        return (BlockEntityShulkerBox) this.holder;
    }

    @Override
    public InventoryType getInventoryType() {
        return InventoryType.SHULKER_BOX;
    }

    @Override
    public WindowTypeId getWindowTypeId() {
        return WindowTypeId.CONTAINER;
    }

    @Override
    public void onOpen( Player player ) {
        if ( this.viewer.size() == 1 ) {
            Location location = this.getInventoryHolder().getBlock().getLocation();

            BlockEventPacket blockEventPacket = new BlockEventPacket();
            blockEventPacket.setPosition( location );
            blockEventPacket.setData1( 1 );
            blockEventPacket.setData2( 2 );
            for ( Player players : player.getWorld().getPlayers() ) {
                player.getWorld().playSound( players, LevelSound.SHULKERBOX_OPEN );
            }
            player.getWorld().sendChunkPacket( location.getBlockX() >> 4, location.getBlockZ() >> 4, blockEventPacket );
        }
    }

    @Override
    public void onClose( Player player ) {
        if ( this.viewer.size() == 1 ) {
            Location location = this.getInventoryHolder().getBlock().getLocation();

            BlockEventPacket blockEventPacket = new BlockEventPacket();
            blockEventPacket.setPosition( location );
            blockEventPacket.setData1( 1 );
            blockEventPacket.setData2( 0 );
            for ( Player players : player.getWorld().getPlayers() ) {
                player.getWorld().playSound( players, LevelSound.SHULKERBOX_CLOSED );
            }
            player.getWorld().sendChunkPacket( location.getBlockX() >> 4, location.getBlockZ() >> 4, blockEventPacket );
        }
    }
}