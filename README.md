# om2m-cep
Complex Event Processing library for om2m - open source IOT platform

Implementation instructions:

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

- In usage_example_gui.pdf are step by step instructions how to use om2m-cep library in your project using GUI for adding rules.

