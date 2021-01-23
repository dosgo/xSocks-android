package io.github.xSocks.service;


import android.net.LocalServerSocket;
import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.util.Log;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.FileChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.github.xSocks.R;
import io.github.xSocks.utils.Constants;

public class xSocksVpnThread extends Thread {
    private String TAG = "xSocksVpnService";

    private volatile boolean isRunning = true;
    private volatile LocalServerSocket serverSocket = null;

    private xSocksVpnService vpnService;


    public xSocksVpnThread(xSocksVpnService vpnService) {
        this.vpnService = vpnService;
    }

    private void closeServerSocket() {
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                // ignore
            }
            serverSocket = null;
        }
    }

    public void stopThread() {
        isRunning = false;
        closeServerSocket();
    }

    @Override
    public void run() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            //use SOCKET_SEQPACKET;
            LocalSocket tunnel = new LocalSocket(LocalSocket.SOCKET_SEQPACKET);
            Log.d("connect sock",Constants.Path.BASE + "tunDevSock");
            tunnel.connect(new LocalSocketAddress(Constants.Path.BASE + "tunDevSock",LocalSocketAddress.Namespace.FILESYSTEM));
            int ByteBufLen= vpnService.VPN_MTU+80;//32767*1;
            // Packets received need to be written to this output stream.
            new Thread() {
                public void run() {
                    try {
                        ByteBuffer packet = ByteBuffer.allocateDirect(ByteBufLen);
                        InputStream tunnelIn = tunnel.getInputStream();
                        FileOutputStream out = new FileOutputStream(vpnService.vpnInterface.getFileDescriptor());
                        while (isRunning) {
                            packet.clear();
                            // Read the incoming packet from the tunnel.
                            int length = tunnelIn.read(packet.array());
                            if (length > 0) {
                               // packet.limit(length);
                                packet.flip();
                                // Write the incoming packet to the output stream.
                                out.write(packet.array(),0, length);
                            }else{
                                Thread.sleep(5);
                            }
                    }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }.start();
                ByteBuffer packet = ByteBuffer.allocateDirect(ByteBufLen);
                FileInputStream in = new FileInputStream(vpnService.vpnInterface.getFileDescriptor());
                // Packets received need to be written to this output stream.
                OutputStream tunnelInOut = tunnel.getOutputStream();
                while (isRunning) {
                    packet.clear();
                    // Read the outgoing packet from the input stream.
                    int length = in.read(packet.array());
                    if (length > 0) {
                        // Write the outgoing packet to the tunnel.
                        packet.flip();
                        tunnelInOut.write(packet.array(), 0, length);
                    }else{
                        Thread.sleep(20);
                    }
                }
            }  catch(Exception e){
                this.vpnService.changeState(Constants.State.STOPPED,this.vpnService.getString(R.string.service_failed));
                Log.e(TAG, "Error when accept socket", e);
            }
    }
}
