package scalanlp.collection.mutable;

import scala.collection.mutable.PriorityQueue;

class Beam[T](val maxSize:Int, xs:T*)(implicit o : T=>Ordered[T]) { outer =>
  assert(maxSize >= 0)
  val queue = new PriorityQueue[T]()(reverseOrder(o));
  queue ++= xs;

  def size = queue.size;

  def map[U<%Ordered[U]](f: T=>U) = new Beam[U](maxSize)  {
    queue ++= outer.queue.map(f);
  }

  def flatMap[U](f: T=>Collection[U])(implicit oU: U =>Ordered[U]) = new Beam[U](maxSize) {
      for(x <- outer.queue;
          y <- f(x)) {
        if(queue.size < maxSize) {
          queue += y;
        } else if (queue.max < y) { // q.max is the smallest element.
          queue.dequeue();
          queue += y;
        }
      }
  }


  def filter[U](f: T=>Boolean) = new Beam[T](maxSize)  {
    queue ++= outer.queue.filter(f);
  }


  def +(x:T) = {this += x; this}
  def ++(x:Iterator[T]) = {this ++= x; this};
  def ++(x:Iterable[T]) = {this ++= x; this};

  def +=(x:T) = { cat(queue,x); }
  def ++=(x:Iterator[T]) = { x.foreach{cat(queue,_)}}
  def ++=(x:Iterable[T]) = { x.foreach(cat(queue,_))}

  private def trim() {
    while(queue.size > maxSize) {
      queue.dequeue();
    }
  }

  private def cat(h : PriorityQueue[T], x : T) {
    if(h.size < maxSize) h += x;
    else if (h.max < x) {h.dequeue(); h += x;}
  }


  private def reverseOrder[U](o : U=>Ordered[U]) = {x : U =>
    val oReal = o(x);
    new Ordered[U] {
      def compare(x2 : U) = -oReal.compare(x2);
    }
  }

  def elements = queue.elements;
  override def toString() = elements.mkString("Beam(",",",")");

}

object Beam {
  def apply[T<%Ordered[T]](maxSize:Int, xs:T*) = new Beam[T](maxSize, xs:_*);
}
