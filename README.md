# Scalismo-ui

Scalismo-ui is a library for visualizing statistical shape models and related 3D objects, such as 3D meshes or 3D medical images. It is designed to complement the [Scalismo](https://github.com/unibas-gravis/scalismo) framework and to make it easy to build simple, graphical shape modelling applications and to allow for "visual debugging" of complex image analysis algorithms.
Further, it can be used as a standalone application for visualizing shape models, 3D meshes and 3D medical images.

Scalismo-ui was developed and is currently maintained by the [Graphics and Vision Research Group](http://gravis.cs.unibas.ch) of the [University of Basel](http://www.unibas.ch).


# Usage

### Scalismo-ui as a library

Add the following dependency to your `build.sbt`:

```scala
libraryDependencies ++= Seq("ch.unibas.cs.gravis" %% "scalismo-ui" % "0.11.0-RC1",
                            "ch.unibas.cs.gravis" % "scalismo-native-all" % "4.0.0")

resolvers += Resolver.jcenterRepo
```

### Standalone application
You can also use a scalismo-ui as a standalone application. Simply call
```
sbt run
```
to start the application.


# Documentation

### Quickstart

Coming soon.

### Mailing List

For questions regarding scalismo-ui or suggestions for improvements, please post to the Mailing List [scalismo google group](https://groups.google.com/forum/#!forum/scalismo).


# Maintainers


- University of Basel, Graphics and Vision research: [@unibas-gravis](https://github.com/unibas-gravis), [homepage](http://gravis.cs.unibas.ch)

- Marcel Luethi, [@marcelluethi](https://github.com/marcelluethi)
- Ghazi Bouabene, [@marcelluethi](https://github.com/ghazi-bouabene)


# Contributions

Contributions to scalismo-ui are very welceom. Please send us a pull request from your fork of this repository!
We do have to ask you to sign the [contributors agreement](contributors-agreement.pdf) before we can merge any of your work into our code base.

# Copyright and Licenses

All code is released under the GNU public license, available [here](https://opensource.org/licenses/GPL-3.0).

Copyright, University of Basel, 2017.
