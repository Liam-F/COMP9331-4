
public class NeighborNode
{
    private String m_name;
    private Cost m_cost;
    /**
     * The pr_cost is for emulation of Poison Reverse scenarios
     */
    private Cost m_pr_cost;
    private int m_port;
    
    private boolean m_dead;
    private long m_last_msg_time;
    
    public NeighborNode(String name, Cost cost, Cost pr_cost, int port)
    {
        this.m_name = name;
        this.m_cost = cost;
        this.m_port = port;
        this.m_pr_cost = pr_cost;
        
        this.m_dead = true;
        this.m_last_msg_time = 0;
    }
    
    public boolean isDead()
    {
        return this.m_dead;
    }
    
    public void die()
    {
        this.m_dead = true;
    }
    
    public void live()
    {
        this.m_dead = false;
    }
    
    public void setLastMsgTime(long millis)
    {
        this.m_last_msg_time = millis;
    }
    
    public long getLastMsgTime()
    {
        return this.m_last_msg_time;
    }
    
    public void setCost(Cost cost)
    {
        this.m_cost = cost;
    }
    
    public Cost getCost()
    {
        return this.m_cost;
    }
    
    /**
     * The pr_cost is for emulation of Poison Reverse scenarios
     */
    public Cost getPrCost()
    {
        return this.m_pr_cost;
    }
    
    public String getName()
    {
        return this.m_name;
    }
    
    public int getPort()
    {
        return this.m_port;
    }
    
    
}
