/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

/**
 * Definition of the element for enqueue and dequeue of
 * inter session messages
 * @author Chunnan Sheng
 *
 */
public class InterSessionMessage
{
    // The person who will receive this message
    private String m_sender;
    private String m_content;
    private String m_host;
    private long m_sent_time;
    private boolean m_login;

    public InterSessionMessage(String sender, String content, String host, long sent_time)
    {
        this.m_sender = sender;
        this.m_content = content;
        this.m_host = host;
        this.m_sent_time = sent_time;
        this.m_login = false;
    }
    
    public InterSessionMessage(String sender, boolean login, String host, long sent_time)
    {
        this.m_sender = sender;
        this.m_content = null;
        this.m_host = host;
        this.m_sent_time = sent_time;
        this.m_login = login;
    }
    
    public boolean isLoginPresence()
    {
        return this.m_login;
    }

    public String getSender()
    {
        return this.m_sender;
    }

    public String getContent()
    {
        return this.m_content;
    }

    public String getHost()
    {
        return this.m_host;
    }

    public long getSentTime()
    {
        return this.m_sent_time;
    }
    
    public boolean getPresenceType()
    {
        return this.m_login;
    }
}