package marketplace.models

sealed trait MarketplaceRequest

case class DummyMarketplaceRequest(dummy: "you") extends MarketplaceRequest
