case class Item(id: String, weight: Double, price: Double)

case class Order(items: List[Item])

case class Inventory(stock: Map[String, Int])

case class WarehouseState(
  inventory: Inventory,            
  assembledOrders: List[Order],    
  shippedOrders: List[Order]      
)

case class DeliveryConfig(
  rates: Map[String, Double], // зависит от типа упаковки
  maxWeight: Double,
  packagingRule: Double => Double, // зависит от веса
  freeShipping: Double
)

