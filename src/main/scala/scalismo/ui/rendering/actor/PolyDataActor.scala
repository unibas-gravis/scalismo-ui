/*
 * Copyright (C) 2016  University of Basel, Graphics and Vision Research Group 
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package scalismo.ui.rendering.actor

import vtk._

trait DataActor extends vtkActor {
  val mapper: vtkMapper
}

class PolyDataActor extends DataActor {
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

class UnstructuredGridActor extends DataActor {
  val mapper: vtkDataSetMapper = new vtk.vtkDataSetMapper()

  SetMapper(mapper)
  GetProperty().SetInterpolationToGouraud()
}

