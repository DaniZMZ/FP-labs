/*
### Можно использовать
- стандартную библиотеку Scala;
- библиотеки вроде Cats, Cats Effect, ZIO, если вы сможете объяснить, как и что делает ваш код;
- даже если вы используете библиотечные монады, в блоке 0 вы должны реализовать свои варианты монад;
- трансформеры эффектов, если сможете объяснить суть их раюоты.

### Нельзя использовать
- императивный стиль;
- голые эффекты;

### Блок 0. Инфраструктура

Перед решением предметной части варианта необходимо реализовать:

- `trait Monad[M[_]]` с операциями `pure`, `flatMap`, `map`;
- наивный `Reader[Env, A]`;
- наивный `Writer[Log, A]`;
- наивный `State[S, A]`;
- наивный `IO[A]` с маленьким `unsafeRun`.

Технически удобно использовать лог вида `Vector[String]` или `List[String]`.

### Блок 1. Reader

Reader используется для в функциях, которые требуют чтения параметров из конфигурации.

### Блок 2. Writer

Writer используется вместо обычного лога. Можно не использовать его посеместно и заменить на IO, но в паре мест пожалуйста пусть будет.

### Блок 3. State

Если для чего-то хочется использовать глобальную переменную, вместо неё используем State.

### Блок 4. IO

Используем для взаимодействия с пользователем через консоль.

### Замечание по оформлению решения

Во всех вариантах желательно придерживаться одной и той же структуры проекта:

1. отдельно определения монад;
2. отдельно предметные типы и функции;
3. отдельно сценарий `IO`;
4. короткий `README` с инструкцией по запуску.

Цель задания — не просто реализовать набор функций, а показать, что вы умеете:

- выносить конфигурацию в `Reader`;
- делать вычисление объяснимым через `Writer`;
- моделировать изменение мира через `State`;
- отделять описание взаимодействия от исполнения через `IO`.
*/


/* ## Вариант 9. Склад заказов

Сюжет: склад резервирует товары, комплектует заказ и отправляет его.

### Reader

Из окружения:

- тарифы доставки;
- предельный вес посылки;
- тип упаковки по весу;
- правило бесплатной доставки от суммы.

Реализовать:

- `canAssemble(order, inventory)`;
- `packageType(weight)`;
- `shippingCost(weight, totalPrice)`;
- `freeShipping(totalPrice)`.

### Writer

Логировать:

- резервирование товаров;
- расчёт веса;
- выбор упаковки;
- стоимость доставки.

### State

Состояние:

- остатки товаров;
- собранные заказы;
- отправленные заказы.

Переходы:

- `receiveShipment`;
- `reserveItems`;
- `packOrder`;
- `shipOrder`.

### IO

Сценарий:

- ввести заказ;
- собрать его;
- вывести отчёт по упаковке и доставке.

### Обязательные тесты

| Кейс | Ожидание |
|---|---|
| Товара хватает | заказ собирается |
| Товара не хватает | отказ |
| Тяжёлый заказ | выбирается крупная упаковка |
| Дорогой заказ | доставка бесплатна |

---
*/

trait Monad [M[_]] {
  def pure[A](a: A): M[A]
  def flatMap[A, B](ma: M[A])(f: A => M[B]): M[B]
  def map[A, B](ma: M[A])(f:A => B):M[B] = flatMap(ma)(a => pure(f(a)))
}

case class Reader[Env, A](run: Env => A) {
  def map[B](f: A=> B): Reader[Env, B] =
    Reader(e => f(run(e)))
  /*
  * val a: A = run(e)
  * val b: B = f(a)
  * b
  */
  def flatMap[B](f: A => Reader[Env, B]): Reader[Env, B] =
    Reader(e => f(run(e)).run(e))
  /*
  * val a: A = run(e)
  * val readerB: Reader[E, B] = f(a)
  * val b: B = readerB.run(e)
  */
}

object Reader {
  def pure[Env, A](a: A): Reader[Env, A] = Reader(_ => a)
}

case class Writer[A](run: (Vector[String], A)) {
  def map[B](f: A=> B): Writer[B] =
    Writer((run._1, f(run._2)))

  def flatMap[B](f: A => Writer[B]): Writer[B] = {
    val (log1, a) = run
    val (log2, b) = f(a).run
    Writer((log1 ++ log2, b))
  }
}

case class State[S, A](run: S => (S,A)) {
  def map[B](f: A => B): State[S, B] =
    State { s =>
      val (s1, a) = run(s)
      (s1, f(a))
    }
  def flatMap[B](f: A => State[S, B]): State[S,B] =
    State { s =>
      val (s1, a) = run(s)
      f(a).run(s1)
    }
}

object State {
  def pure[S, A](a: A): State[S, A] = State(s => (s, a))
  def get[S]: State[S, S] = State(s => (s, s))
  def set[S](s: S): State[S, Unit] = State(_ => (s, ()))
  def modify[S](f: S => S): State[S, Unit] = State(s => (f(s), ()))
}

class IO[A](private val thunk: () => A) {
  def unsafeRun(): A = thunk()
  def map[B](f: A => B):IO[B] = new IO(()=>f(thunk()))
  def flatMap[B](f: A => IO[B]): IO[B] = new IO(()=> f(thunk()).unsafeRun())
}

object IO {
  def apply[A](a: => A): IO[A] = new IO(() => a)
}
