package example4

import java.util.concurrent.{Callable, ExecutorService, Executors, Future}

object CallableTest extends App {

  // フィボナッチ数を計算して返す関数
  def fibonacci(n: Long): BigDecimal =
    (1L to n).map(BigDecimal(_)).foldLeft(BigDecimal(1))(_ * _)

  // 結果を格納するためのケースクラス
  case class Result(in: Long, out: BigDecimal)

  // ExecutorService#newFixedThreadPoolでスレッドプールを作成する
  val executorService: ExecutorService = Executors.newFixedThreadPool(3)

  val futures: Seq[Future[Result]] = for (i <- 2 to 1000) yield {
    val c = new Callable[Result] {
      override def call(): Result = Result(i, fibonacci(i))
    }
    // タスクの実行を依頼する
    executorService.submit(c)
  }

  futures.foreach { future =>
    // タスクの結果を取得する
    val result: Result = future.get()
    // タスクの結果を標準出力に表示する
    printf("fac(%d) = %s\n", result.in, result.out)
  }

  // ExecutorServiceを終了する
  executorService.shutdown()
}
