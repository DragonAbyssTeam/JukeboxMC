package org.jukeboxmc.inventory.transaction.action;

import org.jukeboxmc.inventory.Inventory;
import org.jukeboxmc.inventory.transaction.InventoryAction;
import org.jukeboxmc.inventory.transaction.InventoryTransaction;
import org.jukeboxmc.item.Item;
import org.jukeboxmc.player.Player;

import java.util.HashSet;
import java.util.Set;

/**
 * @author CreeperFace
 * @version 1.0
 */
public class SlotChangeAction extends InventoryAction {

    protected Inventory inventory;
    private final int inventorySlot;

    public SlotChangeAction( Inventory inventory, int inventorySlot, Item sourceItem, Item targetItem ) {
        super( sourceItem, targetItem );
        this.inventory = inventory;
        this.inventorySlot = inventorySlot;
    }

    public Inventory getInventory() {
        return this.inventory;
    }

    public int getSlot() {
        return inventorySlot;
    }

    public boolean isValid( Player player ) {
        Item check = inventory.getItem( this.inventorySlot );
        boolean equalsExact = check.equalsExact( this.sourceItem );
        return equalsExact;
    }

    public boolean execute( Player player ) {
        this.inventory.setItem( this.inventorySlot, this.targetItem, false );
        return true;
    }

    public void onExecuteSuccess( Player player ) {
        Set<Player> viewers = new HashSet<>( this.inventory.getViewer() );
        viewers.remove( player );

        for ( Player viewer : viewers ) {
            this.inventory.sendContents( this.inventorySlot, viewer );
        }

    }

    public void onExecuteFail( Player player ) {
        this.inventory.sendContents( this.inventorySlot, player );
    }

    @Override
    public void onAddToTransaction( InventoryTransaction transaction ) {
        transaction.addInventory( this.inventory );
    }
}
