package com.sun.jvmstat.tools.visualgc;

import java.net.*;
import java.io.*;

public class ReSocket {
    String serverName;
    String serverProto;
    Integer serverPort;
    Socket connection;
    // DataOutputStream out;
    PrintWriter out = null;

    /* assume tcp, setup connection abstract later*/
    public ReSocket(String server, Integer port) {
        this.serverName = server;
        this.serverPort = port;
        this.serverProto = "tcp";
        _connect(server, port, "tcp");
    }

    /* should really do some checking here for tcp/udp stuff */
    private void _connect(String server, Integer port, String proto) {
        try {
            // System.out.println("to: " + server + ":" + port);
            Socket client = new Socket(server, port);
            this.connection = client;
            this.out = new PrintWriter(client.getOutputStream(), true);
            // System.out.println("ok");
        } catch(IOException e) {
            // System.out.println("not ok");
            e.printStackTrace();
        }
    }

    private void checkCon() {
        if (this.out.checkError() == true) {
            _connect(serverName, serverPort, serverProto);
        } /* else {
            System.out.println("connected");
        } */
    }

    private void _write(String data) {
        checkCon();
        try {
            this.out.println(data);
        } catch(Exception e) {
            // System.out.println("not ok");
            e.printStackTrace();
        }
    }

    /* do a check if it's ok if not reconnect */
    public void write(String data) {
        _write(data);
    }

    public void write(String[] dataset) {
        for(String data: dataset) {
            _write(data);
        }
    }

    public String serverName() {
        return this.serverName;
    }
    
    public Integer serverPort() {
        return this.serverPort;
    }
    
    public String serverProto() {
        return this.serverProto;
    }
}
