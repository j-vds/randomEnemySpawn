# RandomEnemySpawn

### Concept
This plugin is designed to spice up survival/attack mode. Each 2 minutes some daggers and/or flares will spawn on a random tile. So enemies can come from every direction. The amount increases linearly from now on. 

*But it still requires some balencing*

### Terminal commands
* `res_info` --> shows the timer and how many units will spawn next.
* `res [on/off]` --> this is a way to enable/disable the plugin completely.

### Commands
`res` --> shows an infomessage.

### Admin Only commands
*Most of them are for debugging purposes*
* `spawnenemy` --> this will spawn a dagger or flare on a random tile.
* `resdisable` --> this disables the RandomEnemySpawn until game over.

### Feedback
Open an issue if you have a suggestion or feedback.

### Releases
Prebuild relases can be found [here](https://github.com/J-VdS/randomEnemySpawn/releases).

### Server which isn't allowed to use the plugin
mindustry.ru

### info/help
You can always open an issue or contact me on discord: Fishbuilder#4502

### Building a Jar 

`gradlew jar` / `./gradlew jar`

Output jar should be in `build/libs`.


### Installing

Simply place the output jar from the step above in your server's `config/mods` directory and restart the server.
List your currently installed plugins by running the `plugins` command.

