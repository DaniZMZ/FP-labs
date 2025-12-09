import scala.concurrent.{Await, Future, ExecutionContext}
import scala.concurrent.duration._
/*
Посчитать уникальные элементы массива
```scala
def countUnique[T](data:Seq[Double], threadsNumber:Int):Int
```
*/
object Main {
  def countUniqueSimple(data: Seq[Double], threadsNumber: Int): Int = {
    implicit val ec: ExecutionContext = ExecutionContext.global

    val sectionSize = math.max(1, data.length / threadsNumber)
    val section = data.grouped(sectionSize).toVector

    val futureResults = Future.traverse(section) { section =>
      Future {
        section.toSet
      }
    }

    val sectionSets = Await.result(futureResults, 1.minute)
    sectionSets.reduce(_ ++ _).size
  }

  def main(args: Array[String]): Unit = {
    println(countUniqueSimple(Seq(1.0, 2.0, 3.0, 4.0, 5.0, 2.0, 3.0),3))
  }
}
