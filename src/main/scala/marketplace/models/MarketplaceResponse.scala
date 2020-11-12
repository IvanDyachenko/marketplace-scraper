package marketplace.models

sealed trait MarketplaceResponse

case class DummyMarketplaceResponse(dummy: "you") extends MarketplaceResponse
