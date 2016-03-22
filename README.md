# CEP library for OM2M
CEP (Complex evenet processing) library for open source IOT (Internet of things) platform OM2M (Open source platform for M2M communication)

Library (om2m-cep.jar) is available in folder [library](library).

##Implementation instructions

- Clone OM2M project into Eclipse Kepler as described on https://wiki.eclipse.org/OM2M/one/Clone
- Create custom plugin as described on https://wiki.eclipse.org/OM2M/one/Developer (at the stage "Add the plugin to the OM2M product(s)" add plugin to in-cse)

- Open Eclipse Kepler
- Copy om2m-cep/library/om2m-cep.jar to your projects lib folder (e.g. org.eclipse.om2m.sample.ipe/lib)
- Select your plugin project and press F5
- Go to Project -> Properties -> Java build path -> Libraries -> Add JARS... 
- Open your plugins lib folder (e.g. org.eclipse.om2m.sample.ipe -> lib) and select om2m-cep.jar file
- Click OK to close libraries window
- Open your projects MANIFEST.MF file -> Runtime -> Classpath -> Add...
- Select lib -> om2m-cep.jar and press OK
- Press CTRL + S to save MANIFEST.MF file

You successfuly added om2m-cep library to your project. Enjoj!

##Function list
```
public CepHttpServlet(CseService cse, Class eventClass)
public void run()
public void stopThread()
public void sendEvent(DataInterface data, String deviceName)
public void insertDevice(String deviceName)
public void addCepRule(String deviceName, String dataName, String rule)
public void editCepRule(String deviceName, String dataName, String newRule)
public void deleteCepRule(String deviceName, String dataName)
public ArrayList<CepRule> getAllCepRules()
public ArrayList<Device> getAllCepDevices()
public boolean isRunning()
public static void setDebug(boolean mode)
public static boolean isDebug()
```

##Using examples

- In [usage_example_gui.pdf](usage_example_gui.pdf) are step by step instructions how to use om2m-cep library in your project using GUI for adding rules.
- In [usage_example_programatical_solution.pdf](usage_example_programatical_solution.pdf) are step by step instructions how to use om2m-cep library programaticaly within your plugin.

##References

CEP library for OM2M uses:
- [Espertech](http://www.espertech.com/) library for creating and managing CEP. **Syntax for writing CEP rules in this library is the same as in Espertech library.**
- [H2 Database engine](http://www.h2database.com/html/main.html) library for saving CEP rules and devices into database.
- [Jetty](https://eclipse.org/jetty/) for web server implementation (web GUI for managing CEP rules).
