package org.jukeboxmc.network.raknet;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import lombok.Getter;
import org.jukeboxmc.network.protocol.Protocol;
import org.jukeboxmc.network.protocol.packet.BatchPacket;
import org.jukeboxmc.network.protocol.packet.LoginPacket;
import org.jukeboxmc.network.raknet.protocol.*;
import org.jukeboxmc.network.raknet.utils.BinaryStream;
import org.jukeboxmc.network.raknet.utils.Zlib;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.zip.DataFormatException;

/**
 * @author LucGamesYT
 * @version 1.0
 */
public class Connection {

    private Listener listener;
    private short mtuSize;
    @Getter
    private InetSocketAddress sender;

    private Status state = Status.CONNECTING;

    private LinkedList<Integer> nackQueue = new LinkedList<>();
    private LinkedList<Integer> ackQueue = new LinkedList<>();

    private ConcurrentHashMap<Integer, DataPacket> recoveryQueue = new ConcurrentHashMap<>();

    private LinkedList<DataPacket> packetToSend = new LinkedList<>();

    private DataPacket sendQueue = new DataPacket();

    private ConcurrentHashMap<Integer, Map<Integer, EncapsulatedPacket>> splitPackets = new ConcurrentHashMap<>();

    private int windowStart = -1;
    private int windowEnd = 2048;
    private int reliableWindowStart = 0;
    private int reliableWindowEnd = 2048;
    private ConcurrentHashMap<Integer, EncapsulatedPacket> reliableWindow = new ConcurrentHashMap<>();
    private int lastReliableIndex = -1;

    private Queue<Integer> receivedWindow = new ConcurrentLinkedQueue<>();

    private int lastSequenceNumber = -1;
    private int sendSequenceNumber = 0;

    private int messageIndex;
    private LinkedList<Integer> channelIndex = new LinkedList<>();

    private int splitID = 0;

    private long lastUpdate;
    private boolean isActive = false;

    private final Queue<org.jukeboxmc.network.protocol.packet.Packet> packets = new ConcurrentLinkedQueue<>();

    public Connection( Listener listener, short mtuSize, InetSocketAddress sender ) {
        this.listener = listener;
        this.mtuSize = mtuSize;
        this.sender = sender;

        this.lastUpdate = System.currentTimeMillis();

        for ( int i = 0; i < 32; i++ ) {
            this.channelIndex.add( 0 );
        }
    }

    public void update( long timestap ) {
        if ( !this.isActive && this.lastUpdate + 5000 < timestap ) {
            this.disconnect( "timeout" );
            return;
        }
        this.isActive = false;

        if ( this.ackQueue.size() > 0 ) {
            ACK packet = new ACK();
            packet.setPackets( this.ackQueue );
            this.sendPacket( packet );
            this.ackQueue.clear();
        }

        if ( this.nackQueue.size() > 0 ) {
            NACK packet = new NACK();
            packet.setPackets( this.nackQueue );
            this.sendPacket( packet );
            this.nackQueue.clear();
        }

        if ( this.packetToSend.size() > 0 ) {
            int limit = 16;
            for ( DataPacket dataPacket : this.packetToSend ) {
                dataPacket.sendTime = timestap;
                dataPacket.write();
                this.recoveryQueue.put( dataPacket.sequenceNumber, dataPacket );
                this.packetToSend.remove( dataPacket );
                this.sendPacket( dataPacket );

                if ( --limit <= 0 ) {
                    break;
                }
            }

            if ( this.packetToSend.size() > 2048 ) {
                this.packetToSend.clear();
            }
        }

        this.recoveryQueue.forEach( ( seq, packet ) -> {
            if ( packet.sendTime < System.currentTimeMillis() - 8000 ) {
                this.packetToSend.add( packet );
                this.recoveryQueue.remove( seq );
            }
        } );

        for ( Integer seq : this.receivedWindow ) {
            if ( seq < this.windowStart ) {
                this.receivedWindow.remove( seq );
            } else {
                break;
            }
        }
        this.sendQueue();
    }


    public void receive( ByteBuf buffer ) {
        this.isActive = true;
        this.lastUpdate = System.currentTimeMillis();

        int packetId = buffer.getUnsignedByte( 0 );

        if ( ( packetId & BitFlags.VALID ) == 0 ) {
            //Ignore
        } else if ( ( packetId & BitFlags.ACK ) != 0 ) {
            this.handleAck( buffer );
        } else if ( ( packetId & BitFlags.NACK ) != 0 ) {
            this.handleNack( buffer );
        } else {
            this.handleDatagram( buffer.asReadOnly() );
        }
    }

    private void handleDatagram( ByteBuf buffer ) {
        DataPacket dataPacket = new DataPacket();
        dataPacket.buffer = buffer;
        dataPacket.read();

        if ( dataPacket.getSequenceNumber() < this.windowStart || dataPacket.getSequenceNumber() > this.windowEnd || this.reliableWindow.containsKey( dataPacket.getSequenceNumber() ) ) {
            return;
        }

        int diff = dataPacket.sequenceNumber - this.lastSequenceNumber;
        int index = this.nackQueue.indexOf( dataPacket.getSequenceNumber() );

        if ( index > -1 ) {
            this.nackQueue.remove( index );
        }

        this.ackQueue.add( dataPacket.sequenceNumber );
        this.receivedWindow.add( dataPacket.sequenceNumber );

        if ( diff != 1 ) {
            for ( int i = this.lastSequenceNumber + 1; i < dataPacket.sequenceNumber; i++ ) {
                if ( !this.receivedWindow.contains( i ) ) {
                    this.nackQueue.add( i );
                }
            }
        }

        if ( diff >= 1 ) {
            this.lastSequenceNumber = dataPacket.sequenceNumber;
            this.windowStart += diff;
            this.windowEnd += diff;
        }

        for ( Object packet : dataPacket.getPackets() ) {
            if ( packet instanceof EncapsulatedPacket ) {
                this.receivePacket( (EncapsulatedPacket) packet ); //Hier könnte was falsch sein
            }
        }
    }

    private void receivePacket( EncapsulatedPacket packet ) {
        if ( packet.messageIndex == -1 ) {
            this.handlePacket( packet );
        } else {
            if ( packet.messageIndex < this.reliableWindowStart || packet.messageIndex > this.reliableWindowEnd ) {
                return;
            }

            if ( packet.messageIndex - this.lastReliableIndex == 1 ) {
                this.lastReliableIndex++;
                this.reliableWindowStart++;
                this.reliableWindowEnd++;
                this.handlePacket( packet );

                if ( this.reliableWindow.size() > 0 ) {
                    ArrayList<Map.Entry<Integer, EncapsulatedPacket>> windows = new ArrayList<>( this.reliableWindow.entrySet() );
                    ConcurrentHashMap<Integer, EncapsulatedPacket> reliableWindow = new ConcurrentHashMap<>();
                    windows.sort( Comparator.comparingInt( Map.Entry::getKey ) );

                    windows.forEach( entry -> {
                        reliableWindow.put( entry.getKey(), entry.getValue() );
                    } );

                    this.reliableWindow = reliableWindow;

                    this.reliableWindow.forEach( ( seqIndex, pk ) -> {
                        if ( seqIndex - this.lastReliableIndex == 1 ) {
                            this.lastReliableIndex++;
                            this.reliableWindowStart++;
                            this.reliableWindowEnd++;
                            this.handlePacket( pk );
                            this.reliableWindow.remove( seqIndex );
                        }
                    } );
                }
            } else {
                this.reliableWindow.put( packet.messageIndex, packet );
            }
        }
    }

    private void handlePacket( EncapsulatedPacket packet ) {
        if ( packet.split ) {
            this.handleSplit( packet );
            return;
        }
        int id = packet.getBuffer().getUnsignedByte( 0 );

        if ( id < 0x80 ) {
            if ( this.state == Status.CONNECTING ) {
                if ( id == Protocol.CONNECTION_REQUEST ) {
                    this.handleConnectionRequest( packet.getBuffer() );
                } else if ( id == Protocol.NEW_INCOMING_CONNECTION ) {
                    NewIncomingConnection dataPacket = new NewIncomingConnection();
                    dataPacket.buffer = packet.getBuffer();
                    dataPacket.read();

                    if ( dataPacket.getAddress().getPort() == this.listener.getAddress().getPort() ) {
                        this.state = Status.CONNECTED;
                    }
                }
            } else if ( id == Protocol.DISCONNECT_NOTIFICATION ) {
                this.disconnect( "Client disconnect" );
            } else if ( id == Protocol.CONNECTED_PING ) {
                this.handleConnectedPing( packet.getBuffer() );
            }
        } else if ( this.state == Status.CONNECTED ) {
            ByteBuf buffer = packet.getBuffer();
            if ( id == 0xfe ) {
                buffer.readerIndex( 1 );
                BatchPacket batchPacket = new BatchPacket();
                batchPacket.buffer = buffer;
                batchPacket.read();

                this.packets.offer( batchPacket );
            }

            org.jukeboxmc.network.protocol.packet.Packet dataPacket;
            while ( ( dataPacket = this.packets.poll() ) != null ) {
                if ( dataPacket instanceof BatchPacket ) {
                    this.processBatch( (BatchPacket) dataPacket );
                }
            }
        }
    }

    private BinaryStream getPacketBinaryStream(ByteBuf byteBuf) {
        int packetLength = new BinaryStream( byteBuf ).readUnsignedVarInt();
        return new BinaryStream( byteBuf.readBytes( packetLength ) );
    }

    private void processBatch( BatchPacket packet ) {
        byte[] data;
        try {
            byte[] payload = packet.payload;
            data = Zlib.infalte( payload );
        } catch ( DataFormatException | IOException e ) {
            e.printStackTrace();
            return;
        }

        ByteBuf buffer = Unpooled.wrappedBuffer( data );

        while ( buffer.readableBytes() > 0 ) {
            BinaryStream binaryStream = this.getPacketBinaryStream( buffer );
                byte packetId = (byte) binaryStream.readUnsignedVarInt();

                if(packetId == 0x01) {
                    System.out.println( "LoginPacket" );
                    LoginPacket loginPacket = new LoginPacket();
                    loginPacket.buffer = binaryStream.buffer;
                    loginPacket.read();
                }

            }
        }

    private void handleSplit( EncapsulatedPacket packet ) {
        if ( this.splitPackets.containsKey( packet.splitID ) ) {
            Map<Integer, EncapsulatedPacket> value = this.splitPackets.get( packet.splitID );
            value.put( packet.splitIndex, packet );
            this.splitPackets.put( packet.splitID, value );
        } else {
            Map<Integer, EncapsulatedPacket> value = new HashMap<>();
            value.put( packet.splitID, packet );
            this.splitPackets.put( packet.splitIndex, value );
        }

        Map<Integer, EncapsulatedPacket> localSplits = this.splitPackets.get( packet.splitID );
        if ( localSplits.size() == packet.splitCount ) {
            EncapsulatedPacket encapsulatedPacket = new EncapsulatedPacket();
            ByteBuf stream = Unpooled.buffer( 0 );
            for ( EncapsulatedPacket packets : localSplits.values() ) {
                stream.writeBytes( packets.getBuffer() );
            }
            this.splitPackets.remove( packet.splitID );
            encapsulatedPacket.buffer = stream;
            this.receivePacket( encapsulatedPacket ); //Oder hier ist was falsch
        }
    }

    private void handleConnectionRequest( ByteBuf buffer ) {
        ConnectionRequest dataPacket = new ConnectionRequest();
        dataPacket.buffer = buffer;
        dataPacket.read();

        ConnectionRequestAccepted packet = new ConnectionRequestAccepted();
        packet.setAddress( this.sender );
        packet.setRequestTimestamp( dataPacket.getRequestTimestamp() );
        packet.setAcceptedTimestamp( System.currentTimeMillis() );
        packet.write();

        EncapsulatedPacket encapsulatedPacket = new EncapsulatedPacket();
        encapsulatedPacket.reliability = 0;
        encapsulatedPacket.buffer = packet.buffer;

        this.addToQueue( encapsulatedPacket, Priority.IMMEDIATE );
    }

    private void handleConnectedPing( ByteBuf buffer ) {
        ConnectedPing dataPacket = new ConnectedPing();
        dataPacket.buffer = buffer;
        dataPacket.read();

        ConnectedPong packet = new ConnectedPong();
        packet.setClientTimestamp( dataPacket.getClientTimestamp() );
        packet.setServerTimestamp( System.currentTimeMillis() );
        packet.write();

        EncapsulatedPacket encapsulatedPacket = new EncapsulatedPacket();
        encapsulatedPacket.reliability = 0;
        encapsulatedPacket.buffer = buffer;

        this.addToQueue( encapsulatedPacket, Priority.IMMEDIATE );
    }

    private void handleAck( ByteBuf buffer ) {
        ACK packet = new ACK();
        packet.buffer = buffer;
        packet.read();

        for ( Integer seq : packet.getPackets() ) {
            this.recoveryQueue.remove( seq );
        }
    }

    private void handleNack( ByteBuf buffer ) {
        NACK packet = new NACK();
        packet.buffer = buffer;
        packet.read();

        for ( Integer seq : packet.getPackets() ) {
            if ( this.recoveryQueue.containsKey( seq ) ) {
                DataPacket dataPacket = this.recoveryQueue.get( seq );
                dataPacket.setSequenceNumber( this.sendSequenceNumber++ );
                dataPacket.setSendTime( System.currentTimeMillis() );
                dataPacket.write();
                this.sendPacket( dataPacket );

                this.recoveryQueue.remove( seq );
            }
        }
    }

    public void disconnect( String timeout ) {
        this.listener.removeConnection( this, timeout );
    }

    private void addEncapsulatedToQueue( EncapsulatedPacket packet, int flags ) {
        if ( packet.reliability == 2 || packet.reliability == 3 || packet.reliability == 4 || packet.reliability == 6 || packet.reliability == 7 ) {
            packet.messageIndex = this.messageIndex++;
            if ( packet.reliability == 3 ) {
                Integer orderChannel = this.channelIndex.get( packet.orderIndex );
                orderChannel++;
                packet.orderChannel = orderChannel;
            }
        }

        if ( packet.getTotalLength() + 4 > this.mtuSize ) {
            Map<Integer, ByteBuf> buffers = new HashMap<>();
            int i = 0;
            int splitIndex = 0;

            while ( i < packet.buffer.capacity() ) {
                buffers.put( ( splitIndex += 1 ) - 1, packet.buffer.slice( i, ( i += this.mtuSize - 60 ) ) );
            }

            int splitID = ++this.splitID % 65536;
            buffers.forEach( ( count, buffer ) -> {
                EncapsulatedPacket encapsulatedPacket = new EncapsulatedPacket();
                encapsulatedPacket.splitID = splitID;
                encapsulatedPacket.split = true;
                encapsulatedPacket.splitCount = buffers.size();
                encapsulatedPacket.reliability = packet.reliability;
                encapsulatedPacket.splitIndex = count;
                encapsulatedPacket.buffer = buffer;
                if ( count > 0 ) {
                    encapsulatedPacket.messageIndex = this.messageIndex++;
                } else {
                    encapsulatedPacket.messageIndex = packet.messageIndex;
                }
                if ( encapsulatedPacket.reliability == 3 ) {
                    encapsulatedPacket.orderChannel = packet.orderChannel;
                    encapsulatedPacket.orderIndex = packet.orderIndex;
                }
                this.addToQueue( encapsulatedPacket, flags | Priority.IMMEDIATE );
            } );
        } else {
            this.addToQueue( packet, flags );
        }
    }

    private void addToQueue( EncapsulatedPacket encapsulatedPacket, int flags ) {
        int priority = flags & 0b1;
        if ( priority == Priority.IMMEDIATE ) {
            DataPacket packet = new DataPacket();
            packet.getPackets().add( encapsulatedPacket.toBinary() );
            this.sendPacket( packet );
            packet.sendTime = System.currentTimeMillis();
            this.recoveryQueue.put( packet.sequenceNumber, packet );
            return;
        }
        int length = this.sendQueue.length();
        if ( length + encapsulatedPacket.getTotalLength() > this.mtuSize ) {
            this.sendQueue();
        }
        this.sendQueue.getPackets().add( encapsulatedPacket.toBinary() );
    }

    private void sendQueue() {
        if ( this.sendQueue.getPackets().size() > 0 ) {
            this.sendQueue.setSequenceNumber( this.sendSequenceNumber++ );
            this.sendPacket( this.sendQueue );
            this.sendQueue.sendTime = System.currentTimeMillis();
            this.recoveryQueue.put( this.sendQueue.getSequenceNumber(), this.sendQueue );
            this.sendQueue = new DataPacket();
        }
    }

    public void sendPacket( Packet packet ) {
        packet.write();
        this.listener.sendBuffer( packet.buffer, this.sender );
    }

    public void close() {
        ByteBuf buffer = Unpooled.buffer( 4 );
        buffer.writeBytes( new byte[]{ 0x00, 0x00, 0x08, 0x15 } );
        EncapsulatedPacket packet = EncapsulatedPacket.fromBinary( buffer );
        this.addEncapsulatedToQueue( packet, Priority.IMMEDIATE );
    }


    public static class Priority {
        public static final int NORMAL = 0;
        public static final int IMMEDIATE = 1;
    }

    public enum Status {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED
    }
}
