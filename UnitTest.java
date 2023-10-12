import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.Assert.assertEquals;

public class UnitTest {

    /*
    *test for: simulate to send a request other than get and put
    */

    @Test
    public void invalidRequest(){
        try (Socket socket = new Socket("localhost", 8888);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


            // Configure HTTP POST request
            out.println("POST");


            // Receive and parse the JSON response
            String response = in.readLine();
            System.out.println(response);


        } catch (Exception e) {
            System.out.println("Connection to AggregationServer timeout, please check your serverHost or serverPort or try again later");
        }
    }

    /*
    *test for: the functionality of Lamport Clock
    */

    @Test
    public void testLamportClock() {
        AggregationServer aggregationServer = new AggregationServer();
        ContentServer contentServer = new ContentServer();
        GETClient getClient = new GETClient();

        // Test AggregationServer Lamport Clock
        int initialAggrServerValue = aggregationServer.getLamportClock().get();
        aggregationServer.incrementLamportClock();
        int incrementedAggrServerValue = aggregationServer.getLamportClock().get();
        assertEquals(initialAggrServerValue + 1, incrementedAggrServerValue);

        // Test ContentServer Lamport Clock
        int initialContentServerValue = contentServer.getLamportClock().get();
        contentServer.incrementLamportClock();
        int incrementedContentServerValue = contentServer.getLamportClock().get();
        assertEquals(initialContentServerValue + 1, incrementedContentServerValue);

        // Test GetClient Lamport Clock
        int initialGetClientValue = getClient.getLamportClock().get();
        getClient.incrementLamportClock();
        int incrementedGetClientValue = getClient.getLamportClock().get();
        assertEquals(initialGetClientValue + 1, incrementedGetClientValue);
    }

}


