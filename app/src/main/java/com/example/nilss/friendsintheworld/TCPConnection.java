package com.example.nilss.friendsintheworld;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;

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
    private DataInputStream dataIS;
    private DataOutputStream dataOS;
    private InetAddress address;

    //You can say that this is the "constructor".
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.ip = intent.getStringExtra(IP);
        this.connectionPort = Integer.parseInt(intent.getStringExtra(PORT));
        receiveBuffer = new Buffer<String>();       //have to convert an incoming string to a JSON Object
        runOnThread = new RunOnThread();
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
                    //result = (String) input.readObject();
                    result = (String)dataIS.readUTF();
                    receiveBuffer.put(result);
                }
            } catch (Exception e) { // IOException, ClassNotFoundException
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
                //InputStream input = socket.getInputStream();
                dataIS = new DataInputStream(socket.getInputStream());
                //OutputStream output = socket.getOutputStream();
                dataOS = new DataOutputStream(socket.getOutputStream());
                dataOS.flush();
                //receiveBuffer.put("CONNECTED");
                receive = new Receive();                //start the receive thread. Running while app is running.
                receive.start();
                Log.d(TAG, "run: CONNECTED");
            } catch (Exception e) { // SocketException, UnknownHostException
                //exception = e;
                //receiveBuffer.put("EXCEPTION");
            }
        }
    }

    private class Disconnect implements Runnable {
        public void run() {
            try {
                if (dataIS != null)
                    dataIS.close();
                if (dataOS != null)
                    dataOS.close();
                if (socket != null)
                    socket.close();
                runOnThread.stop();
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
                dataOS.writeUTF(message);
                dataOS.flush();
            } catch (IOException e) {
                //exception = e;
                //receiveBuffer.put("EXCEPTION");
            }
        }
    }
}