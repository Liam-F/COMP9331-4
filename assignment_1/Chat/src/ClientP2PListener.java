/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * This is definition of a listening session of P2P
 * The listening port is randomly allocated 
 * @author Chunnan Sheng
 *
 */
public class ClientP2PListener implements Runnable
{
    private ServerSocket m_listener;
    private boolean m_stop;
    
    public ClientP2PListener() throws IOException
    {
        this.m_listener = new ServerSocket(0);
        this.m_stop = false;
    }
    
    public int getPort()
    {
        return this.m_listener.getLocalPort();
    }
    
    /**
     * Main loop of this session will exit if the stop
     * flag is set to true
     */
    public void stop()
    {
        synchronized (this)
        {
            this.m_stop = true;

            try
            {
                this.m_listener.close();
            }
            catch (IOException e)
            {
                Globals.debugPrintStackTrace(e);;
            }
        }
    }
    
    public boolean shouldStop()
    {
        synchronized (this)
        {
            return this.m_stop;
        }
    }
    
    @Override
    public void run()
    {
        // Main loop of this session
        // 
        while (!this.shouldStop())
        {
            try
            {
                // Establish a new connection
                // This call will be blocked until a new connection request
                // comes.
                Socket new_socket = m_listener.accept();

                Globals.debugPrintln("New connection from: " + new_socket.getInetAddress());
                PrintWriter net_out = new PrintWriter(new_socket.getOutputStream(), true);
                BufferedReader net_in = new BufferedReader(new InputStreamReader(new_socket.getInputStream()));
                
                PacketFromP2P.ClientMsg cm = PacketFromP2P.generateClientP2PRequest(net_in);
                
                // The socket for receiving data from the other client
                // But for the other client, it is sending session
                if (cm instanceof PacketFromP2P.SendingSocketRequest)
                {
                     PacketFromP2P.SendingSocketRequest ssr = (PacketFromP2P.SendingSocketRequest)cm;
                     String user = ssr.getUser();
                     
                     ClientP2PSession p2p_session = ClientEntity.getP2PSession(user);
                     if (null == p2p_session)
                     {
                         p2p_session = new ClientP2PSession(user);
                         ClientEntity.addP2PSession(user, p2p_session);
                     }
                     
                     ClientP2PReceivingSession r_session = new ClientP2PReceivingSession(new_socket, net_in);
                     p2p_session.setReceivingSession(r_session);                   
                     (new Thread(r_session)).start();
                }
                // The socket for for sending data to the other client
                // But for the other client, it is receiving session
                else if (cm instanceof PacketFromP2P.ReceivingSocketRequest)
                {
                    PacketFromP2P.ReceivingSocketRequest rsr = (PacketFromP2P.ReceivingSocketRequest)cm;
                    String user = rsr.getUser();
                    
                    ClientP2PSession p2p_session = ClientEntity.getP2PSession(user);                  
                    if (null == p2p_session)
                    {
                        p2p_session = new ClientP2PSession(user);
                        ClientEntity.addP2PSession(user, p2p_session);
                    }
                    
                    ClientP2PSendingSession s_session = new ClientP2PSendingSession(new_socket, net_out, user);
                    p2p_session.setSendingSession(s_session);                    
                    (new Thread(s_session)).start();
                }
                else
                {
                    new_socket.close();
                    Globals.println("Connection type not supported. \r\nOnly Primary or Secondary types is allowed.");
                    continue;
                }
            }
            catch (Exception ex)
            {
                Globals.debugPrintStackTrace(ex);;
            }
        }
        
        try
        {
            this.m_listener.close();
        }
        catch (IOException e)
        {
            Globals.debugPrintStackTrace(e);;
        }
        
    }
    
}
