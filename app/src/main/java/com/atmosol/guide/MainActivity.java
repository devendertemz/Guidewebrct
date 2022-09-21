package com.atmosol.guide;

import static org.webrtc.SessionDescription.Type.ANSWER;
import static io.socket.client.Socket.EVENT_CONNECT;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.atmosol.guide.databinding.ActivityMainBinding;
import com.atmosol.randwebrtclibrary.ToasterMessage;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.AudioSource;
import org.webrtc.AudioTrack;
import org.webrtc.DataChannel;
import org.webrtc.IceCandidate;
import org.webrtc.MediaConstraints;
import org.webrtc.MediaStream;
import org.webrtc.PeerConnection;
import org.webrtc.PeerConnectionFactory;
import org.webrtc.RtpReceiver;
import org.webrtc.SessionDescription;

import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.socket.client.IO;
import io.socket.client.Socket;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;


///ad
public class MainActivity extends AppCompatActivity implements NewViewerListAdapter.SearchListener {

    private static final String TAG = "Guide";

    private ActivityMainBinding binding;
    private Socket mSocket;
    String roomnumber, name, id;

    ArrayList<User> NewViewerList;
    private PeerConnectionFactory factory;
    // Map<String, PeerConnection> map = new HashMap<String, PeerConnection>();

    ArrayList<webRTCPeerConnection> arrayListwebRTCPeerConnection = new ArrayList<>();

    private static final int RC_CALL = 111;

    private DataChannel dataChannel;

    private boolean isStarted = false;

    MediaConstraints audioConstraints;
    AudioSource audioSource;
    AudioTrack localAudioTrack;
    MediaStream mediaStream;


    {
        try {

            // mSocket = IO.socket("http://44.204.153.163:8080");
            mSocket = IO.socket("http://15.161.2.78:80");

        } catch (URISyntaxException e) {

            e.printStackTrace();

        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //  setContentView(R.layout.activity_main);
        mSocket.connect();
        NewViewerList = new ArrayList<User>();

        arrayListwebRTCPeerConnection.clear();

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        ToasterMessage.s(this,"Testing android lib");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SocketStatus();
            }
        }, 2000);

        start();

        binding.buttonbroadcaster.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerasBroadCaster();
            }
        });

        binding.sendall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertBoxWithSendMes("null");
            }
        });


    }

    private void muteORUnmute() {
        AudioTrack curentAudioTrack = mediaStream.audioTracks.get(0);
        curentAudioTrack.setEnabled(false);
        binding.mute.setVisibility(View.VISIBLE);

        binding.unmute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AudioTrack curentAudioTrack = mediaStream.audioTracks.get(0);
                curentAudioTrack.setEnabled(false);

                binding.mute.setVisibility(View.VISIBLE);
                binding.unmute.setVisibility(View.GONE);
            }
        });

        binding.mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                binding.mute.setVisibility(View.GONE);
                binding.unmute.setVisibility(View.VISIBLE);
                AudioTrack curentAudioTrack = mediaStream.audioTracks.get(0);
                curentAudioTrack.setEnabled(true);
            }
        });


    }

    private void doCall(PeerConnection peerConnection) {

       /* MediaConstraints sdpMediaConstraints = new MediaConstraints();

        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveAudio", "true"));
        sdpMediaConstraints.mandatory.add(
                new MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"));
*/


        peerConnection.createOffer(new SimpleSdpObserver() {
            @Override
            public void onCreateSuccess(SessionDescription sessionDescription) {
                Log.e(TAG, "onCreateSuccess: ");
                peerConnection.setLocalDescription(new SimpleSdpObserver(), sessionDescription);
                Log.e(TAG, "doCall: ");
                JSONObject user = new JSONObject();
                JSONObject sdp = new JSONObject();

                JSONObject message = new JSONObject();

                try {
                    message.put("type", "offer");

                    sdp.put("type", "offer");
                    sdp.put("sdp", sessionDescription.description);
                    message.put("sdp", sdp);
                    user.put("room", roomnumber);
                    user.put("name", name);
                    message.put("broadcaster", user);
                    Log.e(TAG, "docall: sending offer " + message);
                    // sendMessage(message);
                    mSocket.emit("offer", id, message);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "docall: JSONException " + e.getLocalizedMessage());

                }


            }
        }, new MediaConstraints());
    }


    private void registerasBroadCaster() {
        name = binding.youname.getText().toString().trim();
        roomnumber = binding.roomnumber.getText().toString().trim();

        if (name.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter name", Toast.LENGTH_SHORT).show();
        } else if (roomnumber.isEmpty()) {
            Toast.makeText(MainActivity.this, "Please enter roon number", Toast.LENGTH_SHORT).show();
        } else {

            if (mSocket.connected()) {


                JSONObject user = new JSONObject();
                try {
                    user.put("room", roomnumber);
                    user.put("name", name);
                    mSocket.emit("register as broadcaster", user);
                    Log.e(TAG, "register as broadcaster " + user);

                    binding.broadcasterstatus.setText("Register as Broadcaster: YES");


                } catch (JSONException e) {
                    e.printStackTrace();
                    binding.broadcasterstatus.setText("Register as Broadcaster: NO");

                }

            } else {
                binding.broadcasterstatus.setText("Register as Broadcaster: NO");

                Toast.makeText(MainActivity.this, "not Connect", Toast.LENGTH_SHORT).show();
            }
        }


    }

    private void SocketStatus() {


        if (mSocket.connected()) {
            binding.socketstatus.setText("Socket Status: YES");

        } else {
            binding.socketstatus.setText("Socket Status: NO");

        }
    }


    private void connectToSignallingServer() {


        mSocket.on(EVENT_CONNECT, args -> {
                    Log.e(TAG, ": connect");
                    //socket.emit("create or join", "foo");
                }).
                on("register as broadcaster", args -> {
                    Log.e(TAG, "register as broadcaster: ");

                }).
                on("register as viewer", args -> {
                    Log.e(TAG, "register as viewer: ");

                })
                .on("new viewer", args -> {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e(TAG, "new viewer: got message " + args[0]);
                            //Toast.makeText(Socket_with_Io.this, args.toString()+"new viewer", Toast.LENGTH_SHORT).show();
                            JSONObject data = (JSONObject) args[0];
                            try {
                                NewViewerList.add(new User(data.getString("id"), data.getString("name"), data.getString("room")));
                                id = data.getString("id");


                                PeerConnection peerConnection = initializePeerConnections(id);
                                startStreamingVideo(peerConnection);


                                //userArrayList.add( new User(id,name,room) );


                                doCall(peerConnection);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                            if (NewViewerList.size() > 0) {
                                binding.viewlistLL.setVisibility(View.VISIBLE);
                                LinearLayoutManager linearLayoutManager = (new LinearLayoutManager(MainActivity.this, LinearLayoutManager.VERTICAL, false));

                                binding.recyclerView.setLayoutManager(linearLayoutManager);
                                binding.recyclerView.setHasFixedSize(true);
                                NewViewerListAdapter searchAdapter = new NewViewerListAdapter(NewViewerList, MainActivity.this, MainActivity.this);
                                binding.recyclerView.setAdapter(searchAdapter);
                            } else {
                                binding.viewlistLL.setVisibility(View.GONE);
                            }


                        }
                    });


                })
                .on("offer", args -> {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//start boardcaster

                            Log.e(TAG, "offer: got message " + args[0]);
                            //  Toast.makeText(Socket_with_Io.this, args.toString() + "offerid", Toast.LENGTH_LONG).show();


                            JSONObject data = (JSONObject) args[0];
/*
                            try {

                                id=data.getJSONObject("broadcaster").getString("id");


                                Log.e(TAG, "sdp: got message " + data.getString("sdp"));

                                PeerConnection peerConnection=initializePeerConnections(data.getJSONObject("broadcaster").getString("id"));
                                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, data.getJSONObject("sdp").getString("sdp")));
                                doAnswer(peerConnection);
                                //  maybeStart();
                                //   peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));


                                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(OFFER, message.getString("sdp")));
                                doAnswer();

                            } catch (JSONException e) {
                                e.printStackTrace();
                                Log.e(TAG, "sdp: JSONException " + e.getLocalizedMessage());

                            }*/


                        }
                    });


                })
                .on("candidate", args -> {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            PeerConnection peerConnection = null;

                            Log.e(TAG, "candidate: got message " + args[0]);

                            //Toast.makeText(Socket_with_Io.this, args[0].toString() + "candidate", Toast.LENGTH_SHORT).show();

                            try {

                                JSONObject data = (JSONObject) args[0];
                                JSONObject event = data.getJSONObject("event");
                                IceCandidate candidate = new IceCandidate(event.getString("id"), event.getInt("label"), event.getString("candidate"));
                                Log.e(TAG, "IceCandidate: send  message " + candidate.toString());

                                //       PeerConnection peerConnection = map.get(data.getString("socket_id"));

                                for (int i = 0; i < arrayListwebRTCPeerConnection.size(); i++) {

                                    if (data.getString("socket_id").equalsIgnoreCase(arrayListwebRTCPeerConnection.get(i).id)) {

                                        peerConnection = arrayListwebRTCPeerConnection.get(i).peerConnection;
                                        Log.e(TAG, "peerConnection: Gotted " + peerConnection);

                                    }


                                }
                                peerConnection.addIceCandidate(candidate);


                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    });


                })

                .on("answer", args -> {


                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {


                            Log.e(TAG, "answer: got message " + args[0]);

                            //  Toast.makeText(Socket_with_Io.this, args.toString() + "offerid", Toast.LENGTH_LONG).show();
                            PeerConnection peerConnection = null;

                            JSONObject data = (JSONObject) args[0];

                            try {

                                Log.e(TAG, "answer: got message " + data.getJSONObject("sdp").getString("sdp"));
                                id = data.getString("viewer_id");


                                for (int i = 0; i < arrayListwebRTCPeerConnection.size(); i++) {

                                    if (data.getString("viewer_id").equalsIgnoreCase(arrayListwebRTCPeerConnection.get(i).id)) {

                                        peerConnection = arrayListwebRTCPeerConnection.get(i).peerConnection;
                                        Log.e(TAG, "peerConnection: answer " + peerConnection);

                                    }


                                }


                                peerConnection.setRemoteDescription(new SimpleSdpObserver(), new SessionDescription(ANSWER, data.getJSONObject("sdp").getString("sdp")));

                            } catch (JSONException e) {
                                e.printStackTrace();
                            }


                        }
                    });


                });

    }


    private PeerConnection initializePeerConnections(String id) {


        PeerConnection peerConnection;
        peerConnection = createPeerConnection(factory, id);
        //map.put(id, peerConnection);

        //peerConnection.getReceivers()
        // for data channel send string


        DataChannel.Init dcInit = new DataChannel.Init();
        dcInit.id = 1;
        dataChannel = peerConnection.createDataChannel("WebRTCData", dcInit);

        dataChannel.registerObserver(new DcObserver());
        arrayListwebRTCPeerConnection.add(new webRTCPeerConnection(id, dataChannel, peerConnection));


        return peerConnection;

    }

    private void initializePeerConnectionFactory() {
        PeerConnectionFactory.initializeAndroidGlobals(this, true, true, true);
        factory = new PeerConnectionFactory(null);
        //factory.setVideoHwAccelerationOptions(rootEglBase.getEglBaseContext(), rootEglBase.getEglBaseContext());
    }

    @Override
    public void onSENDSDPclick(User user) {
        Log.e(TAG, "onSENDSDPclick " + user.id);
        id = user.id;


        AlertBoxWithSendMes(user.id);


        /*PeerConnection peerConnection=initializePeerConnections(id);
        doCall(peerConnection,user.id);*/

        //Toast.makeText(this, user.name+"", Toast.LENGTH_SHORT).show();
    }


    private PeerConnection createPeerConnection(PeerConnectionFactory factory, String id) {
        ArrayList<PeerConnection.IceServer> iceServers = new ArrayList<>();
       /* String URL = "stun:stun.services.mozilla.com";
        String URL2 = "stun:stun.l.google.com:19302";*/


        iceServers.add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun1.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun2.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun3.l.google.com:19302"));
        iceServers.add(new PeerConnection.IceServer("stun:stun4.l.google.com:19302"));


       /* iceServers.add(new PeerConnection.IceServer(URL));
        iceServers.add(new PeerConnection.IceServer(URL2));*/

        PeerConnection.RTCConfiguration rtcConfig = new PeerConnection.RTCConfiguration(iceServers);


        rtcConfig.iceTransportsType = PeerConnection.IceTransportsType.ALL;
        rtcConfig.bundlePolicy = PeerConnection.BundlePolicy.BALANCED;
        rtcConfig.rtcpMuxPolicy = PeerConnection.RtcpMuxPolicy.NEGOTIATE;
        rtcConfig.tcpCandidatePolicy = PeerConnection.TcpCandidatePolicy.ENABLED;
        rtcConfig.candidateNetworkPolicy = PeerConnection.CandidateNetworkPolicy.ALL;
        rtcConfig.audioJitterBufferMaxPackets = 50;
        rtcConfig.audioJitterBufferFastAccelerate = false;
        rtcConfig.iceConnectionReceivingTimeout = -1;
        rtcConfig.keyType = PeerConnection.KeyType.ECDSA;
        rtcConfig.iceCandidatePoolSize = 0;
        rtcConfig.pruneTurnPorts = false;
        rtcConfig.presumeWritableWhenFullyRelayed = false;

        MediaConstraints pcConstraints = new MediaConstraints();

        PeerConnection.Observer pcObserver = new PeerConnection.Observer() {
            @Override
            public void onSignalingChange(PeerConnection.SignalingState signalingState) {
                Log.e(TAG, "onSignalingChange: " + signalingState);
            }

            @Override
            public void onIceConnectionChange(PeerConnection.IceConnectionState iceConnectionState) {
                Log.e(TAG, "onIceConnectionChange: " + iceConnectionState);
            }


            @Override
            public void onIceConnectionReceivingChange(boolean b) {
                Log.e(TAG, "onIceConnectionReceivingChange: ");
            }

            @Override
            public void onIceGatheringChange(PeerConnection.IceGatheringState iceGatheringState) {
                Log.e(TAG, "onIceGatheringChange: " + iceGatheringState);
          /*      if (iceGatheringState.toString().equalsIgnoreCase("COMPLETE")) {
                    Log.e(TAG, "onIceGatheringChangetoString: " + "Yes WiTh complete");

                    PeerConnection peerConnection = map.get(id);
                    List<RtpReceiver> arraylistrtpRecvier = peerConnection.getReceivers();
                    Log.e(TAG, "Codec encodings " + arraylistrtpRecvier.size());


                    for (int i = 0; i < arraylistrtpRecvier.size(); i++) {
                        RtpReceiver rtpReceiver = arraylistrtpRecvier.get(i);
                        for (int j = 0; j < arraylistrtpRecvier.get(i).getParameters().codecs.size(); j++) {
                            Log.e(TAG, "Codec Tourist " + arraylistrtpRecvier.get(i).getParameters().codecs.get(j));
                            //  Log.e(TAG,"Codec Tourist "+rtpReceiver.);

                        }

                    }




                }

       */

            }

            @Override
            public void onIceCandidate(IceCandidate iceCandidate) {
                Log.e(TAG, "onIceCandidate: " + iceCandidate);
                Log.e(TAG, "onIceCandidate:sdpMid:  " + iceCandidate.sdpMid);

                JSONObject message = new JSONObject();


                try {
                    message.put("type", "candidate");
                    message.put("label", String.valueOf(iceCandidate.sdpMLineIndex));
                    message.put("id", iceCandidate.sdpMid);
                    message.put("candidate", iceCandidate.sdp);

                    // sendMessage(message);
                    mSocket.emit("candidate", MainActivity.this.id, message);
                    Log.e(TAG, "onIceCandidate: sending candidate  message" + message);
                    //  Log.e(TAG, "onIceCandidate: sending candidate  id" + id);

                    //maybeStart();


                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(TAG, "onIceCandidate: JSONException " + e.getLocalizedMessage());

                }
            }

            @Override
            public void onIceCandidatesRemoved(IceCandidate[] iceCandidates) {
                Log.e(TAG, "onIceCandidatesRemoved: " + iceCandidates);
            }

            @Override
            public void onAddStream(MediaStream mediaStream) {

                //   Log.e(TAG, "onAddStream: " + mediaStream.videoTracks.size());
                Log.e(TAG, "onAddStream media data: " + mediaStream);
                //  VideoTrack remoteVideoTrack = mediaStream.videoTracks.get(0);
                AudioTrack remoteAudioTrack = mediaStream.audioTracks.get(0);
                remoteAudioTrack.setEnabled(true);

                //remoteVideoTrack.addRenderer(new VideoRenderer(binding.surfaceView2));
                //remoteVideoTrack.setEnabled(true);
                remoteAudioTrack.setEnabled(true);


            }

            @Override
            public void onRemoveStream(MediaStream mediaStream) {
                Log.e(TAG, "onRemoveStream: " + mediaStream);
            }

            @Override
            public void onDataChannel(DataChannel dataChannel) {

                Log.e(TAG, "onDataChannelPeerconnection: " + dataChannel.label());


                dataChannel.registerObserver(new DcObserver());

            }


            @Override
            public void onRenegotiationNeeded() {
                Log.e(TAG, "onRenegotiationNeeded: ");
            }


        };

        return factory.createPeerConnection(rtcConfig, pcConstraints, pcObserver);
    }


    private class DcObserver implements DataChannel.Observer {

        @Override
        public void onMessage(final DataChannel.Buffer buffer) {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {


                    ByteBuffer data = buffer.data;
                    byte[] bytes = new byte[data.remaining()];
                    data.get(bytes);
                    final String command = new String(bytes);

                    GetMsssageWithUI(command);


                }
            });

        }

        @Override
        public void onBufferedAmountChange(long l) {

        }

        @Override
        public void onStateChange() {
            Log.e(TAG, "DataChannel: onStateChange: " + dataChannel.state());
        }
    }

    public void sendData(String s, final String data) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show();

        ByteBuffer buffer = ByteBuffer.wrap(data.getBytes());

        Log.e(TAG, "dataChannel size" + arrayListwebRTCPeerConnection.size());
        for (int i = 0; i < arrayListwebRTCPeerConnection.size(); i++) {
            if (s.equalsIgnoreCase("null")) {

                Log.e(TAG, " dataChannel" + arrayListwebRTCPeerConnection.get(i).dataChannel);
                Log.e(TAG, " dataChannel id" + arrayListwebRTCPeerConnection.get(i).id);

                DataChannel dataChannel = arrayListwebRTCPeerConnection.get(i).dataChannel;
                dataChannel.send(new DataChannel.Buffer(buffer, false));


            } else {
                if (s.equalsIgnoreCase(arrayListwebRTCPeerConnection.get(i).id)) {

                    DataChannel dataChannel = arrayListwebRTCPeerConnection.get(i).dataChannel;
                    Toast.makeText(this, dataChannel + " match", Toast.LENGTH_SHORT).show();
                    dataChannel.send(new DataChannel.Buffer(buffer, false));
                }

            }
        }


    }

    public void GetMsssageWithUI(String message) {
        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        //AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle("MESSAGE");

        // Setting Dialog Message
        alertDialog.setMessage(message);
        //alertDialog.setView(input);

        // Setting Icon to Dialog

        //alertDialog.setIcon(R.drawable.key);

        // Setting Positive "Yes" Button

        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Ok",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        // closed

        // Showing Alert Message
        alertDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mSocket.disconnect();
        Toast.makeText(this, "desiconnect", Toast.LENGTH_SHORT).show();

        //    mSocket.off("connection",onNewMessage);


    }

    @AfterPermissionGranted(RC_CALL)
    private void start() {
        String[] perms = {Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO};
        if (EasyPermissions.hasPermissions(this, perms)) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            audioManager.setSpeakerphoneOn(true);


            Toast.makeText(this, "Yesss", Toast.LENGTH_SHORT).show();
            connectToSignallingServer();
            initializePeerConnectionFactory();
            createVideoTrackFromCameraAndShowIt();


        } else {
            connectToSignallingServer();
            initializePeerConnectionFactory();
            createVideoTrackFromCameraAndShowIt();

            Toast.makeText(this, "Noo", Toast.LENGTH_SHORT).show();

            EasyPermissions.requestPermissions(this, "Need some permissions", RC_CALL, perms);
        }
    }

    private void createVideoTrackFromCameraAndShowIt() {
        audioConstraints = new MediaConstraints();
       /* VideoCapturer videoCapturer = createVideoCapturer();
        VideoSource videoSource = factory.createVideoSource(videoCapturer);
        videoCapturer.startCapture(VIDEO_RESOLUTION_WIDTH, VIDEO_RESOLUTION_HEIGHT, FPS);

        videoTrackFromCamera = factory.createVideoTrack(VIDEO_TRACK_ID, videoSource);
        videoTrackFromCamera.setEnabled(true);
        videoTrackFromCamera.addRenderer(new VideoRenderer(binding.surfaceView));
*/
        //create an AudioSource instance
        audioSource = factory.createAudioSource(audioConstraints);
        localAudioTrack = factory.createAudioTrack("audio0", audioSource);

    }

    private void startStreamingVideo(PeerConnection peerConnection) {

        mediaStream = factory.createLocalMediaStream("stream");

        //mediaStream.addTrack(videoTrackFromCamera);
        mediaStream.addTrack(localAudioTrack);
        peerConnection.addStream(mediaStream);
        if (isStarted == false) {
            muteORUnmute();
            isStarted = true;

        }


    }

    public void AlertBoxWithSendMes(String id) {
        // Creating alert Dialog with one Button
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);

        //AlertDialog alertDialog = new AlertDialog.Builder(MainActivity.this).create();

        // Setting Dialog Title
        alertDialog.setTitle("MESSAGE");

        // Setting Dialog Message
        alertDialog.setMessage("Enter Message");

        final EditText input = new EditText(MainActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        input.setLayoutParams(lp);
        alertDialog.setView(input);

        //alertDialog.setView(input);

        // Setting Icon to Dialog

        //alertDialog.setIcon(R.drawable.key);

        // Setting Positive "Yes" Button
        alertDialog.setPositiveButton("Sending",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog

                        if (input.getText().toString().isEmpty()) {
                            Toast.makeText(MainActivity.this, "Enter message", Toast.LENGTH_SHORT).show();
                        } else {

                            sendData(id, input.getText().toString());


                            dialog.cancel();
                        }


                    }
                });
        // Setting Negative "NO" Button
        alertDialog.setNegativeButton("Cancel",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        // Write your code here to execute after dialog
                        dialog.cancel();
                    }
                });

        // closed

        // Showing Alert Message
        alertDialog.show();
    }
}