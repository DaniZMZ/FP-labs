@main def run(): Unit = {
  val config = DeliveryConfig(
    rates = Map(
      "Box" -> 5.0,
      "Bigger box" -> 8.0,
      "The biggest box" -> 12.0
    ),
    maxWeight = 50.0,
    packagingRule = w => w * 1.1, // доп вес за упаковку
    freeShipping = 100.0
  )

  val fullInventory: Inventory = Inventory(Map("item1" -> 10, "item2" -> 5, "item3" -> 2))
  val lowInventory: Inventory = Inventory(Map("item1" -> 0, "item2" -> 5))

  val normalOrder: Order = Order(List(Item("item1", 2.0, 30.0), Item("item2", 1.5, 20.0)))
  val heavyOrder: Order = Order(List(Item("item3", 40.0, 10.0)))
  val expensiveOrder: Order = Order(List(Item("item1", 1.0, 120.0)))

  val all: IO[Unit] = for {
    _ <- IO(println("Тест 1: товара хватает"))
    _ <- program(normalOrder, config, WarehouseState(fullInventory, Nil, Nil))
    _ <- IO(println("\nТест 2: товара не хватает"))
    _ <- program(normalOrder, config, WarehouseState(lowInventory, Nil, Nil))
    _ <- IO(println("\nТест 3: тяжёлый заказ"))
    _ <- program(heavyOrder, config, WarehouseState(fullInventory, Nil, Nil))
    _ <- IO(println("\nТест 4: дорогой заказ"))
    _ <- program(expensiveOrder, config, WarehouseState(fullInventory, Nil, Nil))
  } yield ()
  all.unsafeRun()
}