def receiveShipment(shipment: Map[String, Int]): State[WarehouseState, Unit] = {
  State { state =>
    val newStock = shipment.foldLeft(state.inventory.stock) { case (stock, (id, qty)) =>
      stock.updated(id, stock.getOrElse(id, 0) + qty)
    }
    val newState = state.copy(inventory = state.inventory.copy(stock = newStock))
    (newState, ())
  }
}

def packOrder(order: Order): State[WarehouseState, Either[String, Unit]] =
  State { state =>
    val newState = state.copy(assembledOrders = order :: state.assembledOrders)
    (newState, Right(()))
  }

def shipOrder(order: Order): State[WarehouseState, Either[String, Unit]] =
  State { state =>
    state.assembledOrders.find(_ == order) match
      case None => (state, Left("Заказ не найден в собранных"))
      case Some(_) =>
        val newAssembled = state.assembledOrders.filterNot(_ == order)
        val newState = state.copy(
          assembledOrders = newAssembled,
          shippedOrders = order :: state.shippedOrders
        )
        (newState, Right(()))
  }
  
def reserveItems(order: Order): State[WarehouseState, Writer[Either[String, Unit]]] =
  State { state =>
    val items = order.items
    val canReserve = items.forall(item => state.inventory.stock.getOrElse(item.id, 0) >= 1)

    if !canReserve then
      val log = Vector(s"Недостаточно товаров: ${items.map(_.id).mkString(", ")}")
      (state, Writer((log, Left("Недостаточно товаров"))))
    else
      val newStock = items.foldLeft(state.inventory.stock) { (stock, item) =>
        stock.updated(item.id, stock(item.id) - 1)
      }
      val newState = state.copy(inventory = state.inventory.copy(stock = newStock))
      val log = Vector(s"Зарезервированы: ${items.map(_.id).mkString(", ")}")
      (newState, Writer((log, Right(()))))
  }