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
import java.net.Socket;

/**
 * This session will start and wait for connection request from server
 * after client successfully logs in
 * @author dada
 *
 */
public class ClientPeerReplySession implements Runnable
{
    private Socket m_socket;
    private ClientEntity m_mother;
    
    public ClientPeerReplySession(Socket secondary, ClientEntity mother)
    {
        this.m_socket = secondary;
        this.m_mother = mother;
    }

    @Override
    public void run()
    {
        Globals.debugPrintln("==================== Peer reply thread begins =======================");

        try
        {
            PacketFromClient.SecondarySocketRequest s_req = (new PacketFromClient()).new SecondarySocketRequest(
                    this.m_mother.getUser());

            PrintWriter net_out = new PrintWriter(m_socket.getOutputStream(), true);
            BufferedReader net_in = new BufferedReader(new InputStreamReader(m_socket.getInputStream()));
            
            net_out.print(s_req.toString());
            net_out.flush();
            
            while (true)
            {
                PacketFromServer.ServerMsg msg = PacketFromServer.generateServerRequest(net_in);

                if (null == msg)
                {
                    break;
                }

                if (msg instanceof PacketFromServer.ClientPeerResponse)
                {
                    PacketFromServer.ClientPeerResponse cp_res = (PacketFromServer.ClientPeerResponse) msg;
                    Globals.println(cp_res.getSender() + ": " + cp_res.getContent());
                }
                else if (msg instanceof PacketFromServer.PresenceNotification)
                {
                    PacketFromServer.PresenceNotification pn = (PacketFromServer.PresenceNotification) msg;
                    if (pn.getLoginType())
                    {
                        Globals.println(pn.getUser() + " logged in");
                    }
                    else
                    {
                        Globals.println(pn.getUser() + " logged out");
                    }
                }
            }
            
        }
        catch (Exception e)
        {
            Globals.debugPrintStackTrace(e);
        }
        finally
        {
            this.m_mother.getMainThread().interrupt();
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
            
            Globals.debugPrintln("==================== Peer reply thread exits =======================");
        }
        
    }
    
    
}
