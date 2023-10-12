import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

public class GETClient {


    /**
     * Parses a single argument string to extract specific components.
     *
     * @param arg1 The input argument string to be parsed.
     * @return An array of strings containing the parsed components.
     */
    private static String[] parseArguments(String arg1) {
        try {
            // Use regular expression to match the arg1 pattern
            String hostPattern = "http://(.*):(\\d+)";

            if (arg1.matches(hostPattern)) {
                // Extract host and port
                String[] hostPort = arg1.replace("http://", "").split(":");
                String host = hostPort[0];
                String port = hostPort[1];

                // Store host and port into a string array and return it
                String[] result = new String[]{host, port};
                return result;
            } else {
                System.err.println("Invalid arguments, please provide correct arguments!");
                return null;
            }
        } catch (Exception e) {
            // Handle any exceptions that may occur during parsing
            System.err.println("Error parsing arguments: " + e.getMessage());
            return null;
        }
    }


    private static AtomicInteger lamportClock = new AtomicInteger(0);

    public AtomicInteger getLamportClock() {
        return lamportClock;
    }

    public static void incrementLamportClock() {
        lamportClock.incrementAndGet();
    }
    public static void main(String[] args) {
        //set the server and port that GETClint is about to connect to


        String[] address = parseArguments(args[0]);
        String id = args[1];

        if (address == null || address.length < 2) {
            return; // Exit the program gracefully
        }

        String serverHost = address[0];
        int serverPort;

        try {
            serverPort = Integer.parseInt(address[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid server port. Please provide a valid integer as the second argument.");
            return; // Exit the program gracefully
        }
        String path = "/";

        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {


            // Configure HTTP Get request
            out.println("GET");
            out.println(id);
            out.println(path + " HTTP/1.1");
            out.println("Host: " + serverHost);
            out.println("Connection: close");
            out.println();

            // Receive and parse the JSON response
            String jsonResponse = in.readLine();
            if (jsonResponse != null) {
                // Create Gson bulider
                Gson gson = new GsonBuilder().setPrettyPrinting().create();

                // parse lines
                try {
                    Object jsonObject = gson.fromJson(jsonResponse, Object.class);

                    // format and print lines
                    String formattedJson = gson.toJson(jsonObject);
                    System.out.println("Received JSON data:");
                    System.out.println(formattedJson);
                } catch (Exception e) {
                    System.err.println(jsonResponse + ", Please provide a correct station id and try again...");
                }
            } else {
                System.out.println("No JSON response received from the server.");
            }

            incrementLamportClock();

        } catch (Exception e) {
            System.out.println("Connection to AggregationServer timeout, please check your serverHost or serverPort or try again later");
        }
    }
}
