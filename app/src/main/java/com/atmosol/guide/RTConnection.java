package com.atmosol.guide;

import android.content.Context;

import org.webrtc.DataChannel;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;

import java.util.ArrayList;
import java.util.List;

public class RTConnection {
/*
    private static Context context;
    private PeerConnectionFactory peerConnectionFactory;
    private PeerConnection peerConnection;
    private DataChannel dataChannel;

    private RTConnection(Context c) {
        context = c;
    }

    public static synchronized RTConnection getInstance(Context context) {

        if (rtConnection == null) {
            rtConnection = new RTConnection(context);
        }
        return rtConnection;
    }

    public void initialize(){

        PeerConnectionFactory.InitializationOptions initializationOptions =
                PeerConnectionFactory.InitializationOptions.builder(context)
                        .createInitializationOptions();
        PeerConnectionFactory.initialize(initializationOptions);

        PeerConnectionFactory.Options options = new PeerConnectionFactory.Options();
        peerConnectionFactory = PeerConnectionFactory.builder().setOptions(options)
                .createPeerConnectionFactory();

        List<PeerConnection.IceServer> iceServers = new ArrayList<>();
        iceServers.add(PeerConnection.IceServer.builder("stun:stun2.1.google.com:19302").createIceServer());

        PeerConnection.RTCConfiguration rtcConfig =
                new PeerConnection.RTCConfiguration(iceServers);

        peerConnection = peerConnectionFactory.createPeerConnection(rtcConfig, new CustomPeerConnectionObserver() {

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                super.onIceCandidate(iceCandidate);
                //DataPacks.RTCIceCandidateDataPack rtcIceCandidateDataPack = new DataPacks.RTCIceCandidateDataPack();
                //rtcIceCandidateDataPack.candidate = iceCandidate.sdp;
                //rtcIceCandidateDataPack.sdpMid = iceCandidate.sdpMid;
                //rtcIceCandidateDataPack.sdpMlineIndex = iceCandidate.sdpMLineIndex;
                //EventBus.getDefault().post(rtcIceCandidateDataPack);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {
                super.onDataChannel(dataChannel);
                //DataPacks.RTCDataChannelDataPack dataChannelDataPack = new DataPacks.RTCDataChannelDataPack();
                //dataChannelDataPack.dataChannel = dataChannel;
                //EventBus.getDefault().post(dataChannelDataPack);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                super.onIceConnectionChange(iceConnectionState);
                if(iceConnectionState != null){
                    //DataPacks.RTCIceStateChange rtcIceStateChange = new DataPacks.RTCIceStateChange();
                    if(iceConnectionState == PeerConnection.IceConnectionState.CONNECTED){
                        //rtcIceStateChange.isClose = false;
                        //EventBus.getDefault().post(rtcIceStateChange);
                    }
                    if(iceConnectionState == PeerConnection.IceConnectionState.CLOSED){
                        //Session.getInstance().setBackgroundCloseStatus(true);
                        //rtcIceStateChange.isClose = true;
                        //EventBus.getDefault().post(rtcIceStateChange);
                    }
                    if(iceConnectionState == PeerConnection.IceConnectionState.FAILED){
                        //Session.getInstance().setBackgroundCloseStatus(true);
                        //rtcIceStateChange.isClose = true;
                        //EventBus.getDefault().post(rtcIceStateChange);
                    }
                }
            }
        });

        if(peerConnection == null){
            //DataPacks.RTCErrorDataPack rtcErrorDataPack = new DataPacks.RTCErrorDataPack();
            //rtcErrorDataPack.error = "peerConnection is null";
            //EventBus.getDefault().post(rtcErrorDataPack);
            return;
        }

        DataChannel.Init dcInit = new DataChannel.Init();
        dataChannel = peerConnection.createDataChannel("1", dcInit);
        dataChannel.registerObserver(new CustomDataChannelObserver(){

            @Override
            public void onMessage(DataChannel.Buffer buffer) {
                super.onMessage(buffer);
                //DataPacks.RTCMessageDataPack rtcMessageDataPack = new DataPacks.RTCMessageDataPack();
                //rtcMessageDataPack.buffer = buffer;
                //EventBus.getDefault().post(buffer);
            }
        });
    }

    public void createOffer(){
        if(peerConnection == null){
            //DataPacks.RTCErrorDataPack rtcErrorDataPack = new DataPacks.RTCErrorDataPack();
            r//tcErrorDataPack.error = "peerConnection is null, can't able to create offer";
            //EventBus.getDefault().post(rtcErrorDataPack);
            return;
        }

        peerConnection.createOffer(new CustomSdpObserver(){

            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                super.onCreateSuccess(sessionDescription);
                //genericMethods.Logd("RTConnection", "createOffer", sessionDescription.description);
                peerConnection.setLocalDescription(new CustomSdpObserver(), sessionDescription);
                //DataPacks.RTCOfferCreatedDataPack offerCreatedDataPack = new DataPacks.RTCOfferCreatedDataPack();
                //offerCreatedDataPack.sdp = sessionDescription.description;
                //EventBus.getDefault().post(offerCreatedDataPack);
            }

            @Override
            public void onCreateFailure(String s) {
                super.onCreateFailure(s);
                //DataPacks.RTCErrorDataPack rtcErrorDataPack = new DataPacks.RTCErrorDataPack();
                //rtcErrorDataPack.error = "offer creation failed";
                // EventBus.getDefault().post(rtcErrorDataPack);
            }
        }, new MediaConstraints());
    }

    public void setRemoteAnswer(String sessionDescription){
        if(peerConnection == null){
            //DataPacks.RTCErrorDataPack rtcErrorDataPack = new DataPacks.RTCErrorDataPack();
            //rtcErrorDataPack.error = "peerConnection is null, can't able to set answer";
            //EventBus.getDefault().post(rtcErrorDataPack);
            return;
        }

        peerConnection.setRemoteDescription(new CustomSdpObserver(), new SessionDescription(SessionDescription.Type.ANSWER, sessionDescription));
    }

    public void setIceCandidate(String candidate, String sdpmid, int sdpmlineIndex){
        if(peerConnection == null){
            //DataPacks.RTCErrorDataPack rtcErrorDataPack = new DataPacks.RTCErrorDataPack();
            //rtcErrorDataPack.error = "peerConnection is null, can't able to set ice candidate";
            //EventBus.getDefault().post(rtcErrorDataPack);
            return;
        }

        peerConnection.addIceCandidate(new IceCandidate(sdpmid, sdpmlineIndex, candidate));
    }

    public void sendData(String message){
        if(peerConnection == null || dataChannel == null){
//            DataPacks.RTCErrorDataPack rtcErrorDataPack = new DataPacks.RTCErrorDataPack();
//            rtcErrorDataPack.error = "peerConnection is null, can't able to send data";
//            EventBus.getDefault().post(rtcErrorDataPack);
            return;
        }

        ByteBuffer buffer = ByteBuffer.wrap(message.getBytes());
        dataChannel.send(new DataChannel.Buffer(buffer, false));
    }

    public void close(){
        if(peerConnection == null){
            return;
        }

        if(dataChannel == null){
            peerConnection.close();
            peerConnection = null;
        } else {
            dataChannel.close();
            peerConnection.close();
            peerConnection = null;
        }
    }*/
}