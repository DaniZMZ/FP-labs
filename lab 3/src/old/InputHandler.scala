/*package old

import cats.data.{NonEmptyList, Validated, ValidatedNel}
import cats.effect.std.Console
import cats.effect.{IO, IOApp}
import cats.syntax.all.*

import scala.util.Try

case class Field[A](name: String, prompt: String, parser: String => Option[A], validator: A => ValidationResult[A])

def readField[A](field: Field[A]): IO[A] = {

  def attempt(): IO[A] = {
    for {
      _ <- Console[IO].print(s"${field.prompt}: ")
      input <- Console[IO].readLine
      result <- field.parser(input) match {
        case Some(value) =>
          field.validator(value).fold(
            errors => showErrors(errors) >> attempt(),
            valid => IO.pure(valid)
          )
        case None =>
          IO.println(s"Неверный формат для ${field.name}") >> attempt()
      }
    } yield result
  }
  attempt()
}

def showErrors(errors: NonEmptyList[ValidationError]): IO[Unit] = {
  val messages = errors.toList.map {
    case ValidationError.InvalidDrinkType =>
      "Тип напитка должен быть Coffee или Tea"
    case ValidationError.EmptyProducer =>
      "Производитель не может быть пустым"
    case ValidationError.InvalidSugar(min, max) =>
      s"Сахар должен быть от $min до $max мг/100мл"
    case ValidationError.InvalidPrice(min, max) =>
      f"Цена должна быть от $min%.2f до $max%.2f руб."
  }
  IO.println(messages.mkString("Ошибки:\n  • ", "\n  • ", ""))
}*/