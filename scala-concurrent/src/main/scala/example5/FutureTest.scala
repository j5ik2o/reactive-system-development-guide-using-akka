package example5

import java.util.concurrent.{ExecutorService, Executors}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}

object FutureTest extends App {

  // フィボナッチ数を計算して返す関数
  def fibonacci(n: Long): BigDecimal =
    (1L to n).map(BigDecimal(_)).foldLeft(BigDecimal(1))(_ * _)

  // 結果を格納するためのケースクラス
  case class Result(in: Long, out: BigDecimal)

  // ExecutorService#newFixedThreadPoolでスレッドプールを作成する
  val executorService: ExecutorService = Executors.newFixedThreadPool(3)
  // (1)
  // ExecutorServiceをExecutionContextとして定義する
  implicit val ec: ExecutionContext =
    ExecutionContext.fromExecutorService(executorService)

  // (2)
  // Future#applyでタスクをExecutionContextに依頼する
  val futures: Seq[Future[Result]] = for (i <- 2 to 1000) yield {
    // タスクの実行を依頼する
    Future { // Future#apply
      Result(i, fibonacci(i))
    } // (ec) ← 第二引数にecが暗黙的に引き渡されます
  }

  // (3)
  // Future#sequenceによって Seq[Future[Result]] → Future[Seq[Result]] に変換する
  // Await#resultによって Future[Seq[Result]] → Seq[Result] に変換する
  val results: Seq[Result] =
    Await.result(Future.sequence(futures), 10 seconds)
  // Seq[Result]の要素を標準出力に表示する
  results.foreach { result =>
    printf("fac(%d) = %s\n", result.in, result.out)
  }

  // ExecutorServiceを終了する
  executorService.shutdown()
}
