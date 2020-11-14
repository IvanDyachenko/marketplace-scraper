package marketplace.models

import beru4s.models.{Response => BeruResponse}

sealed trait MarketplaceResponse {
  type A
  def response: A
}

final case class BeruMarketplaceResponse(response: BeruResponse) extends MarketplaceResponse { type A = BeruResponse }
