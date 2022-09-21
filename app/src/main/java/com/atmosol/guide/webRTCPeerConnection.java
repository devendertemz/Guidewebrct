package com.atmosol.guide;

import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;

public class webRTCPeerConnection {
    String id;
    DataChannel dataChannel;
    PeerConnection peerConnection;

    public webRTCPeerConnection(String id, DataChannel dataChannel, PeerConnection peerConnection) {
        this.id = id;
        this.dataChannel = dataChannel;
        this.peerConnection = peerConnection;
    }

    /*public webRTCPeerConnection(String id, DataChannel dataChannel) {
        this.id = id;
        this.dataChannel = dataChannel;
    }*/

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public DataChannel getDataChannel() {
        return dataChannel;
    }

    public void setDataChannel(DataChannel dataChannel) {
        this.dataChannel = dataChannel;
    }

    public PeerConnection getPeerConnection() {
        return peerConnection;
    }

    public void setPeerConnection(PeerConnection peerConnection) {
        this.peerConnection = peerConnection;
    }
}
