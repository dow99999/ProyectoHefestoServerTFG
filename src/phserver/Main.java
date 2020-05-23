/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package phserver;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author diego
 */
public class Main {

  public static final int MAX_USERS = 2;
  ServerClientMain clientThreadMain;
  ServerClient clientThread2;
  boolean serverOpen;

  public void startServer() {
    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    try {
      ServerSocket ss = new ServerSocket(27827);
      Socket[] m_clients = new Socket[2];
      serverOpen = true;
      clientThreadMain = new ServerClientMain(ss, m_clients);
      clientThread2 = new ServerClient(ss, m_clients);

      clientThreadMain.start();
      clientThread2.start();
      System.out.println("Listening on port 27827...");
      String output;
      do{
        output = br.readLine();
      }while(!output.toLowerCase().equals("quit"));
      serverOpen = false;
      Thread.sleep(100);
      m_clients.notifyAll();
      clientThread2.interrupt();
      clientThreadMain.interrupt();
      
    } catch (IOException ex) {
      System.out.println(ex.getMessage());
    } catch (InterruptedException ex) {
      Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
    }
  }

  private class ServerClient extends Thread {
    public boolean conected;
    public boolean ready;
    private final ServerSocket m_server;
    private final Socket[] m_clients;
    private DataOutputStream out;
    private DataInputStream in;
    private DataOutputStream outm;
    private DataInputStream inm;

    ServerClient(ServerSocket s, Socket[] c) {
      m_server = s;
      m_clients = c;
    }

    @Override
    public void run() {
      try {
        m_clients[1] = m_server.accept();
        System.out.println("Connected 1");
        conected = true;
        in = new DataInputStream(m_clients[1].getInputStream());
        out = new DataOutputStream(m_clients[1].getOutputStream());
        synchronized(m_clients){
          m_clients.notifyAll();
          while(!clientThreadMain.conected){
            m_clients.wait();
          }
        }
        inm = new DataInputStream(m_clients[0].getInputStream());
        outm = new DataOutputStream(m_clients[0].getOutputStream());
        
        out.writeBoolean(false);
        
        outm.writeInt(in.readInt());
        outm.writeFloat(in.readFloat());
        outm.writeFloat(in.readFloat());
        outm.writeInt(in.readInt());
        
        while(serverOpen){
          outm.writeFloat(in.readFloat());
          outm.writeFloat(in.readFloat());
          outm.writeInt(in.readInt());
          outm.writeInt(in.readInt());
        }
      } catch (IOException ex) {
      } catch (InterruptedException ex) {
      }
    }
  }

  private class ServerClientMain extends Thread {
    public boolean conected;
    public boolean ready;
    private final ServerSocket m_server;
    private final Socket[] m_clients;
    private DataOutputStream out;
    private DataInputStream in;
    private DataOutputStream out2;
    private DataInputStream in2;

    ServerClientMain(ServerSocket s, Socket[] c) {
      m_server = s;
      m_clients = c;
      conected = false;
    }

    @Override
    public void run() {
      try {
        m_clients[0] = m_server.accept();
        System.out.println("Connected 0");
        conected = true;
        in = new DataInputStream(m_clients[0].getInputStream());
        out = new DataOutputStream(m_clients[0].getOutputStream());
        synchronized(m_clients){
          m_clients.notifyAll();
          while(!clientThread2.conected){
            m_clients.wait();
          }
        }
        in2 = new DataInputStream(m_clients[1].getInputStream());
        out2 = new DataOutputStream(m_clients[1].getOutputStream());
        
        out.writeBoolean(true);
        
        out2.writeInt(in.readInt());
        out2.writeFloat(in.readFloat());
        out2.writeFloat(in.readFloat());
        out2.writeInt(in.readInt());
        
        while(serverOpen){
          out2.writeFloat(in.readFloat());
          out2.writeFloat(in.readFloat());
          out2.writeInt(in.readInt());
          out2.writeInt(in.readInt());
        }
        
      } catch (IOException ex) {
      } catch (InterruptedException ex) {
      }

    }
  }

  public static void main(String[] args) {
    new Main().startServer();
  }
}
