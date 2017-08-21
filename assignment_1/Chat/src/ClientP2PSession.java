/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

public class ClientP2PSession
{
    private ClientP2PSendingSession m_s_session;
    private ClientP2PReceivingSession m_r_session;
    private String m_remote_user;

    public ClientP2PSession(String remote_user)
    {
        this.m_remote_user = remote_user;
        this.m_s_session = null;
        this.m_r_session = null;
        Globals.println(this.m_remote_user + " started P2P with you");
    }
    
    public void setSendingSession(ClientP2PSendingSession s_session)
    {
        synchronized (this)
        {
            this.m_s_session = s_session;
            this.m_s_session.setParent(this);
        }
    }
    
    public void setReceivingSession(ClientP2PReceivingSession r_session)
    {
        synchronized (this)
        {
            this.m_r_session = r_session;
            this.m_r_session.setParent(this);
        }
    }

    public ClientP2PSendingSession getSendingSession()
    {
        synchronized (this)
        {
            return this.m_s_session;
        }
    }

    public ClientP2PReceivingSession getReceivingSession()
    {
        synchronized (this)
        {
            return this.m_r_session;
        }
    }

    public String getRemoteUser()
    {
        synchronized (this)
        {
            return this.m_remote_user;
        }
    }

    public void stop()
    {
        synchronized (this)
        {
            if (null != this.m_r_session)
            {
                this.m_r_session.stop();
            }
            if (null != this.m_s_session)
            {
                this.m_s_session.stop();
            }
            
            if (null != ClientEntity.getP2PSession(this.m_remote_user))
            {
                Globals.println(this.m_remote_user + " disconnected from P2P");
                ClientEntity.removeP2PSession(this.m_remote_user);
            }
        }
    }
}
