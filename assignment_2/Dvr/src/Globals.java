
public class Globals
{
    public static final boolean debug = false;
    
    public static void debugPrintln(String str)
    {
        if (debug)
        {
            System.out.println(str);
        }
    }

    public static void debugPrint(String str)
    {
        if (debug)
        {
            System.out.print(str);
        }
    }
    
    public static void debugPrintStackTrace(Exception e)
    {
        if (debug)
        {
            System.out.println(e.toString());
            StackTraceElement [] traces = e.getStackTrace();
            for (StackTraceElement trace : traces)
            {
                System.out.println(trace);
            }
        }
    }
    
    public static void println(String str)
    {
        System.out.println(str);
    }
    
    public static void print(String str)
    {
        System.out.print(str);
    }
}
