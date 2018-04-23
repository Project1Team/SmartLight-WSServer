import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;


public class Server extends WebSocketServer {

    public Server(int port ) throws UnknownHostException {
        super( new InetSocketAddress( port ) );
    }

    public Server(InetSocketAddress address ) {
        super( address );
    }

    @Override
    public void onOpen( WebSocket conn, ClientHandshake handshake ) {
        //broadcast( "new connection: " + handshake.getResourceDescriptor() );
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + " entered the room!" );
    }

    @Override
    public void onClose( WebSocket conn, int code, String reason, boolean remote ) {
        broadcast( conn + " has left the room!" );
        //System.out.println( conn + " has left the room!" );
    }

    @Override
    public void onMessage( WebSocket conn, String message ) {
        broadcast( message );
        System.out.println( conn.getRemoteSocketAddress().getAddress().getHostAddress() + ": " + message );
        /*
        if (message.equals("LOGIN"))
        {
            System.out.println( "Server IS GOOD" );
        }*/
    }

    public static void main( String[] args ) throws InterruptedException , IOException {
<<<<<<< HEAD
        int port = 8887;
=======
        WebSocketImpl.DEBUG = true;
        int port = 8888;
>>>>>>> b6e0779d174e9905d97fe4cf6107749fa2b91090
        try {
            port = Integer.parseInt( args[ 0 ] );
        } catch ( Exception ex ) {
        }
        Server s = new Server( port );
        s.start();
        System.out.println( "Server started on port: " + s.getPort() );

        BufferedReader sysin = new BufferedReader( new InputStreamReader( System.in ) );
        while ( true ) {

            String in = sysin.readLine();
            s.broadcast( in );
            if( in.equals( "exit" ) ) {
                s.stop(1000);
                break;

            }
        }
    }
    @Override
    public void onError( WebSocket conn, Exception ex ) {
        /*
        ex.printStackTrace();
        if( conn != null ) {
            // some errors like port binding failed may not be assignable to a specific websocket
        }
        */
    }

    @Override
    public void onStart() {
        //System.out.println("Server started!");
    }

}
