import cats.data.{Validated, ValidatedNel}
import cats.implicits._

sealed trait ValidationError

object ValidationError {
  case object InvalidDrinkType extends ValidationError
  case object EmptyProducer extends ValidationError
  case class InvalidSugar(min: Int, max: Int) extends ValidationError
  case class InvalidPrice(min: Float, max: Float) extends ValidationError
}


object DrinkValidator {

  val MIN_SUGAR = 1
  val MAX_SUGAR = 100
  val MIN_PRICE = 1.0f
  val MAX_PRICE = 5000.0f

  type ValidationResult[A] = ValidatedNel[ValidationError, A]
  
  def validateDrinkType(drinkType: String): ValidationResult[String] = {
    drinkType.trim match {
      case "Coffee" => drinkType.validNel
      case "Tea" => drinkType.validNel
      case _ => ValidationError.InvalidDrinkType.invalidNel
    }
  }

  def validateProducer(producer: String): ValidationResult[String] = {
    producer.trim match {
      case "" => ValidationError.EmptyProducer.invalidNel
      case s => s.validNel
    }
  }

  def validateSugar(sugar: Int): ValidationResult[Int] = {
    if (sugar < MIN_SUGAR || sugar > MAX_SUGAR)
      ValidationError.InvalidSugar(MIN_SUGAR, MAX_SUGAR).invalidNel
    else sugar.validNel
  }

  def validatePrice(price: Float): ValidationResult[Float] = {
    if (price < MIN_PRICE || price > MAX_PRICE)
      ValidationError.InvalidPrice(MIN_PRICE, MAX_PRICE).invalidNel
    else price.validNel
  }
  
  def validateDrink(drinkType: String, producer: String, sugar: Int, price: Float): ValidationResult[Drink] = {
    (validateDrinkType(drinkType),
      validateProducer(producer),
      validateSugar(sugar),
      validatePrice(price)).mapN(Drink.apply)
  }
}
