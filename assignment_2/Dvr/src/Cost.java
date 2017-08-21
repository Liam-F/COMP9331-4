

public class Cost
{
    private double m_value;
    private long m_creation_time;
    
    /**
     * Assign value to cost
     * @param value
     */
    public Cost(double value)
    {
        this.m_value = value;
        this.m_creation_time = System.currentTimeMillis();
    }
    
    public Cost(Cost c1, Cost c2)
    {
        if (c1.isInfinite() || c2.isInfinite())
        {
            this.m_value = -1;
        }
        
        this.m_value = c1.m_value + c2.m_value;
        this.m_creation_time = System.currentTimeMillis();
    }
    
    /**
     * Default constructor of cost would assign infinite flag 
     */
    public Cost()
    {
        this.m_value = -1;
        this.m_creation_time = System.currentTimeMillis();
    }
    
    public static Cost costChange(Cost c1, Cost c2)
    {
        if (c1.isInfinite() || c2.isInfinite())
        {
            return new Cost();
        }
        
        return new Cost(c2.m_value - c1.m_value);
    }
    
    public long getCreationTime()
    {
        return this.m_creation_time;
    }
    
    public double getValue()
    {
        return this.m_value;
    }
    
    public boolean isInfinite()
    {
        if (this.m_value < 0)
        {
            return true;
        }
        
        return false;
    }
    
    public boolean equals(Object other)
    {
        if (null == other || !(other instanceof Cost))
        {
            return false;
        }
        
        Cost other_cost = (Cost)other;
        return this.m_value == other_cost.m_value;
    }
}
