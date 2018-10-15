package com.example.nilss.friendsintheworld;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.nilss.friendsintheworld.Pojos.TextMessage;
import com.example.nilss.friendsintheworld.Pojos.User;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

public class TCPConnection extends Service{
    private static final String TAG = "TCPConnection";
    public static final String IP="IP",PORT="PORT"; //
    private String ip;
    private int connectionPort;
    private RunOnThread runOnThread;
    private Receive receive;
    private Buffer<String> receiveBuffer;
    private Socket socket;
/*    private ObjectInputStream input;
    private ObjectOutputStream output;*/
    private InputStream input;
    private OutputStream output;
    private DataInputStream dataIS;
    private DataOutputStream dataOS;
    private InetAddress address;
    private User currentUser;
    private ArrayList<TextMessage> textMessages;
    private Timer timer;

    //You can say that this is the "constructor".
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.ip = intent.getStringExtra(IP);
        this.connectionPort = Integer.parseInt(intent.getStringExtra(PORT));
        receiveBuffer = new Buffer<String>();       //have to convert an incoming string to a JSON Object
        runOnThread = new RunOnThread();
        timer = new Timer();
        currentUser = new User("Filip", new ArrayList<String>(), new ArrayList<String>());
        //currentUser.addGroupName("Los Amigos"); //TEMP
        textMessages = new ArrayList<>();
        /*textMessages.add(new TextMessage("Los Amigos", "Clas", "hey everyone!", null)); //TEMP
        textMessages.add(new TextMessage("Los Amigos", "Britta", "hey Clas!", null));
        textMessages.add(new TextMessage("Los Amigos", "Clas", "This app rocks!", null));
        textMessages.add(new TextMessage("Los Amigos", "Britta", "No it sucks!", null));*/
        Log.d(TAG, "onStartCommand: Initializing...");
        return Service.START_STICKY;            //LU
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new LocalService();
    }


    public class LocalService extends Binder {
        public TCPConnection getService() {
            return TCPConnection.this;
        }
    }

    public void startLocationHandler() {
        //TBC
    }


    public void setCurrentUser(User currentUser){
        this.currentUser = currentUser;
    }

    public User getCurrentUser(){
        return currentUser;
    }

    public void setTextMessages(ArrayList<TextMessage> textMessages) {
        this.textMessages = textMessages;
    }

    public ArrayList<TextMessage> getTextMessages() {
        return textMessages;
    }


    public void connect() {
        runOnThread.start();
        runOnThread.execute(new Connect());
    }

    public void disconnect() {
        runOnThread.execute(new Disconnect());
    }

    public void sendMessage(String message){
        runOnThread.execute(new Send(message));
    }

    public String receive() throws InterruptedException {
        return receiveBuffer.get();
    }

    private class Receive extends Thread {
        public void run() {
            String result;
            try {
                while (receive != null) {
                    Log.d(TAG, "run: receiving...");
                    //result = (String) input.readObject();
                    result = (String)dataIS.readUTF();
                    receiveBuffer.put(result);
                    Log.d(TAG, "run: received!!!" + result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
                Log.d(TAG, "Receiverun: exception: "+e);
                receive = null;
            }
        }
    }


    private class Connect implements Runnable {
        public void run() {
            try {
                Log.d(TAG, "run: Connecting to server...");
                address = InetAddress.getByName(ip);
                socket = new Socket(address, connectionPort);
                /*input = new ObjectInputStream(socket.getInputStream());       //add these when picture implementation
                output = new ObjectOutputStream(socket.getOutputStream());*/
                input = socket.getInputStream();
                dataIS = new DataInputStream(input);
                output = socket.getOutputStream();
                dataOS = new DataOutputStream(output);
                dataOS.flush();
                output.flush();
                //receiveBuffer.put("CONNECTED");
                receive = new Receive();                //start the receive thread. Running while app is running.
                receive.start();
                startDummy();
                Log.d(TAG, "run: CONNECTED");
            } catch (Exception e) { // SocketException, UnknownHostException
                //exception = e;
                Log.d(TAG, "run: "+ e);
                //receiveBuffer.put("EXCEPTION");
            }
        }
    }

    private class Disconnect implements Runnable {
        public void run() {
            Log.d(TAG, "run: DISCONNECTING!!!");
            try {
                if (dataIS != null)
                    dataIS.close();
                if (dataOS != null)
                    dataOS.close();
                if (input!=null)
                    input.close();
                if (output!=null)
                    output.close();
                if (socket != null)
                    socket.close();
                runOnThread.stop();
                receive=null;
                timer.cancel();
                Log.d(TAG, "run: DISCONNECTED!!!");
                //receiveBuffer.put("CLOSED");
            } catch(IOException e) {
                //exception = e;
                //receiveBuffer.put("EXCEPTION");
            }
        }
    }

    private class Send implements Runnable {
        private String message;

        public Send(String message) {
            this.message = message;
        }

        public void run() {
            try {
                //Log.d(TAG, "run: Sending...");
                dataOS.writeUTF(message);
                dataOS.flush();
                //Log.d(TAG, "run: SENT!!!");
            } catch (IOException e) {
                Log.d(TAG, "run: exception: " + e);
                //receiveBuffer.put("EXCEPTION");
            }
        }
    }

    public void startDummy(){
        Dummy dummy = new Dummy();
        timer.schedule(dummy,0,5000);
    }

    private class Dummy extends TimerTask {
        @Override
        public void run() {
            Log.d(TAG, "run: Timer: Sending my coordinates!");
            //sendMessage("blabla");
        }
    }
}
