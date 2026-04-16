def program(order: Order, config: DeliveryConfig, initialState: WarehouseState): IO[Unit] = {

  val canAssembleOrder = canAssemble(order, initialState.inventory)

  if (!canAssembleOrder) {
    IO(println("Невозможно собрать заказ: не хватает товаров на складе."))
  } else {
    val (stateAfterReserve, writerReserve) = reserveItems(order).run(initialState)
    val (logReserve, reserveResult) = writerReserve.run

    reserveResult match {
      case Left(err) =>
        IO {
          println(s"Ошибка резервирования: $err")
          println("Лог резервирования:")
          logReserve.foreach(println)
        }

      case Right(_) =>
        val (stateAfterPack, packResult) = packOrder(order).run(stateAfterReserve)
        packResult match {
          case Left(err) =>
            IO {
              println(s"Ошибка упаковки: $err")
              println("Лог:")
              logReserve.foreach(println)
            }

          case Right(_) =>
            val (finalState, shipResult) = shipOrder(order).run(stateAfterPack)
            shipResult match {
              case Left(err) =>
                IO {
                  println(s"Ошибка отправки: $err")
                  println("Лог:")
                  logReserve.foreach(println)
                }

              case Right(_) =>
                val totalWeight = order.items.map(_.weight).sum
                val totalPrice  = order.items.map(_.price).sum

                val pkgType = packageType(totalWeight).run(config)
                val shipping = shippingCost(totalWeight, totalPrice).run(config)
                val (logShip, shippingResult) = shipping.run
                shippingResult match {
                  case Left(err) =>
                    IO {
                      println(s"Ошибка упаковки: $err")
                      println("Лог:")
                      logShip.foreach(println)
                    }

                  case Right(_) =>
                    val isFree     = freeShipping(totalPrice).run(config)
                    val (logPkg, pkg) = pkgType.run

                    val allLogs = logReserve ++ logPkg ++ logShip

                    IO {
                      println("\n    Отчет    ")
                      println(s"Товары: ${order.items.map(i => s"${i.id} (${i.weight}кг, $$${i.price})").mkString(", ")}")
                      println(s"Общий вес: $totalWeight кг")
                      println(s"Общая стоимость: $$$totalPrice")
                      println(s"Тип упаковки: ${pkg.key}")
                      println(s"Стоимость доставки: ${shippingResult.map(_.toString).getOrElse("превышен макс. вес")}")
                      println(s"Бесплатная доставка: ${if (isFree) "Да" else "Нет"}")
                      println(s"\nЛоги выполнения:")
                      allLogs.foreach(println)
                      println(s"\nОстатки на складе: ${finalState.inventory.stock}")
                    }
                }

            }
        }
    }
  }
}