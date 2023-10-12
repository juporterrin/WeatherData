
#all: AggregationServer.java ContentServer.java GETClient.java
#	javac -cp .:gson-2.8.9.jar AggregationServer.java ContentServer.java GETClient.java


#AggregationServer: AggregationServer$1.class
#	java AggregationServer
#
#ContentServer: ContentServer.class
#	java -cp .:gson-2.8.9.jar ContentServer
#
#GETClient: GETClient.class
#	java -cp .:gson-2.8.9.jar GETClient
JAVAC = javac
JAVA = java
CP = .:gson-2.8.9.jar:hamcrest-core-1.3.jar

all: AggregationServer ContentServer GETClient

AggregationServer: AggregationServer.java
	$(JAVAC) -cp $(CP) $<

ContentServer: ContentServer.java
	$(JAVAC) -cp $(CP) $<

GETClient: GETClient.java
	$(JAVAC) -cp $(CP) $<

runAggregationServer: AggregationServer
	$(JAVA) -cp $(CP) AggregationServer $(ARGS1)

runContentServer: ContentServer
	$(JAVA) -cp $(CP) ContentServer $(ARGS1) $(ARGS2)

runGETClient: GETClient
	$(JAVA) -cp $(CP) GETClient $(ARGS1) $(ARGS2)

clean:
	rm -f *.class
