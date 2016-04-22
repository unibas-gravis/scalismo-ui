### Code formatting ###

To ensure consistent formatting across all users and commits, please do the following before every commit:

1. In Idea, right-click on the project, and select "Reformat Code". Make sure "Optimize imports" is selected, and "Rearrange entries" is not selected.
2. Once the Idea reformatting is done, run ``` sbt clean compile test ``` on the console.
3. Commit.

### Publishing instructions ###


1. ``` sbt +assembly ```
2. ``` scp target/scala-2.11/statismo-ui.jar statismo@statismo.cs.unibas.ch:/var/www/statismo.cs.unibas.ch/viewer/[versionnumber]/  ```
3. Edit the [Statismo Wiki](https://github.com/statismo/statismo/wiki/Statismo%20Viewer) accordingly.