package it.agilelab.gis.domain.graphhopper

import com.graphhopper.routing.util.FlagEncoder
import com.graphhopper.routing.weighting.Weighting
import com.graphhopper.util.EdgeIteratorState
import com.graphhopper.util.details.{ AbstractPathDetailsBuilder, PathDetailsBuilder, PathDetailsBuilderFactory }

import java.util

/** Custom path details holder.
  * @param distance distance of the edge
  * @param baseNode base node of the edge
  * @param adjNode adjacent node of the edge
  * @param edgeId edge id
  * @param edge edge state
  * @param typeOfRoute type of route
  */
case class CustomPathDetails(
    distance: Double,
    baseNode: Int,
    adjNode: Int,
    edgeId: Int,
    edge: Option[EdgeIteratorState],
    typeOfRoute: Option[String]
)

/** A factory for [[CustomPathDetails]].
  * @param typeOfRouteFunc function to extract the type of route from the edge state.
  */
class CustomPathDetailsBuilderFactory(typeOfRouteFunc: EdgeIteratorState => Option[String])
    extends PathDetailsBuilderFactory {

  override def createPathDetailsBuilders(
      requestedPathDetails: util.List[String],
      encoder: FlagEncoder,
      weighting: Weighting
  ): util.List[PathDetailsBuilder] = {
    val builders = new util.ArrayList[PathDetailsBuilder]
    builders.add(new CustomPathDetails)
    builders
  }

  private class CustomPathDetails extends AbstractPathDetailsBuilder("details") {

    private var customDetails = CustomPathDetails(-1d, -1, -1, -1, None, None)

    override def getCurrentValue: AnyRef = customDetails

    override def isEdgeDifferentToLastEdge(edge: EdgeIteratorState): Boolean = {
      val thisEdgeId = edge.getEdge
      if (thisEdgeId != customDetails.edgeId) {
        customDetails = CustomPathDetails(
          distance = edge.getDistance,
          baseNode = edge.getBaseNode,
          adjNode = edge.getAdjNode,
          edgeId = thisEdgeId,
          edge = Some(edge),
          typeOfRoute = typeOfRouteFunc(edge)
        )
        return true
      }
      false
    }
  }
}
