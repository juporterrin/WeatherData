import com.google.gson.Gson;
import java.io.*;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class ContentServer {
    /**
     * Parses the content of a file into a JSON string.
     *
     * @param filename The name of the file to be parsed.
     * @return A JSON string containing the parsed content of the file.
     */
    private static String parseFileToJsonString(String filename) {
        Map<String, Object> weatherData = new LinkedHashMap<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(":");
                if (parts.length == 2) {
                    String key = parts[0].trim();
                    String value = parts[1].trim();

                    // Convert numeric values to the appropriate type
                    Object parsedValue = tryParse(value);
                    if (parsedValue != null) {
                        weatherData.put(key, parsedValue);
                    } else {
                        weatherData.put(key, value);
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Fail to parse the content of current file to JSON string.");
        }

        // Convert the map to JSON using Gson
        Gson gson = new Gson();
        return gson.toJson(weatherData);
    }

    /**
     * Tries to parse a string into a numeric value (either Double or Long).
     *
     * @param value The string to be parsed.
     * @return If parsing is successful, returns the parsed numeric value as a Double or Long.
     *         If parsing fails, returns null.
     */
    private static Object tryParse(String value) {
        try {
            if (value.contains(".")) {
                return Double.parseDouble(value);
            } else {
                return Long.parseLong(value);
            }
        } catch (NumberFormatException e) {
            return null;
        }
    }
    /**
     * Reads the length (in bytes) of a file.
     *
     * @param fileName The name of the file to read.
     * @return The length of the file in bytes, or -1 if the file does not exist or cannot be read.
     */
    private static long FileLengthReader(String fileName){

        // Create a file obj
        File file = new File(fileName);

        // check if the file exists
        if (file.exists() && file.isFile()) {
            // get the length of the file
            long fileLength = file.length();
            return fileLength;
        } else {
            // file does not exist, or is not a regular file
            System.out.println("File" + fileName + "does not exist or is not a regular file");
            return 0;
        }

    }

    public static String[] parseArguments(String arg1, String arg2) {
        //use regular expression to match the pattern of arg1 and arg2
        String hostPattern = "http://(.*):(\\d+)";
        String idPattern = "IDS(\\d+)\\.txt";

        if (arg1.matches(hostPattern) && arg2.matches(idPattern)) {
            // extract host and port
            String[] hostPort = arg1.replace("http://", "").split(":");
            String host = hostPort[0];
            String port = hostPort[1];

            // extract id
            String id = arg2.replaceAll(idPattern, "$1");

            //store host, port and id into a string arr and return it
            String[] result = new String[]{host, port, id};
            return result;
        } else {
            System.err.println("Invalid arguments, please provide correct arguments!");
            return null;
        }
    }
    //maintain a static common lamportClock value
    private static AtomicInteger lamportClock = new AtomicInteger(0);

    public AtomicInteger getLamportClock() {
        return lamportClock;
    }

    public static void incrementLamportClock() {
        lamportClock.incrementAndGet();
    }


    public static void main(String[] args) {

        String[] address = parseArguments(args[0], args[1]);

        if (address == null || address.length < 3) {
            return; // Exit the program gracefully
        }
        String id = address[2];


        String serverHost = address[0];
        int serverPort = Integer.parseInt(address[1]);

        String path = "/";

        try (Socket socket = new Socket(serverHost, serverPort);
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {

            String filename = "input/"+"IDS"+address[2]+".txt";
            String jsonString = parseFileToJsonString(filename);
            long length = FileLengthReader(filename);

            String content = "PUT /weather.json HTTP/1.1 User-Agent: ATOMClient/1/0 Content-Type: (application/json) Content-Length: "+ length;
            // Send a PUT request with the content from the file as JSON data to the AggregationServer
            out.println("PUT");
            out.println(length);
            out.println(id);
            out.println(content + jsonString);
            System.out.println(content + jsonString);

            // Receive a response (if needed)
            String response = in.readLine();
            System.out.println("Received response: " + response);
            //increment lamport clock when sending a put request.
            incrementLamportClock();

        } catch (Exception e) {
            System.out.println("Connection to AggregationServer timeout, please check your serverHost or serverPort or try again later");
        }
    }
}
