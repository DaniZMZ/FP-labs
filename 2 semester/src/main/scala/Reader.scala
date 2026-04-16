def readRates: Reader[DeliveryConfig, Map[String, Double]] =
  Reader(config => config.rates)

def readMaxWeight: Reader[DeliveryConfig, Double] =
  Reader(config => config.maxWeight)

def readPackagingRules: Reader[DeliveryConfig, Double => Double] =
  Reader(config => config.packagingRule)

def readFreeShipping: Reader[DeliveryConfig, Double] =
  Reader(config => config.freeShipping)

enum PackageType(val key: String):
  case Box extends PackageType("Box")
  case BiggerBox      extends PackageType("Bigger box")
  case TheBiggestBox   extends PackageType("The biggest box")

def canAssemble(order: Order, inventory: Inventory): Boolean =
  order.items.forall {item =>
    inventory.stock.getOrElse(item.id, 0) >= 1
  }

def packageType(weight: Double): Reader[DeliveryConfig, Writer[PackageType]] =
  readPackagingRules.map { rule =>
    val pkg = rule(weight) match {
      case w if w < 1.0  => PackageType.Box
      case w if w < 10.0 => PackageType.BiggerBox
      case _             => PackageType.TheBiggestBox
    }
    val log = Vector(s"Вес $weight -> упаковка ${pkg.toString}")
    Writer((log, pkg))
  }

//Нафига тут итоговая цена как параметр в функции для подсчета стоимости заказа я хз
//Пускай будет, мне не жалко
def shippingCost(weight: Double, totalPrice: Double): Reader[DeliveryConfig, Writer[Either[String, Double]]] =
  for
    pkgWriter       <- packageType(weight)
    rates     <- readRates
    maxWeight <- readMaxWeight
  yield
    val (logPkg, pkg) = pkgWriter.run

    if weight > maxWeight then
      val errorMsg = s"Вес $weight превышает максимально допустимый $maxWeight"
      Writer((logPkg ++ Vector(errorMsg), Left(errorMsg)))
    else
      val rate = rates.getOrElse(pkg.key, Double.MaxValue)
      val cost = weight * rate
      Writer((logPkg ++ Vector(s"Стоимость доставки: $cost"), Right(cost)))

def freeShipping(totalPrice: Double): Reader[DeliveryConfig, Boolean] =
  readFreeShipping.map(limit =>
    totalPrice >= limit
  )