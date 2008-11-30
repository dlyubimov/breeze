package scalanlp.collection.immutable;

import scala.collection.mutable.PriorityQueue;

class Beam[T](val maxSize:Int, xs : T*)(implicit o : T=>Ordered[T]) { outer =>
  assert(maxSize >= 0)
  val heap = trim(BinomialHeap(xs:_*));

  def size = heap.size;

  private def trim(h2 : BinomialHeap[T]) = {
    var h = h2;
    while(h.size > maxSize) {
      h = h.delMin; 
    }
    h
  }

  def map[U<%Ordered[U]](f: T=>U) = new Beam[U](maxSize) { 
    override val heap = BinomialHeap[U]() ++ outer.heap.map(f);
  }

  def flatMap[U](f: T=>Collection[U])(implicit oU: U =>Ordered[U]) = new Beam[U](maxSize) {
    override val heap = {
      val queue = new PriorityQueue[U]()(reverseOrder(oU));
      for(x <- outer.heap;
          y <- f(x)) {
        if(queue.size < maxSize) {
          queue += y;
        } else if (queue.max < y) { // q.max is the smallest element.
          queue.dequeue();
          queue += y;
        }
      }
      BinomialHeap[U]() ++ queue;
    }
  }
  
  def filter(f : T=>Boolean) = new Beam[T](maxSize) {
    override val heap = BinomialHeap[T]() ++ outer.heap.filter(f);
  }

  private def reverseOrder[U](o : U=>Ordered[U]) = {x : U =>
    val oReal = o(x);
    new Ordered[U] {
      def compare(x2 : U) = -oReal.compare(x2);
    }
  }

  def elements = heap.elements;
  override def toString() = elements.mkString("Beam(",",",")");
}