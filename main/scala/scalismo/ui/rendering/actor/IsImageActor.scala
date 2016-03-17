package scalismo.ui.rendering.actor

import vtk.vtkActor

/**
 * This trait is only used as a flag to indicate that this actor is rendering images.
 * It is used to prioritize the order of adding actors to a renderer.
 *
 * See the [[scalismo.ui.rendering.RendererPanel]] implementation for more details.
 */
trait IsImageActor extends vtkActor {

}
