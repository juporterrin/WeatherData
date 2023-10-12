# Assignment2



### 1.Introduction to the project structure

![image-20230923121603410](/Users/zhengyu/Library/Application Support/typora-user-images/image-20230923121603410.png)

#### 1.dataBackup directory

```markdown
This is the folder that belongs to AggregationServer in which the weather data json files are stored for backup functionality in case of the file corruption caused by an unexpected shutdown of the machine when performing file operations.

The json files in this folder are persistent, and they are generated along with the correspoding weather data json files in weatherData folder which are to be deteted once the content server becomes inresponsive to AggregationServers.

Apart from, this folder has version control functionality, as you can see the names of files "backup_IDS89898.json", "backup_IDS89898_1.json" and "backup_IDS89898_2.json", they indicate that every time an update operation is performed, a version-controlled backup json file will be generated by adding the suffix of incrementing 1 after its file name.
```

Sample content of backup_IDS60901.json

```json
{
  "id": "IDS60901",
  "name": "Adelaide (West Terrace /  ngayirdapira)",
  "state": "HH",
  "time_zone": "DDDDDDDD",
  "lat": -34.9,
  "lon": 138.6,
  "local_date_time_full": 2.023071516E13,
  "air_temp": 13.3,
  "apparent_t": 9.5,
  "cloud": "Partly cloudy",
  "dewpt": 5.7,
  "press": 1023.9,
  "rel_hum": 60.0,
  "wind_dir": "S",
  "wind_spd_kmh": 15.0,
  "wind_spd_kt": 8.0
}
```



#### 2.Input direcotry

```markdown
This folder contains the input files of ContentServer
```

An valid input file , the content of which can be converted into json format is as below

```txt
id:IDS60901
name:Adelaide (West Terrace /  ngayirdapira)
state: CCCCCCCCCCCCCCCC
time_zone:CST
lat:-34.9
lon:138.6
local_date_time:15/04:00pm
local_date_time_full:20230715160000
air_temp:8888
apparent_t:8888
cloud:Partly cloudy
dewpt:5.7
press:1023.9
rel_hum:8888
wind_dir:S
wind_spd_kmh:15
wind_spd_kt:8

```

An invalid file “putMessage2.txt” that can be used to test its correctness is below, 

```txt
//sample command for starting AggregationServer
make runAggregationServer ARGS1="8888"

//sample command for starting ContentServer
make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS60901.txt"

//sample command for starting GETClient
make runGETClient ARGS1="http://localhost:8888" ARGS2="IDS60901"
```



3.weatherData directory

```markdown
This is the folder where the temporary weather data json files that are designed to be deleted after they have lost the communication with AS for 30 seconds, such as "IDS89898.json" are generated, and GETClient can send get requests to read the wished content of the json files.
```

Sample content of IDS89898.json,  it has similar content to its back files, and  is unformatted(a single line), but still valid json file.

```json
{"id":"IDS60901","name":"Adelaide (West Terrace /  ngayirdapira)","state":"BBBBBBBBBBBBBBBBBBBBB","time_zone":"CST","lat":-34.9,"lon":138.6,"local_date_time_full":20230715160000,"air_temp":8888,"apparent_t":8888,"cloud":"Partly cloudy","dewpt":5.7,"press":1023.9,"rel_hum":8888,"wind_dir":"S","wind_spd_kmh":15,"wind_spd_kt":8}
```



#### 4.AggregationServer(AS), ContentServer(CS) and GETClient(GC)

```markdown
They are the source code of three main components for this assignment.
```



#### 5.*.jar

```
They are external libraris imported to simplify the parsing code work.
```



### 2.Start the project

The suggested commands to start these 3 components, with arguments(you can change them as you want)

```markdown
//sample command for starting AggregationServer
make runAggregationServer ARGS1="8888"

//sample command for starting ContentServer
make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS60901.txt"

//sample command for starting GETClient
make runGETClient ARGS1="http://localhost:8888" ARGS2="IDS60901"
```



#### 1.Compile 3 components

```shell
make all
```

With seeing the .class files created, it means the compliation is successful.

#### 1.Start AS

```shell
//sample command for starting AggregationServer(you can discard the ARGS1, then the AS will be //running on default port 4567)

make runAggregationServer ARGS1="8888"
```

```shell
//when you see the output below printed, it means AS is running on 8888 successfully.
AggregationServer is running on port 8888 and waiting for connections...
```

#### 2.Start CS

```SHELL
//sample command for starting ContentServer
//it is going to connect AS running on port 8888, sending a put request
//with sending the precessed content of input file "IDS60901.txt" to AS

make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS60901.txt"
```



#### 3.Start GC

```shell
//sample command for starting GETClient
//it is going to connect AS running on port 8888, sending a get request
//to fetch the weather data with station id "IDS60901"

make runGETClient ARGS1="http://localhost:8888" ARGS2="IDS60901"
```





### 3.Implementation of Lamport clock

##### The overview of the design process for implementing the Lamport Clock in the system:

**Step 1: Initialization**
- Each server in the system is initialized with a Lamport Clock, which is set to zero initially.

**Step 2: Event Handling**
- When a server performs an internal event or receives a request (GET or PUT), it increments its Lamport Clock by one.

**Step 3: Message Sending**
- When a server sends a message to another server, it includes its Lamport Clock value in the message.

**Step 4: Message Receiving**
- Upon receiving a message from another server, the recipient server compares the Lamport Clock value in the message with its own Lamport Clock.

**Step 5: Clock Synchronization**
- The recipient server updates its Lamport Clock to the maximum of its current value and the incoming Lamport Clock value plus one. This ensures that the Lamport Clock reflects the logical order of events.

**Step 6: Event Processing**
- The recipient server processes the received message or event based on the Lamport Clock value. It ensures that events are executed in the correct order according to Lamport timestamps.

**Step 7: Tracking Request Progress (Optional)**
- In the case of GET and PUT requests in the AggregationServer, the Lamport Clock is also incremented to track the progress of these operations.

**Step 8: Handling Message Delays**
- The Lamport Clock helps handle message delays or out-of-order message delivery. Servers can correctly order events, even when messages arrive out of sequence.

**Step 9: Handling Timeouts**
- To implement the requirement of deleting JSON files in the AggregationServer, a timer is set for 30 seconds. If no PUT request with the same PUTid is received during this time, the server deletes the corresponding JSON file. If a PUT request arrives, the timer is reset to 30 seconds.

**Step 10: Ensuring Consistency**
- The use of Lamport Clocks ensures that events and messages are processed in a consistent and logical order across all servers in the system.

This design allows the system to maintain logical event ordering and handle various scenarios, including message delays, out-of-order messages, and the timely deletion of JSON files based on activity.



### 4.Testing

#### 1.Status code related testing

##### 1.1 201 - HTTP_CREATEED

How:

```
When the AS is running, run CS with the input file that is feed to the system for the first time.
```

Sample command

```shell
 make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS66666.txt"
```

Expected output should contain “201 - HTTP_CREATEED” to determine its correctness.

The output:

```shell
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS66666.txt"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer http://localhost:8888 IDS66666.txt
http://localhost:8888
IDS66666.txt
PUT /weather.json HTTP/1.1 User-Agent: ATOMClient/1/0 Content-Type: (application/json) Content-Length: 305{"id":"IDS60901","name":"Adelaide (West Terrace /  ngayirdapira)","state":"CCCCCCCCCCCCCCCC","time_zone":"CST","lat":-34.9,"lon":138.6,"local_date_time_full":20230715160000,"air_temp":8888,"apparent_t":8888,"cloud":"Partly cloudy","dewpt":5.7,"press":1023.9,"rel_hum":8888,"wind_dir":"S","wind_spd_kmh":15,"wind_spd_kt":8}
Received response: 201 - HTTP_CREATED

```



##### 1.2 200

How

```markdown
Based on 1.1, run aforementioned make command again within 30s(before the temporary json file in the weatherData is deleted), which means this is an update operation.
```



Sample Command

```shell
 make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS66666.txt"
```



Expected output should contain “200” to determine its correctness.

The Output

```shell
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS66666.txt"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer http://localhost:8888 IDS66666.txt
http://localhost:8888
IDS66666.txt
PUT /weather.json HTTP/1.1 User-Agent: ATOMClient/1/0 Content-Type: (application/json) Content-Length: 304{"id":"IDS60901","name":"Adelaide (West Terrace /  ngayirdapira)","state":"DDDDDDDDDDDDDDD","time_zone":"CST","lat":-34.9,"lon":138.6,"local_date_time_full":20230715160000,"air_temp":8888,"apparent_t":8888,"cloud":"Partly cloudy","dewpt":5.7,"press":1023.9,"rel_hum":8888,"wind_dir":"S","wind_spd_kmh":15,"wind_spd_kt":8}
Received response: 200

```



##### 1.3 400

How

```java
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

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

}

```

Run above test program to send a POST request to AS

Expected output should contain “400” to determine its correctness.

The output:
```shell
Invalid request:400
```



##### 1.4 204

How

```markdown
When the AS is running, run CS with empty input file
```

Sample command

```shell
 make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS00000.txt"
```

Expected output should contain “204” to determine its correctness.

The output

```shell
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS00000.txt"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer http://localhost:8888 IDS00000.txt
http://localhost:8888
IDS00000.txt
PUT /weather.json HTTP/1.1 User-Agent: ATOMClient/1/0 Content-Type: (application/json) Content-Length: 0{}
Received response: 204

```

##### 1.5 500

How

```
When the AS is running, run CS with input file with incorrect JSON
```

Sample command

```
make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS12345.txt" 
```

The content of IDS12345.txt

```txt
asdijasldjljfas
sadlfknaslkfnsadf
lknsadflnasdlfns
ljsandflasndf
a,sjndf,asndfnsadf
nsadknflsadnflsadnf
```

Expected output should contain “500 - Internal server error” to determine its correctness.

The output

```shell
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS12345.txt" 
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer http://localhost:8888 IDS12345.txt
http://localhost:8888
IDS12345.txt
PUT /weather.json HTTP/1.1 User-Agent: ATOMClient/1/0 Content-Type: (application/json) Content-Length: 104{}
Received response: 500 - Internal server error
make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS12345.txt" 

```



##### 1.6 404

How

```
GETClient try to fetch data with incorrect station id
```

Sample command

```
make runGETClient ARGS1="http://localhost:8888" ARGS2="aslidj"  
```

The output

```shell
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runGETClient ARGS1="http://localhost:8888" ARGS2="aslidj"  
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar GETClient.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar GETClient http://localhost:8888 aslidj
404 NOT FOUND, Please provide a correct station id and try again...

```





#### 2.Functionality Testing

##### 2.1 PUT opeartion

How

```
When AS is running, run CS with correct input file
```



Sample command

```shell
 make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS60901.txt"
```

With seeing the json files IDS60901.json created in weatherData folder and backup_IDS60901.json created in dataBackup folder with correct json content to determine its correctness.



![image-20230923134351744](/Users/zhengyu/Library/Application Support/typora-user-images/image-20230923134351744.png)



![image-20230923134409681](/Users/zhengyu/Library/Application Support/typora-user-images/image-20230923134409681.png)



##### 2.2 GET opearation for many read clients

How

```
When AS is running, open multiple terminals to run GETClient with correct station ids
```

sample commands

1.

```shell
make runGETClient ARGS1="http://localhost:8888" ARGS2="IDS60901"
```

2.

```shell
make runGETClient ARGS1="http://localhost:8888" ARGS2="IDS6666"
```



Outputs

1.

```json
Received JSON data:
{
  "id": "IDS60901",
  "name": "Adelaide (West Terrace /  ngayirdapira)",
  "state": "DDDDDDDDDDDDDDD",
  "time_zone": "CST",
  "lat": -34.9,
  "lon": 138.6,
  "local_date_time_full": 2.023071516E13,
  "air_temp": 8888.0,
  "apparent_t": 8888.0,
  "cloud": "Partly cloudy",
  "dewpt": 5.7,
  "press": 1023.9,
  "rel_hum": 8888.0,
  "wind_dir": "S",
  "wind_spd_kmh": 15.0,
  "wind_spd_kt": 8.0
}

```

2.

```json
Received JSON data:
{
  "id": "IDS66666",
  "name": "Adelaide (West Terrace /  ngayirdapira)",
  "state": "DDDDDDDDDDDDDDD",
  "time_zone": "CST",
  "lat": -34.9,
  "lon": 138.6,
  "local_date_time_full": 2.023071516E13,
  "air_temp": 8888.0,
  "apparent_t": 8888.0,
  "cloud": "Partly cloudy",
  "dewpt": 5.7,
  "press": 1023.9,
  "rel_hum": 8888.0,
  "wind_dir": "S",
  "wind_spd_kmh": 15.0,
  "wind_spd_kt": 8.0
}

```



##### 2.3 AggregationServer expunging expired data (30s)

How

```
After creation and update operation, wait for 30 seconds to see if the json file is deleted
```

Yes it is.



##### 2.4 Lamport Clocks Testing

In UnitTest  class

```java
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
```





![image-20231012150729898](/Users/zhengyu/Library/Application Support/typora-user-images/image-20231012150729898.png)



The implementation of Lamport Clocks passed the unit test.





##### 2.5All error codes are implemented and  Content servers and fault tolerant

###### 2.5.1: CS and GC try to connect to AS when AS is not running

How

```
Run CS and GC, when AS is not running
```



Output

CS

```
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="http://localhost:8888" ARGS2="IDS60901.txt"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer http://localhost:8888 IDS60901.txt
http://localhost:8888
IDS60901.txt
Connection to AggregationServer timeout, please check your serverHost or serverPort or try again later

```

GC

```
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runGETClient ARGS1="http://localhost:8888" ARGS2="IDS60901"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar GETClient.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar GETClient http://localhost:8888 IDS60901
Connection to AggregationServer timeout, please check your serverHost or serverPort or try again later

```



###### 2.5.2 Invalid arguments

How

```
Provide incorrect arguments when running AS, CS and GC.
```



Sample commands

1.AS

```shell
make runAggregationServer ARGS1="KAJSDKHJ"   
```

Output

```
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runAggregationServer ARGS1="KAJSDKHJ"                    
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar AggregationServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar AggregationServer KAJSDKHJ
Invalid port, please check port provided and try again later, now using default port 4567...

```



2.GC

Incorrect URL

```shell
make runGETClient ARGS1="asdijlijasljli" ARGS2="IDS60901"
```



Output

```
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar GETClient asdijlijasljli IDS60901
Invalid arguments, please provide correct arguments!
```



3.CS

Incorrect Station ID

```
make runContentServer ARGS1="http://localhost:8888" ARGS2="asoijfalsjf"
```

The output

```shell
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="http://localhost:8888" ARGS2="asoijfalsjf"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer http://localhost:8888 asoijfalsjf
Invalid arguments, please provide correct arguments!
```



Incorrect URL

```shell
make runContentServer ARGS1="laisjdlasijdlaisjd" ARGS2="IDS60901"
```

The output

```
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="laisjdlasijdlaisjd" ARGS2="IDS60901"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer laisjdlasijdlaisjd IDS60901
Invalid arguments, please provide correct arguments!

```





Incorrcet URL and Station Id

```shell
make runContentServer ARGS1="laisjdlasijdlaisjd" ARGS2="asdlajsdliajsdliajs"
```

The output

```
zhengyu@Zhengs-MacBook-Pro Assignment2_copy % make runContentServer ARGS1="laisjdlasijdlaisjd" ARGS2="asdlajsdliajsdliajs"
javac -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer.java
java -cp .:gson-2.8.9.jar:hamcrest-core-1.3.jar ContentServer laisjdlasijdlaisjd asdlajsdliajsdliajs
Invalid arguments, please provide correct arguments!
```




