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
import java.net.Socket;

public class ClientP2PReceivingSession implements Runnable
{
    private Socket m_socket;
    private BufferedReader m_net_in;
    private boolean m_stop = false;
    private ClientP2PSession m_p_session;
    
    public ClientP2PReceivingSession(Socket socket, BufferedReader net_in)
    {
        this.m_socket = socket;
        this.m_net_in = net_in;
        this.m_p_session = null;
    }
    
    public void setParent(ClientP2PSession session)
    {
        this.m_p_session = session;
    }
    
    public void stop()
    {
        synchronized (this)
        {
            this.m_stop = true;
        }
    }
    
    private boolean shouldStop()
    {
        synchronized (this)
        {
            return this.m_stop;
        }
    }
    
    @Override
    public void run()
    {
        Globals.debugPrintln("============ P2P Receiving thread (" + this.m_p_session.getRemoteUser() + ") begins =============");

        try
        {   
            while (!this.shouldStop())
            {
                PacketFromP2P.ClientMsg msg = PacketFromP2P.generateClientP2PRequest(this.m_net_in);

                if (null == msg)
                {
                    break;
                }

                if (msg instanceof PacketFromP2P.MessageRequest)
                {
                    PacketFromP2P.MessageRequest cp_res = (PacketFromP2P.MessageRequest) msg;
                    Globals.println(cp_res.getSender() + " (private): " + cp_res.getContent());
                }
            }
            
        }
        catch (Exception e)
        {
            Globals.debugPrintStackTrace(e);
        }
        finally
        {
            this.m_p_session.stop();
            try
            {
                if (null != this.m_socket)
                {
                    this.m_socket.close();
                }
            }
            catch (IOException e)
            {
                Globals.debugPrintStackTrace(e);
            }
            
            Globals.debugPrintln("============ P2P Receiving thread (" + this.m_p_session.getRemoteUser() + ") exits =============");
        }
        
    }
}