# om2m-cep
Complex Event Processing library for om2m - open source IOT platform

Implementation instructions:

Clone OM2M project into Eclipse Kepler as described on https://wiki.eclipse.org/OM2M/one/Clone
Create custom plugin as described on https://wiki.eclipse.org/OM2M/one/Developer (at the stage "Add the plugin to the OM2M product(s)" add plugin to in-cse)

Open Eclipse Kepler
Copy om2m-cep/library/om2m-cep.jar to your projects lib folder (e.g. org.eclipse.om2m.sample.ipe/lib)
Select your plugin project and press F5
Go to Project -> Properties -> Java build path -> Libraries -> Add JARS... 
Open your plugins lib folder (e.g. org.eclipse.om2m.sample.ipe -> lib) and select om2m-cep.jar file
Click OK to close libraries window
Open your projects MANIFEST.MF file -> Runtime -> Classpath -> Add...
Select lib -> om2m-cep.jar and press OK
Press CTRL + S to save MANIFEST.MF file

Create new class named Data.java
In this class you have to declare which data will Cep server process. Modify it for your needs.
In our demo we will process only double values from the sensor.

Open Monitor.java file and add "static CepHttpServlet cepServer;"
Add "cepServer = new CepHttpServlet(cseService, Data.class);"
Add "cepServer.run();"
Add "cepServer.stopThread();"
Add "cepServer.insertDevice(sensorId);"
Add "Data data= new Data(sensorValue);	cepServer.sendEvent(data, sensorId);"

Right click on org.eclipse.om2m project -> Run as -> Maven install

Open explorer and go to org.eclipse.om2m\org.eclipse.om2m.site.in-cse\target\products\in-cse\win32\win32\x86_64
Double click on start.bat
Type "ss" into comand line and look for id of your recently created plugin (e.g. org.eclipse.om2m.sample.ipe)
Type "start id" (e.g. "start 32")

Open web browse and type into url "http://localhost:8080/webpage"
Default values for login are "admin" : "admin"
Here you can monitor your data catched into cep rules

Open new tab and type into url "http://localhost:8081/cep" 
Default values for login are also "admin" : "admin"
Here you can add/delete/update cep rules for specific device

For example add new cep rule:



