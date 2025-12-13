package old

/*





import cats.effect.{IO, IOApp}
import cats.syntax.all._

// 1. Модель данных (чистая)
enum Operation:
  case Add, Subtract, Multiply, Divide

case class CalculatorInput(
  a: Int,
  b: Int,
  operation: Operation
)

sealed trait CalculatorError
object CalculatorError:
  case object DivisionByZero extends CalculatorError
  case object InvalidOperation extends CalculatorError
  case class ParseError(message: String) extends CalculatorError

// 2. ЧИСТЫЕ ФУНКЦИИ (без IO!)

// Парсинг строки в операцию
def parseOperation(input: String): Either[CalculatorError, Operation] = 
  input.trim.toLowerCase match {
    case "+" | "add" => Right(Operation.Add)
    case "-" | "subtract" => Right(Operation.Subtract)
    case "*" | "multiply" => Right(Operation.Multiply)
    case "/" | "divide" => Right(Operation.Divide)
    case _ => Left(CalculatorError.InvalidOperation)
  }

// Парсинг числа с валидацией
def parseNumber(input: String): Either[CalculatorError, Int] =
  Either.catchOnly[NumberFormatException](input.toInt)
    .leftMap(_ => CalculatorError.ParseError(s"Невозможно преобразовать '$input' в число"))

// Основная логика калькулятора (чистая!)
def calculate(input: CalculatorInput): Either[CalculatorError, Int] = 
  input.operation match {
    case Operation.Add => Right(input.a + input.b)
    case Operation.Subtract => Right(input.a - input.b)
    case Operation.Multiply => Right(input.a * input.b)
    case Operation.Divide => 
      if (input.b == 0) Left(CalculatorError.DivisionByZero)
      else Right(input.a / input.b)
  }

// Форматирование результата (чистое!)
def formatResult(result: Either[CalculatorError, Int]): String = 
  result.fold(
    error => s"Ошибка: ${errorToString(error)}",
    value => s"Результат: $value"
  )

def errorToString(error: CalculatorError): String = error match {
  case CalculatorError.DivisionByZero => "Деление на ноль"
  case CalculatorError.InvalidOperation => "Неизвестная операция"
  case CalculatorError.ParseError(msg) => msg
}

// 3. ГРЯЗНЫЕ функции (с эффектами)

// Чтение с валидацией (эффектная)
def readNumber(prompt: String): IO[Int] = {
  def loop: IO[Int] = for {
    _ <- IO.print(s"$prompt: ")
    input <- IO.readLine
    result <- parseNumber(input) match {
      case Right(value) => IO.pure(value)
      case Left(error) => 
        IO.println(s"Ошибка: ${errorToString(error)}") >> loop
    }
  } yield result
  
  loop
}

// Чтение операции с валидацией (эффектная)
def readOperation: IO[Operation] = {
  def loop: IO[Operation] = for {
    _ <- IO.print("Операция (+, -, *, /): ")
    input <- IO.readLine
    result <- parseOperation(input) match {
      case Right(op) => IO.pure(op)
      case Left(error) => 
        IO.println(s"Ошибка: ${errorToString(error)}") >> loop
    }
  } yield result
  
  loop
}

// Главная эффектная программа
object GoodCalculator extends IOApp.Simple {
  def run: IO[Unit] = for {
    a <- readNumber("Введите первое число")
    op <- readOperation
    b <- readNumber("Введите второе число")
    
    // Вызов чистой функции
    calculationResult = calculate(CalculatorInput(a, b, op))
    
    // Форматирование (чистая функция)
    output = formatResult(calculationResult)
    
    // Вывод (эффект)
    _ <- IO.println(output)
  } yield ()
}


















import cats.effect.IOApp

import scala.util.Try

sealed trait TeaType
case object BlackTea extends TeaType
case object GreenTea extends TeaType
case object HerbalTea extends TeaType
case object WhiteTea extends TeaType

case class Tea (producer: String, teaType: TeaType, sugar: Int, price: Float)

object TeaMenuApp extends IOApp {
  def displayMenu(): IO[Unit] = {
    IO.println(
    """Выберите тип чая:
      |1. Black Tea
      |2. Green Tea
      |3. Herbal Tea
      |4. White Tea
      |Введите номер: """.stripMargin
  )}

  def readChoice(): IO[Int] = {
    IO.readLine.map(_.trim).flatMap { input =>
      IO.fromTry(Try(input.toInt))
        .handleErrorWith(_ =>
          IO.println("Ошибка: введите число") *> readChoice()
        )
    }
  }

  def validateChoice(choice: Int): IO[TeaType] = choice match {
    case 1 => IO.pure(BlackTea)
    case 2 => IO.pure(GreenTea)
    case 3 => IO.pure(HerbalTea)
    case 4 => IO.pure(WhiteTea)
    case _ =>
      IO.println(s"Ошибка: выберите число от 1 до 4, а не $choice") *>
        selectTeaType()
  }

  def selectTeaType(): IO[TeaType] = for {
    _ <- displayMenu()
    choice <- readChoice()
    teaType <- validateChoice(choice)
  } yield teaType

  override def run(args: List[String]): IO[ExitCode] = {
    selectTeaType().flatMap { teaType =>
      IO.println(s"Вы выбрали: $teaType")
    }.as(ExitCode.Success)
  }
}*/
