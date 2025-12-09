import Drink.priceCalculation
import DrinkValidator.*
import cats.effect.{ExitCode, IO, IOApp}
import scala.util.Try
import scala.io.StdIn

def readDrink(ll: LazyList[String]): Either[String, (Drink, LazyList[String])] = {
  ll match {
    case drinkTypeStr #:: producerStr #:: sugarStr #:: priceStr #:: tail =>
      val sugarTry = Try(sugarStr.toInt)
      val priceTry = Try(priceStr.toFloat)

      val result = for {
        sugar <- sugarTry.toEither.left.map(_ => s"Некорректный формат сахара: '$sugarStr'")
        price <- priceTry.toEither.left.map(_ => s"Некорректный формат цены: '$priceStr'")

        drinkTypeValid <- validateDrinkType(drinkTypeStr).toEither.left.map(_.toList.mkString(", "))
        producerValid <- validateProducer(producerStr).toEither.left.map(_.toList.mkString(", "))
        sugarValid <- validateSugar(sugar).toEither.left.map(_.toList.mkString(", "))
        priceValid <- validatePrice(price).toEither.left.map(_.toList.mkString(", "))

      } yield Drink(drinkTypeValid, producerValid, sugarValid, priceValid)

      result match {
        case Right(drink) => Right((drink, tail))
        case Left(error) => Left(error)
      }

    case _ =>
      Left("Недостаточно данных для создания Drink")
  }
}

def consoleInput: LazyList[String] = {
  def loop: LazyList[String] = {
    val line = StdIn.readLine()
    if (line == null) {
      LazyList.empty
    } else {
      line #:: loop
    }
  }
  loop
}

@annotation.tailrec
def processInput(remaining: LazyList[String], count: Int): Unit = {
  if (remaining.isEmpty) {
    println("\nВвод завершен")
  } else {
    readDrink(remaining) match {
      case Right((drink, tail)) =>
        println(s"Создан напиток\n" +
          s"Тип: ${drink.drinkType}\n" +
          s"Производитель: ${drink.producer}\n" +
          s"Сахар: ${drink.sugar}\n" +
          s"Начальная цена: ${drink.price}\n" +
          s"Итоговая цена: ${priceCalculation(drink).price}\n")
        println("Продолжить? (y/n): ")
        val continue = StdIn.readLine().toLowerCase()
        if (continue == "y" || continue == "да") {
          processInput(tail, count + 1)
        }

      case Left(error) =>
        println(s"Ошибка: $error")
        println("Попробуйте снова.")
        processInput(remaining.drop(4), count)
    }
  }
}

object Main {
  def main(args: Array[String]): Unit = {
    println("=== Ввод напитков ===")
    println("Введите данные для каждого напитка (по 4 строки):")
    println("1. Тип напитка (Coffee или Tea)")
    println("2. Производитель")
    println("3. Сахар (1-100)")
    println("4. Цена (10-5000)")
    println("-" * 50)
    val userInput = consoleInput
    processInput(userInput, 0)
  }
}