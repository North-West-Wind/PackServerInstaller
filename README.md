# PackServerInstaller
An automatic modded server installer for Forge and Fabric modpacks.  
This is designed for modpack developers to create a server pack easily.
## Usage
1. Download the .jar file from the [release page](https://github.com/North-West-Wind/PackServerInstaller/releases/latest) and put it in an empty directory
2. Create a file named `installer.json` in the directory.
3. Add these to the JSON file according to your needs:
```json5
{
	"url": "modpack direct download link",
	"skipMods": false, // Whether or not to skip mod downloads
	"skipServer": false, // Whether or not to skip server .jar install
	"autoDownload": true // Whether or not to do everything without user's interaction
}
```
4. (Optional) Create a file named `blacklist.json` in the directory.
5. (Optional) Add these to the blacklist JSON file according to your needs:
```json5
{
	"mods": [], // If you want to delete some mods on the server automatically when installing, put the IDs of the mods here
	"folders": [] // If you want to remove some files/folders from overrides, put their relative path here
}
```
6. (Optional) Create a batch/shell script to run the .jar file with `java -jar pack-server-installer.jar`

If any issue arise, please report at the [issues](https://github.com/North-West-Wind/PackServerInstaller/issues) page.
## License
GNU GPLv3