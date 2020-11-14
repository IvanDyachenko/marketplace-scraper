package marketplace.models

import beru4s.models.{Request => BeruRequest}

sealed trait MarketplaceRequest {
  type A
  def request: A
}

final case class BeruMarketplaceRequest(request: BeruRequest) extends MarketplaceRequest { type A = BeruRequest }
