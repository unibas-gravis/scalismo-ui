package scalismo.ui.rendering.actor

import vtk.{vtkActor, vtkLookupTable, vtkPolyDataMapper}

class PolyDataActor extends vtkActor {
  val mapper: vtkPolyDataMapper = new vtkPolyDataMapper

  // to set a Blue to Red Color map
  val lut = new vtkLookupTable()
  lut.SetHueRange(0.667, 0.0)
  lut.SetNumberOfColors(256)
  lut.Build()
  mapper.SetLookupTable(lut)

  SetMapper(mapper)
  GetProperty().SetInterpolationToGouraud()

}
