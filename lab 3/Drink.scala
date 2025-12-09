case class Drink (drinkType: String, producer: String, sugar: Int, price: Float)

object Drink {
  def priceCalculation(drink: Drink): Drink = {
    val currentSugar = drink.sugar
    val currentPrice = drink.price
    val newPrice = currentPrice + (currentSugar * 3)
    drink.copy(price = newPrice)
  }
}