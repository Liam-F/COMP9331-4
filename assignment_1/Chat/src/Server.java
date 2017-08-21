/*********************************************
 * 
 *      COMP9331 Assignment 1
 *      
 *      Programmed by   Chunnan Sheng
 *      Student Code    z5100764
 *      Date            15/04/2017
 * 
 *********************************************/

import java.io.IOException;


public class Server
{
    /**
     * Entrance of this program
     * Listening port is its argument.
     * 
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws Exception
    {
        if (args.length != 3 && args.length != 4)
        {
            Globals.println("Command format: Server <server_port> <block_duration> <timeout>");
            return;
        }

        int port = Integer.parseInt(args[0]);
        long block_duration = Long.parseLong(args[1]);
        long timeout = Long.parseLong(args[2]);
        
        if (args.length > 3 && args[3].equalsIgnoreCase("debug"))
        {
            Globals.debug = true;
        }

        ServerEntity server = ServerEntity.createServerInstance(port, block_duration * 1000, timeout * 1000);
        server.start();
    }
}
