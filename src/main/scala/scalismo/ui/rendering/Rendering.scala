package scalismo.ui.rendering

import java.util.concurrent.TimeUnit

import vtk.vtkObjectBase

object Rendering {

  private var clients: List[Object] = Nil

  // This is a var so that the interval can be easily overridden. The unit is in seconds.
  var GarbageCollectorInterval: Int = 60

  /* This is *required*, otherwise you'll run out of memory sooner or later. The current rendering engine (VTK)
  *  uses native objects which have their own life cycles and must be periodically garbage-collected.
  *  It should be safe (but pointless) to call this method multiple times.
  */
  def register(client: Object): Unit = {
    if (!clients.contains(client)) {
      clients = clients ++ List(client)
    }
    if (clients.length == 1) {
      // first client, start garbage collection
      val gc = vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector
      gc.SetScheduleTime(GarbageCollectorInterval, TimeUnit.SECONDS)
      gc.Start()
    }
  }

  def unregister(client: Object): Unit = {
    clients = clients.filterNot(_ == client)
    if (clients.isEmpty) {
      // all clients gone, stop garbage collection.
      // This is necessary, otherwise the program will simply keep running.
      vtkObjectBase.JAVA_OBJECT_MANAGER.getAutoGarbageCollector.Stop()
    }
  }

}
