package com.cylan.publicApi;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer implements Runnable {
    private int mPort;

    public TCPServer(int port) {
        mPort = port;
    }

    //please override this!
    public void onClientConnect(Socket st) {
    }

    public void onClientDisconnect(InetAddress addr) {
    }

    public void onClientMsg(byte[] buff, int count, OutputStream output) {
    }

    private class ServerThread implements Runnable {

        private Socket client = null;

        public ServerThread(Socket client) {
            this.client = client;
        }

        @Override
        public void run() {
            InputStream input = null;
            OutputStream output = null;
            try {
                input = client.getInputStream();
                output = client.getOutputStream();

                byte[] buff = new byte[4096];
                int n;
                while (true) {
                    n = input.read(buff);
                    if (n < 0) {
                        break;
                    }
                    onClientMsg(buff, n, output);
                }

                input.close();
                output.close();
                client.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            onClientDisconnect(client.getInetAddress());
        }

    }

    @Override
    public void run() {
        ServerSocket server = null;
        try {
            server = new ServerSocket(mPort);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return;
        }
        Socket client = null;
        while (true) {
            try {
                client = server.accept();
                onClientConnect(client);
                new Thread(new ServerThread(client)).start();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
