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

public class Client
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
        if (args.length != 2 && args.length != 3)
        {
            Globals.println("Command format: Client <server_IP> <server_port>");
            return;
        }

        String server_ip = args[0];
        int server_port = Integer.parseInt(args[1]);
        
        if (args.length > 2 && args[2].equalsIgnoreCase("debug"))
        {
            Globals.debug = true;
        }

        ClientEntity client = ClientEntity.createClientInstance(server_ip, server_port);
        client.start();
    }
}