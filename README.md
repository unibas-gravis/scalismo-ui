### Publishing instructions ###


1. ``` sbt +assembly ```
2. ``` scp target/scala-2.11/statismo-ui.jar statismo@statismo.cs.unibas.ch:/var/www/statismo.cs.unibas.ch/viewer/[versionnumber]/  ```
3. Edit the [Statismo Wiki](https://github.com/statismo/statismo/wiki/Statismo%20Viewer) accordingly.