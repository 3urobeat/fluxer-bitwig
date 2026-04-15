<div align="center">
    <h1>fluxer-bitwig</h1>
    <p align="center"><img width=80% src="assets/banner.png"></p>
    <h4>💻 Update your Fluxer status with your Bitwig activity!</h4>
    <div>
        <a href="#features">Features</a> •
        <a href="#installing">Installing</a> •
        <a href="#settings">Extension Settings</a>
    </div>
</div> 

&nbsp;

<a id="features"></a>

## Features
- Automatically syncs your current Bitwig Studio activity to your Fluxer status
- Displays your current project name and whether you are arranging, mixing or editing
- Customizable status format with configurable app name, activity text and behavior, see [Extension Settings](#settings)

> [!NOTE]
> Fluxer does not yet support Rich Presence (RPC), so this extension uses the Custom Status feature to show your activity.  
> This requires the extension to use your user token to authenticate the API requests.  
> As soon as Fluxer adds support for RPC, this extension will be updated to utilize it.

&nbsp;

### Limitations
Bitwig saves Controller/Extension settings on project level, not on application level.  
This means that at the moment you have to reconfigure your Fluxer token for every project.  
Due to this limitation the extension will remain in pre-relase until either Fluxer supports native RPC or I implement a filesystem read/write mechanism.

> [!WARNING]
> This also means that your token will be saved in the Bitwig Project File!

&nbsp;

<a id="installing"></a>

## Installing
### Download
Download the `.bwextension` file from the Assets section of the latest [release](https://github.com/3urobeat/fluxer-bitwig/releases/latest).  
Open Bitwig and simply drag the downloaded file into Bitwig.

If that doesn't work for you, manually copy the file into your Extensions folder, which should be located at:
- Windows: `%USERPROFILE%\Documents\Bitwig Studio\Extensions\`
- Mac: `~/Documents/Bitwig Studio/Extensions/`
- Linux: `~/Bitwig Studio/Extensions/`

&nbsp;

<details>
<summary><strong>Building from source & installing for development instead</strong> (Click to unfold)</summary>

Make sure you have `openjdk-17` and `maven` installed and that your shell environment points to the Java 17 installation (you'll otherwise get `error: invalid target release: 17`).  
Clone this repository and `cd` into it.
```bash
# To compile, run:
mvn install
# Alternatively you can run 'mvn clean' to clean or 'mvn clean install' to do a clean build.
# Tip: To only resolve dependencies but not build, run 'mvn dependency:resolve'.

# Link the compiled extension once. In the future you only need to restart Bitwig to reload the extension
ln -s ./target/fluxerbitwig.bwextension ~/Bitwig\ Studio/Extensions
```
In Bitwig, open Settings > Shortcuts, search for 'console' and bind the plugin console to a key, for example <kbd>CTRL</kbd>+<kbd>Shift</kbd>+<kbd>J</kbd>.  
Press that key combination and select 'fluxer-bitwig' in the popup window to see all messages the plugin logs through the API to the host.

</details>

&nbsp;

In Bitwig, open Settings > Controllers and press '+ Add Controller'.  
From the 'Hardware Vendor' dropdown select '3urobeat' and then 'Fluxer Bitwig'.  
Press Add. A new controller (🎹 keyboard) icon will appear in the top right corner of Bitwig. 

&nbsp;

### Configuration
You need to provide your Fluxer user token so the extension can make the API requests to update your account's status.  

To retrieve your user token:
1. Log in to Fluxer at [https://web.fluxer.app](https://web.fluxer.app) or your desktop app
2. Open your Developer Tools by pressing <kbd>F12</kbd> (browser) or <kbd>Ctrl</kbd>+<kbd>Shift</kbd>+<kbd>I</kbd> (desktop app)
3. Go to the `Application` Tab at the top
4. Under `Storage` on the left, unfold `Local storage` and `https://web.fluxer.app`
5. Find the entry `token` in the table on the right and copy its value `flx_abcdefg...`
6. Back in Bitwig, paste the token into the `Fluxer: Token` field.
7. The extension will automatically reload. Open a project and take a look at your Fluxer status!

&nbsp;

> [!WARNING]
> User tokens are sensitive information and provide access to your account. They must not be shared with anyone.  
> Your configured token is stored in your project file.  
> Make sure you don't blindly share your Project File with someone else.  
> Should your token ever get leaked, update your Fluxer password to invalidate old tokens/sessions.

&nbsp;

<a id="settings"></a>

## Extension Settings
Controller/Extension Settings can be accessed from the controller popout in the top right of Bitwig (🎹 keyboard icon).  
The extension exposes the following settings:

| Setting | Default | Description |
|---------|---------|-------------|
| `Enable` | On | Controls whether extension is enabled |
| `Fluxer Token` |  | User Token to authenticate with Fluxer. This is sensitive and should not be shared |
| `Status Format` | "🎹 {appName} {activityText}" | Controls formatting of status text. Variables: {appName}, {activityText} |
| `Status Activity Text` | "- {activity}" | Controls formatting of activity text to show in status text. Variable {activity} will be replaced by your current project name |
| `Status App Name` | "Bitwig Studio" | Application name to show in status text |
| `Show Idle` | On | If disabled, extension will clear {activityText} instead of showing 'Idling' when no project is loaded |
| `Clear Idle After` | 0 | Time in ms of being idle after which your status should be cleared. Set to 0 to never clear |
