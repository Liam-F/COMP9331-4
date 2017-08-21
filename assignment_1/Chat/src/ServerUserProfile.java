/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;

public class ServerUserProfile
{
    private String m_user;
    private String m_password;
    private Queue<InterSessionMessage> m_message_queue;
    
    // Black list of this user
    // All peer's messages would be blocked if they exist in this black list
    private HashSet <String> m_black_list;
    
    // This integer indicates when this user was blocked in milliseconds
    private long m_blocked_time;
    
    // The login time will be recorded at server site
    private long m_login_time;
    // The login time will be recorded at server site as well
    private long m_logout_time;
    // The IP address of this user recorded each time this user logs on
    private String m_ip_address;
    private int m_p2p_port;
    
    int m_login_falures;
    
    public ServerUserProfile(String user, String pass)
    {
        this.m_user = user;
        this.m_password = pass;
        this.m_message_queue = new LinkedList<InterSessionMessage>();
        this.m_black_list = new HashSet <String> ();
        this.m_login_time = 0;
        this.m_logout_time = 0;
        this.m_blocked_time = 0;
        this.m_login_falures = 0;
        this.m_ip_address = "0.0.0.0";
        this.m_p2p_port = 0;
    }
    
    public void setPassword(String pass)
    {
        this.m_password = pass;
    }
    
    public String getPassword()
    {
        return this.m_password;
    }
    
    public String getUser()
    {
        return this.m_user;
    }
    
    public boolean isBlocked()
    {
        if (System.currentTimeMillis() - this.m_blocked_time <= ServerEntity.getServerInstance().getBlockDuration())
        {
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void block()
    {
        this.m_blocked_time = System.currentTimeMillis();
        this.m_login_falures = 0;
    }
    
    public void plusLoginFailure()
    {
        if (this.m_login_falures < 3)
        {
            this.m_login_falures ++;
        }
        
        if (this.m_login_falures == 3)
        {
            this.block();
        }
    }
    
    public void plusLogin()
    {
        this.m_login_time = System.currentTimeMillis();
        this.m_login_falures = 0;
    }
    
    public void plusLogout()
    {
        this.m_logout_time = System.currentTimeMillis();
    }
    
    public long getLoginTime()
    {
        return this.m_login_time;
    }
    
    public long getLogoutTime()
    {
        return this.m_logout_time;
    }
    
    public void enqueuePeerMessage(String sender, String content, String host, long sent_time)
    {
        InterSessionMessage msg = new InterSessionMessage(sender, content, host, sent_time);
        this.m_message_queue.add(msg);
    }
    
    public InterSessionMessage dequeuePeerMessage()
    {
        return this.m_message_queue.poll();
    }
    
    public boolean inBlackList(String user)
    {
        return this.m_black_list.contains(user);
    }
    
    public void intoBlacklist(String user)
    {
        this.m_black_list.add(user);
    }
    
    public void outBlacklist(String user)
    {
        this.m_black_list.remove(user);
    }
    
    public void setIPAddress(String ip)
    {
        this.m_ip_address = ip;
    }
    
    public String getIPAddress()
    {
        return this.m_ip_address;
    }
    
    public void setP2PPort(int port)
    {
        this.m_p2p_port = port;
    }
    
    public int getP2PPort()
    {
        return this.m_p2p_port;
    }
}
