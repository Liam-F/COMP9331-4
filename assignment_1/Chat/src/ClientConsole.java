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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.util.Collection;

/**
 * 
 * @author Chunnan Sheng
 *
 */
public class ClientConsole
{
    private String m_user;
    private PrintWriter m_net_out;
    private BufferedReader m_net_in;
    private BufferedReader m_std_in;
    
    public ClientConsole(String login_user, PrintWriter net_out, BufferedReader net_in, BufferedReader std_in)
    {
        this.m_user = login_user;
        this.m_net_out = net_out;
        this.m_net_in = net_in;
        this.m_std_in = std_in;
    }
    
    private boolean handleMessageCmd(String[] segments, String command) throws Exception
    {
        if (segments.length < 3)
        {
            Globals.println("Message format: message <user> <message>");
            return false;
        }

        String ip = InetAddress.getLocalHost().getHostAddress();
        String user = segments[1];
        long time = System.currentTimeMillis();
        String message = command.replaceFirst(Globals.command_message + "[ \t]+" + user + "[ \t]+", "");

        PacketFromClient.MessageRequest msg_req = (new PacketFromClient()).new MessageRequest(ip, this.m_user, user, time,
                message);

        this.m_net_out.print(msg_req.toString());
        this.m_net_out.flush();

        return true;
    }
    
    private boolean handleBroadcastCmd(String[] segments, String command) throws Exception
    {
        if (segments.length < 2)
        {
            Globals.println("Message format: broadcast <message>");
            return false;
        }

        String ip = InetAddress.getLocalHost().getHostAddress();
        long time = System.currentTimeMillis();
        String message = command.replaceFirst(Globals.command_broadcast + "[ \t]+", "");

        PacketFromClient.BroadcastRequest msg_req = (new PacketFromClient()).new BroadcastRequest(ip, this.m_user, time, message);

        this.m_net_out.print(msg_req.toString());
        this.m_net_out.flush();

        return true;
    }
    
    private boolean handleLogoutCmd() throws Exception
    {
        
        PacketFromClient.LogoutRequest logout_req = (new PacketFromClient()).new LogoutRequest(this.m_user);
        
        this.m_net_out.print(logout_req.toString());
        this.m_net_out.flush();
        
        return true;
    }
    
    private boolean handleWhoelseCmd() throws Exception
    {
        PacketFromClient.WhoelseRequest whoelse_req = (new PacketFromClient()).new WhoelseRequest();
        
        this.m_net_out.print(whoelse_req.toString());
        this.m_net_out.flush();
        
        return true;
    }
    
    private boolean handleWhoelsesinceCmd(String[] segments) throws Exception
    {
        if (segments.length < 2)
        {
            Globals.println("Command format: whoelsesince <time in seconds>");
            return false;
        }

        int time = 0;
        try
        {
            time = Integer.parseInt(segments[1]);
        }
        catch (NumberFormatException ex)
        {
            Globals.println("The time should be in digit format like 480, 60, etc.");
            return false;
        }

        PacketFromClient.WhoelsesinceRequest wes_req = (new PacketFromClient()).new WhoelsesinceRequest(time);

        this.m_net_out.print(wes_req.toString());
        this.m_net_out.flush();

        return true;
    }
    
    private boolean handleBlockCmd(String[] segments)
    {
        if (segments.length < 2)
        {
            Globals.println("Command format: block <user>");
            return false;
        }
        
        String user = segments[1]; 
        PacketFromClient.BlockRequest br = (new PacketFromClient()).new BlockRequest(user, true);
        
        this.m_net_out.print(br.toString());
        this.m_net_out.flush();
        
        return true;
    }
    
    private boolean handleUnblockCmd(String[] segments)
    {
        if (segments.length < 2)
        {
            Globals.println("Command format: unblock <user>");
            return false;
        }
        
        String user = segments[1];
        PacketFromClient.BlockRequest br = (new PacketFromClient()).new BlockRequest(user, false);
        
        this.m_net_out.print(br.toString());
        this.m_net_out.flush();
        
        return true;
    }
    
    private boolean handleStartP2PCmd(String [] segments)
    {
        if (segments.length < 2)
        {
            Globals.println("Command format: startprivate <user>");
            return false;
        }
        
        String user = segments[1];
        
        if (this.m_user.equals(user))
        {
            Globals.println("Error. Private messaging to self not allowed");
            return false;
        }
        
        if (null != ClientEntity.getP2PSession(user))
        {
            Globals.println("User " + user + " is already P2P online");
            return false;
        }
        
        PacketFromClient.StartP2PRequest pr = (new PacketFromClient()).new StartP2PRequest(user);
        
        this.m_net_out.print(pr.toString());
        this.m_net_out.flush();
        
        return true;
    }
    
    private boolean handleP2PMessageCmd(String[] segments, String command) throws Exception
    {
        if (segments.length < 3)
        {
            Globals.println("Message format: private <user> <message>");
            return false;
        }

        String ip = InetAddress.getLocalHost().getHostAddress();
        String user = segments[1];
        
        if (this.m_user.equals(user))
        {
            Globals.println("Error. Private messaging to self not allowed");
            return false;
        }
        
        long time = System.currentTimeMillis();
        String message = command.replaceFirst(Globals.command_private + "[ \t]+" + user + "[ \t]+", "");

        ClientP2PSession session = ClientEntity.getP2PSession(user);
        if (null == session)
        {
            Globals.println("Error. Private messaging to " + user + " not enabled");
            return false;
        }

        session.getSendingSession().enqueueSendingMessage(this.m_user, message, ip, time);
        Globals.println("Private message sent");

        return true;
    }
    
    private boolean handleStopP2PCmd(String [] segments)
    {
        if (segments.length < 2)
        {
            Globals.println("Command format: stopprivate <user>");
            return false;
        }
        
        String user = segments[1];
        ClientP2PSession session = ClientEntity.getP2PSession(user);
        if (null == session)
        {
            Globals.println("User " + user + " is already P2P offline");
            return false;
        }

        session.stop();
        ClientEntity.removeP2PSession(user);
        
        return true;
    }
    
    private boolean handleDefaultCmd(String command) throws Exception
    {
        PacketFromClient.DefaultRequest dr = (new PacketFromClient()).new DefaultRequest(command);
        
        this.m_net_out.print(dr.toString());
        this.m_net_out.flush();
        
        return true;
    }
    
    private boolean handleP2PWhoelse()
    {
        Collection<String> users = ClientEntity.findP2PWhoelse(this.m_user);
        for (String user : users)
        {
            Globals.println(user);
        }
        
        return true;
    }
    
    /**
     * Handle all responses sent from the server
     * @throws Exception
     */
    private void handleResponse() throws Exception
    {
        // Try to receive response from server
        PacketFromServer.ServerMsg s_res = PacketFromServer.generateServerRequest(this.m_net_in);
        
        if (s_res instanceof PacketFromServer.AckResponse)
        {
            Globals.debugPrintln("Command sent to the server successfully.");
            String ack_msg = ((PacketFromServer.AckResponse)s_res).getContent();
            if (null != ack_msg)
            {
                Globals.println(ack_msg);
            }
        }
        else if (s_res instanceof PacketFromServer.UserListResponse)
        {
            Collection <String> users = ((PacketFromServer.UserListResponse)s_res).getAllUsers();
            for (String user : users)
            {
                Globals.println(user);
            }
        }
        else if (s_res instanceof PacketFromServer.PrivateResponse)
        {
            PacketFromServer.PrivateResponse pr = (PacketFromServer.PrivateResponse) s_res;
            Globals.debugPrintln(pr.getUser());
            Globals.debugPrintln(pr.getIPAddress());
            Globals.debugPrintln(pr.getP2PPort() + "");

            ClientEntity.getClientInstance().establishP2P(pr.getUser(), pr.getIPAddress(), pr.getP2PPort());
            // TODO: Try to establish p2p connection to the peer
        }
    }
        
    public void consoleInputLoop() throws Exception
    {
        while (true)
        {
            String command = Globals.readLine(this.m_std_in);
            String[] segments = command.split("[ \t]+");

            if (segments.length > 0 && segments[0].length() > 0)
            {
                if (segments[0].equals(Globals.command_message))
                {

                    if (this.handleMessageCmd(segments, command))
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_logout))
                {
                    if (this.handleLogoutCmd())
                    {
                        this.handleResponse();
                        break;
                    }
                }
                else if (segments[0].equals(Globals.command_whoelse))
                {
                    if (this.handleWhoelseCmd())
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_whoelsesince))
                {
                    if (this.handleWhoelsesinceCmd(segments))
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_broadcast))
                {
                    if (this.handleBroadcastCmd(segments, command))
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_block))
                {
                    if (this.handleBlockCmd(segments))
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_unblock))
                {
                    if (this.handleUnblockCmd(segments))
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_startprivate))
                {
                    if (this.handleStartP2PCmd(segments))
                    {
                        this.handleResponse();
                    }
                }
                else if (segments[0].equals(Globals.command_stopprivate))
                {
                    if (this.handleStopP2PCmd(segments))
                    {
                        // This command just disconnect P2P and does not
                        // need responses from the server
                    }
                }
                else if (segments[0].equals(Globals.command_private))
                {
                    if (this.handleP2PMessageCmd(segments, command))
                    {
                        // This message is directly sent via P2P and does not
                        // need responses from the server
                    }
                }
                else if (segments[0].equals(Globals.command_private_whoelse))
                {
                    if (this.handleP2PWhoelse())
                    {
                        // No server response is needed
                    }
                }
                else
                {
                    if (this.handleDefaultCmd(command))
                    {
                        this.handleResponse();
                    }
                }    
            }        
        }
    }

}
