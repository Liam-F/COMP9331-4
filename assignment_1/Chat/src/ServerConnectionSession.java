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
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Collection;


/**
 * This is a connection session (thread) in which TCP transmissions are executed
 * 
 * @author Chunnan Sheng
 *
 */
public class ServerConnectionSession implements Runnable
{
    private SessionStatus m_status;
    private Socket m_socket;
    // net input from the client
    private BufferedReader m_net_in;
    // net output to the client
    private PrintWriter m_net_out;
    
    private String m_user;
    private ServerPeerReplySession m_pr_session;

    public ServerConnectionSession(Socket socket, BufferedReader net_in, PrintWriter net_out)
    {
        this.m_socket = socket;
        this.m_user = null;
        this.m_status = SessionStatus.Connected;
        this.m_net_in = net_in;
        this.m_net_out = net_out;
        this.m_pr_session = null;
    }
    
    public ServerPeerReplySession getPeerReplySession()
    {
        synchronized (this)
        {
            return this.m_pr_session;
        }
    }

    /**
     * This procedure may be called by multiple threads
     * so, it should be multiple thread safe
     * 
     * @return
     */
    /*
    public String getUser()
    {
        synchronized (this)
        {
            return this.m_user;
        }
    }*/

    /**
     * This procedure may be executed by multiple threads
     * so, it should be multiple thread safe
     * 
     * @return
     */
    /*
    public SessionStatus getSessionStatus()
    {
        synchronized (this)
        {
            return this.m_status;
        }
    }
    */

    /**
     * This procedure may be executed by multiple threads
     * so, it should be multiple thread safe
     * 
     * @return
     */
    /*
    public void setSessionStatus(SessionStatus status)
    {
        synchronized (this)
        {
            this.m_status = status;
        }
    }
    */
    
    /**
     * The peer reply session should start when the client asks for peer reply connection
     * after it successfully logs in.
     * 
     * @param secondary
     */
    public void startPeerReplySession(Socket secondary)
    {
        synchronized (this)
        {
            this.m_pr_session = new ServerPeerReplySession(secondary, this.m_user);
            (new Thread(this.m_pr_session)).start();
        }
    }
    
    public void sendRequest(PacketFromServer.ServerMsg request)
    {
        this.m_net_out.print(request.toString());
        this.m_net_out.flush();
    }

    /**
     * The login procedure
     * 
     * @return
     * @throws Exception
     */
    private boolean handleClientLogin() throws Exception
    {
        // There is potential timeout exception here
        PacketFromClient.ClientMsg cm = PacketFromClient.generateClientRequest(this.m_net_in);

        if (!(cm instanceof PacketFromClient.LoginRequest))
        {
            throw new Exception("This is not a login request!");
        }

        PacketFromClient.LoginRequest login_rq = (PacketFromClient.LoginRequest) cm;
        String user_name = login_rq.getUser();
        String password = login_rq.getPassword();

        // search user name
        boolean exist = ServerEntity.inCredential(user_name);
        
        // This user should exist with a correct password
        // It is not allowed to login with this user name again when this user is already online.
        
        PacketFromServer.LoginResponse lr;
        // The user does not exist
        if (!exist)
        {           
            lr = (new PacketFromServer()).new LoginResponse(LoginStatus.Failed);
            this.sendRequest(lr);
            this.m_status = SessionStatus.Disconnected;

            return false;
        }
        // Wrong password
        else if (!ServerEntity.getCredentialPassword(user_name).equals(password))
        {
            ServerEntity.addLoginFailureToCredential(user_name);
            
            if (ServerEntity.isCredentialBlocked(user_name))
            {
                lr = (new PacketFromServer()).new LoginResponse(LoginStatus.Blocked);
            }
            else
            {           
                lr = (new PacketFromServer()).new LoginResponse(LoginStatus.Failed);
            }
            
            this.sendRequest(lr);
            this.m_status = SessionStatus.Disconnected;

            return false;
        }
        // The user is already online
        else if (null != ServerEntity.getConnectionSession(user_name))
        {
            lr = (new PacketFromServer()).new LoginResponse(LoginStatus.Failed);
            this.sendRequest(lr);
            this.m_status = SessionStatus.Disconnected;

            return false;
        }
        // The user is blocked
        else if (ServerEntity.isCredentialBlocked(user_name))
        {
            lr = (new PacketFromServer()).new LoginResponse(LoginStatus.StillBlocked);
            this.sendRequest(lr);
            this.m_status = SessionStatus.Disconnected;

            return false;
        }
        else
        {   
            this.m_user = user_name;        
            ServerEntity.insertConnectionSession(user_name, this);
            ServerEntity.addLoginToCredential(user_name);
            ServerEntity.addIPAddressToCredential(user_name, this.m_socket.getInetAddress().toString().replaceFirst("/", ""));
          
            lr = (new PacketFromServer()).new LoginResponse(LoginStatus.Successful);
            this.sendRequest(lr);
            
            PacketFromClient.ClientMsg cm2 = PacketFromClient.generateClientRequest(m_net_in);
            if (cm2 instanceof PacketFromClient.P2PPort)
            {
                PacketFromClient.P2PPort p2p_port = (PacketFromClient.P2PPort)cm2;
                ServerEntity.addP2PPortToCredential(user_name, p2p_port.getPort());
            }
            else
            {
                Globals.debugPrint("Only P2PPort is allowed to be received this time!");
            }
            
            // Broadcast login information of this session (user) to all peer users
            this.broadcastPresenceNotification(true);            
            this.m_status = SessionStatus.Login;
            
            return true;
        }
    }
    
    /**
     * 
     */
    private void broadcastPresenceNotification(boolean login)
    {
        if (null != this.m_user)
        {
            Collection<String> other_online_users = ServerEntity.findWhoelse(this.m_user);

            String host = this.m_socket.getInetAddress().toString().replaceFirst("/", "");
            long sent_time = System.currentTimeMillis();

            for (String receiver : other_online_users)
            {
                ServerConnectionSession session = ServerEntity.getConnectionSession(receiver);
                
                if (null != session)
                {
                    session.getPeerReplySession().enqueuePeerMessage(this.m_user, login, host, sent_time);
                }
            }
        }
    }
    
    /**
     * Get message from client,
     * find the user who should receive the message,
     * and put the message into the user's message queue
     * 
     * @param cm
     * @throws Exception
     */
    private void handleClientMessageCmd(PacketFromClient.ClientMsg cm) throws Exception
    {
        PacketFromClient.MessageRequest cmr = (PacketFromClient.MessageRequest) cm;

        String content = cmr.getContent();
        String sender = cmr.getSender();
        String receiver = cmr.getReceiver();
        String host = cmr.getHost();
        long sent_time = cmr.getSentTime();
        
        // Search for the receiver in the credentials
        boolean exist = ServerEntity.inCredential(receiver);
        if (!exist) // If the receiver does not exist
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(Globals.error_invalid_user);
            this.sendRequest(error);
            return;
        }
        
        if (ServerEntity.inBlacklist(receiver, this.m_user))
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(Globals.error_blocked_by_user);
            this.sendRequest(error);
            return;
        }

        // Search receiver from hash map
        ServerConnectionSession receiver_session = ServerEntity.getConnectionSession(receiver);
        if (null != receiver_session)
        {
            // Insert the new message into the receiver's message queue
            receiver_session.getPeerReplySession().enqueuePeerMessage(sender, content, host, sent_time);
        }
        // If the receiver is offline but registered
        else
        {
            ServerEntity.enqueueOfflinePeerMessage(sender, receiver, content, host, sent_time);
        }
        
        PacketFromServer.AckResponse ack = (new PacketFromServer()).new AckResponse("Message sent");
        this.sendRequest(ack);
    }
    
    /**
     * Get message from the client and traverse
     * all registered users in the credentials.
     * If the user is online, send online message. Otherwise,
     * send offline message.
     * @param cm
     * @throws Exception
     */
    private void handleClientBroadcastCmd(PacketFromClient.ClientMsg cm) throws Exception
    {
        PacketFromClient.BroadcastRequest br = (PacketFromClient.BroadcastRequest) cm;
        Collection <String> all_users = ServerEntity.getAllUsers();

        String content = br.getContent();
        String sender = br.getSender();
        String host = br.getHost();
        long sent_time = br.getSentTime();
        
        int users_who_block_you = 0;
        
        for (String user : all_users)
        {   
            // Skip myself because the sender himself does not need to receive broadcast message
            if (user.equals(this.m_user))
            {
                continue;
            }
            
            if (ServerEntity.inBlacklist(user, this.m_user))
            {
                users_who_block_you ++;
                continue;
            }
            
            ServerConnectionSession receiver_session = ServerEntity.getConnectionSession(user);          
            // If this user is online
            if (null != receiver_session)
            {
                // Insert the new message into the receiver's message queue
                receiver_session.getPeerReplySession().enqueuePeerMessage(sender, content, host, sent_time);
            }
            else // This user is offline
            {
                ServerEntity.enqueueOfflinePeerMessage(sender, user, content, host, sent_time);
            }   
        }
        
        if (users_who_block_you > 0)
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(Globals.error_blocked_by_users);
            this.sendRequest(error);
        }
        else
        {
            PacketFromServer.AckResponse ack = (new PacketFromServer()).new AckResponse("Message sent");
            this.sendRequest(ack);
        }
    }
    
    /**
     * 
     * @param cm
     */
    private void handleClientWhoelseCmd(PacketFromClient.ClientMsg cm)
    {
        Collection <String> users = ServerEntity.findWhoelse(this.m_user);
        
        PacketFromServer.UserListResponse ulr = (new PacketFromServer()).new UserListResponse(users);
        this.sendRequest(ulr);
    }
    
    /**
     * 
     * @param cm
     */
    private void handleClientWhoelsesinceCmd(PacketFromClient.ClientMsg cm)
    {       
        PacketFromClient.WhoelsesinceRequest wesr = (PacketFromClient.WhoelsesinceRequest)cm;
        int time = wesr.getTime();
        Collection <String> users = ServerEntity.findWhoelseSince(this.m_user, time);
        
        PacketFromServer.UserListResponse ulr = (new PacketFromServer()).new UserListResponse(users);
        this.sendRequest(ulr);
    }
    
    /**
     * 
     * @param cm
     */
    private void handleClientBlockCmd(PacketFromClient.ClientMsg cm)
    {
        PacketFromClient.BlockRequest br = (PacketFromClient.BlockRequest)cm;
        String user = br.getUser();
        boolean type = br.getType();
        
        // Cannot block or unblock self
        if (user.equals(this.m_user))
        {
            String str_error;
            if (type)
            {
                str_error = Globals.error_cannot_block_self;
            }
            else
            {
                str_error = Globals.error_cannot_unblock_self;
            }
            
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(str_error);
            this.sendRequest(error);
            return;
        }
        
        // Check if this user exists
        boolean exist = ServerEntity.inCredential(user);
        if (!exist)
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(Globals.error_invalid_user);
            this.sendRequest(error);
            return;
        }
        
        String ack_msg;
        // Add or remove this user to my blacklist
        if (type)
        {
            if (!ServerEntity.inBlacklist(this.m_user, user))
            {
                ServerEntity.intoBlacklist(this.m_user, user);
                ack_msg = user + " is blocked";
            }
            else
            {
                ack_msg = "Error. " + user + " was already blocked";
            }
        }
        else
        {
            if (ServerEntity.inBlacklist(this.m_user, user))
            {
                ServerEntity.outBlacklist(this.m_user, user);
                ack_msg = user + " is unblocked";
            }
            else
            {
                ack_msg = "Error. " + user + " was not blocked";
            }
        }
        
        PacketFromServer.AckResponse ack = (new PacketFromServer()).new AckResponse(ack_msg);
        this.sendRequest(ack);
    }
    
    /**
     * 
     * @param cm
     */
    private void handleClientStartP2PCmd(PacketFromClient.ClientMsg cm)
    {
        PacketFromClient.StartP2PRequest pr = (PacketFromClient.StartP2PRequest) cm;
        String remote_user = pr.getUser();

        String ip = ServerEntity.getCredentialIPAddress(remote_user);
        int port = ServerEntity.getCredentialP2PPport(remote_user);
        ServerConnectionSession session = ServerEntity.getConnectionSession(remote_user);
        if (null == ip)
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(Globals.error_invalid_user);
            this.sendRequest(error);
            return;
        }

        if (null == session)
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(
                    "User " + remote_user + " is offline");
            this.sendRequest(error);
            return;
        }

        if (ServerEntity.inBlacklist(remote_user, this.m_user))
        {
            PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(
                    "User " + remote_user + " has blocked you");
            this.sendRequest(error);
            return;
        }

        PacketFromServer.PrivateResponse p_res = (new PacketFromServer()).new PrivateResponse(remote_user, ip, port);
        this.sendRequest(p_res);
    }
    
    /**
     * Send ack message to the client and set session status to disconnected
     * so that the main loop of this session would exit
     * @param cm
     */
    private void handleClientLogoutCmd(PacketFromClient.ClientMsg cm)
    {
        PacketFromServer.AckResponse ack = (new PacketFromServer()).new AckResponse();
        this.sendRequest(ack);
        
        this.m_status = SessionStatus.Disconnected;
    }
    
    private void handleClientWrongCmd(PacketFromClient.ClientMsg cm)
    {
        PacketFromServer.AckResponse error = (new PacketFromServer()).new AckResponse(Globals.error_invalid_command);
        this.sendRequest(error);
    }

    /**
     * Main function of this session (thread)
     */
    @Override
    public void run()
    {
        try
        {
            try
            {
                handleClientLogin();
            }
            catch (SocketTimeoutException te)
            {
                // Terminate this session if socket timeout exception occurs
                Globals.debugPrintStackTrace(te);              
                this.m_status = SessionStatus.Disconnected;
            }

            while (SessionStatus.Login == this.m_status)
            { 
                PacketFromClient.ClientMsg cm = null;
                // The thread will be blocked here until new request
                // comes from the client or timeout exception occurs
                try
                {
                    cm = PacketFromClient.generateClientRequest(this.m_net_in);
                }
                catch (SocketTimeoutException ste)
                {
                    // Terminate this session if socket timeout exception occurs
                    Globals.debugPrintStackTrace(ste);
                    // Insert the timeout message into message queue
                    this.getPeerReplySession().enqueuePeerMessage("Server", "Connection timeout due to inactivity.",
                            InetAddress.getLocalHost().getHostAddress(), System.currentTimeMillis());
                    
                    this.m_status = SessionStatus.Disconnected;
                    continue;
                }

                if (cm instanceof PacketFromClient.MessageRequest)
                {
                    this.handleClientMessageCmd(cm);
                }
                else if (cm instanceof PacketFromClient.WhoelseRequest)
                {
                    this.handleClientWhoelseCmd(cm);
                }
                else if (cm instanceof PacketFromClient.WhoelsesinceRequest)
                {
                    this.handleClientWhoelsesinceCmd(cm);
                }
                else if (cm instanceof PacketFromClient.BroadcastRequest)
                {
                    this.handleClientBroadcastCmd(cm);
                }
                else if (cm instanceof PacketFromClient.BlockRequest)
                {
                    this.handleClientBlockCmd(cm);
                }
                else if (cm instanceof PacketFromClient.StartP2PRequest)
                {
                    this.handleClientStartP2PCmd(cm);
                }
                else if (cm instanceof PacketFromClient.LogoutRequest)
                {
                    this.handleClientLogoutCmd(cm);
                }
                else if (cm instanceof PacketFromClient.DefaultRequest)
                {
                    this.handleClientWrongCmd(cm);
                }
                else
                {
                    this.handleClientWrongCmd(cm);
                }

            }
        }
        catch (Exception ex)
        {
            Globals.debugPrintStackTrace(ex);
        }
        // Close the socket when this session is going to terminate.
        finally
        {   
            this.m_status = SessionStatus.Disconnected;
            if (null != this.m_pr_session)
            {
                this.m_pr_session.stop();
            }
            this.broadcastPresenceNotification(false);
            ServerEntity.addLogoutToCredential(this.m_user);
            ServerEntity.removeConnectionSession(this.m_user);
            
            Globals.println(this.m_socket.getInetAddress().toString() + " disconnected");
            
            try
            {
                m_socket.close();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                Globals.debugPrintStackTrace(e);
            }
            
            Globals.debugPrintln("==================== Main session thread exits =======================");
        }
    }
}
