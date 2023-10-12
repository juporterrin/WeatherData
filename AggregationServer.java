import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class AggregationServer {

    //maintain a static common lamport clock value
    private static AtomicInteger lamportClock = new AtomicInteger(0);
    public AtomicInteger getLamportClock() {
        return lamportClock;
    }

    public static void incrementLamportClock() {
        lamportClock.incrementAndGet();
    }

    public static void main(String[] args) {
        int port = 4567;
        if(args.length > 0){
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid port, please check port provided and try again later, now using default port 4567...");
            }
        }

        // Port for the server to listen on
        int serverPort = port;

        try (ServerSocket serverSocket = new ServerSocket(serverPort)) {
            System.out.println("AggregationServer is running on port " + serverPort + " and waiting for connections...");

            while (true) {
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))) {

                    // Handle client request
                    String request = in.readLine();//
                    System.out.println("Receive a request: " + request);
                    if (request != null) {
                        //if the request is Get request
                        if (request.equals("GET")) {
                            String GETid = in.readLine();
                            try (FileReader reader = new FileReader("weatherData/"+GETid+".json");
                                 BufferedReader fileReader = new BufferedReader(reader)) {
                                StringBuilder jsonResponse = new StringBuilder();
                                String line;
                                while ((line = fileReader.readLine()) != null) {
                                    jsonResponse.append(line);
                                }

                                //send the content of the file to GC
                                out.println(jsonResponse.toString());
                                System.out.println("Sent JSON response to GET request.");
                            } catch (IOException e) {
                                out.println("404 NOT FOUND");
                            }

                            //increment lamport clock value after the GET request has been processed
                            lamportClock.incrementAndGet();

                            //if the requests is Put request
                        }
                        else if (request.equals("PUT")) {
                            String length = in.readLine();
                            String PUTid = in.readLine();
                            // Handle PUT request to store JSON data
                            String fileName = "weatherData/weatherData.json";

                            //check if the given id exists, if false then create, return status 201 - HTTP_CREATED
                            // , else then update, return status 200
                            boolean fileExistence = FileExistence("IDS"+PUTid+".json");
                            String weatherData = in.readLine();
                            System.out.println(weatherData);

                            // Parse the JSON data
                            try {
                                // Use a regular expression to extract the JSON part
                                Pattern pattern = Pattern.compile("\\{(.+?)\\}");
                                Matcher matcher = pattern.matcher(weatherData);

                                if (matcher.find()) {
                                    String jsonPart = matcher.group(0); // Extract the JSON part
                                    System.out.println("Extracted JSON: " + jsonPart);

                                    // Create a JSON file and write the extracted JSON part to it
                                    try (FileWriter fileWriter = new FileWriter("weatherData/IDS"+ PUTid +".json")) {
                                        fileWriter.write(jsonPart);
                                    } catch (IOException e) {
                                        System.out.println("Fail to create JSON file.");
                                    }

                                    System.out.println("Saved extracted JSON data to IDS"+ PUTid +".json'");
                                } else {
                                    if (length.equals("0")) {
                                        out.println("204");
                                        System.out.println("JSON Content is empty");
                                    } else {
                                        out.println("500 - Internal server error");
                                        System.out.println("No JSON data found in the input string.");
                                    }
                                }

                            } catch (Exception e) {
                                out.println("500 - Internal server error");
                                System.out.println("Invalid JSON data");
                            }

                            if (fileExistence) {
                                out.println("200");
                            } else {
                                out.println("201 - HTTP_CREATED");
                            }
                            jsonCreateBackUp("weatherData/IDS"+ PUTid +".json", PUTid);
                            deleteInactiveWeatherData(PUTid);
                            //increment lamport clock value after the PUT request has been processed
                            incrementLamportClock();

                        } else {
                            out.println("Invalid request:" + 400);
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Fail to start AggregationServer");
        }
    }
    /**
     * Creates a backup JSON file based on the given input file and identifier.
     *
     * @param inputFile The path to the input JSON file.
     * @param id        The identifier used to generate the backup file name.
     */
    private static void jsonCreateBackUp(String inputFile, String id) {
        String baseDirectory = "dataBackup";
        String baseFileName = "backup_IDS" + id;
        String outputFile = baseDirectory + "/" + baseFileName + ".json";

        int suffix = 1;
        while (new File(outputFile).exists()) {
            outputFile = baseDirectory + "/" + baseFileName + "_" + suffix + ".json";
            suffix++;
        }

        try (FileReader reader = new FileReader(inputFile);
             FileWriter writer = new FileWriter(outputFile)) {
            // Create Gson obj
            Gson gson = new GsonBuilder().setPrettyPrinting().create();

            // parse the content of json file
            Object jsonObject = gson.fromJson(reader, Object.class);


            //writh the formatted content to the file
            gson.toJson(jsonObject, writer);
        } catch (IOException e) {
            System.out.println("No JSON data was found");
        }
    }

    //initial a static timer
    private static Timer timer = new Timer();
    /**
     * Deletes inactive weather data files associated with a specific identifier.
     *
     * @param id The identifier used to locate and delete inactive weather data files.
     */
    private static void deleteInactiveWeatherData( final String id) {

        //cancel previous timer
        timer.cancel();

        timer = new Timer();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                //set the path of the file to be deleted
                String deleteFilePath = "weatherData" + "/IDS" + id + ".json";

                File fileToDelete = new File(deleteFilePath);

                if (fileToDelete.exists()) {
                    if (fileToDelete.delete()) {
                        System.out.println("PUTid: "+ id + " contact lost, File deleted successfully: " + deleteFilePath);
                    } else {
                        System.err.println("Failed to delete file: " + deleteFilePath);
                    }
                } else {
                    System.out.println("File does not exist: " + deleteFilePath);
                }
            }
        }, 30000);
    }

    /**
     * Checks if the specified file exists in the file system.
     *
     * @param fileName The path and name of the file to check.
     * @return True if the file exists; otherwise, false.
     */
    private static boolean FileExistence(String fileName){
        //set directory path
        String directoryPath = "weatherData";

        //create a file obj to represent directory
        File directory = new File(directoryPath);

        // check if the directory exists
        if (directory.exists() && directory.isDirectory()) {
            // search the file in this direcotry
            String fileNameToFind = fileName;
            File[] filesInDirectory = directory.listFiles();

            if (filesInDirectory != null) {
                for (File file : filesInDirectory) {
                    if (file.isFile() && file.getName().equals(fileNameToFind)) {
                        return true;
                    }
                }
            }

            // not found the file
            return false;
        } else {
            // directory does not exist
            return false;
        }

    }


}
