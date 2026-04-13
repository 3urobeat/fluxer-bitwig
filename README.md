# fluxer-bitwig
💻 Update your Fluxer status with your Bitwig activity!

## Features
TODO

## Installing
TODO

In Bitwig, open Settings > Controllers and press '+ Add Controller'.  
From the 'Hardware Vendor' dropdown select '3urobeat' and then 'fluxer-bitwig'.  
Press Add. A new controller (keyboard) icon will appear in the top right corner of Bitwig. 

<details>
<summary><strong>Compiling & Installing for development</strong> (Click to unfold)</summary>

Make sure you have `openjdk-17` and `maven` installed and that your shell environment points to the Java 17 installation (you'll otherwise get `error: invalid target release: 17`).
```bash
# To compile, run:
mvn install
# Alternatively you can run 'mvn clean' to clean or 'mvn clean install' to do a clean build.
# Tip: To only resolve dependencies but not build, run 'mvn dependency:resolve'.

# Link the compiled extension once. In the future you only need to restart Bitwig to reload the extension
ln -s ~/Dokumente/Code-Projekte/fluxer-bitwig/target/fluxerbitwig.bwextension ~/Bitwig\ Studio/Extensions
```
In Bitwig, open Settings > Shortcuts, search for 'console' and bind the plugin console to a key, for example <kbd>CTRL</kbd>+<kbd>Shift</kbd>+<kbd>J</kbd>.  
Press that key combination and select 'fluxer-bitwig' in the popup window to see all messages the plugin logs through the API to the host.

</details>

## Extension Settings
TODO
