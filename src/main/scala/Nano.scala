object Nano {
  class Nano() extends scala.annotation.StaticAnnotation
  class Gateway() extends scala.annotation.StaticAnnotation

  private val empty = List[Int]()

  @Gateway
  def sort(list: List[Int]): List[Int] = qsort(list)

  @Nano
  def qsort(list: List[Int]): List[Int] =
    if (list == Nil)
      empty
    else {
      val head = list.head
      val tail = list.tail
      val left = filter(tail, i => i < head)
      val right = filter(tail, i => i >= head)
      sort(left) ++ (head :: sort(right))
    }

  @Nano
  def filter(list: List[Int], predicate: Int => Boolean): List[Int] =
    if (list == Nil)
      empty
    else {
      val head = list.head
      val tail = list.tail

      if (predicate(head))
        head :: filter(tail, predicate)
      else
        filter(tail, predicate)
    }

  def main(args: Array[String]): Unit = {
    System.out.println(sort(List(3,5,4,1,2)))
  }

}
