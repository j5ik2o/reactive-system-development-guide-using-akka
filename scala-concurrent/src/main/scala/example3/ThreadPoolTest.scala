package example3

import java.util.concurrent.{ExecutorService, Executors}

object ThreadPoolTest extends App {

  // ExecutorService#newFixedThreadPoolでスレッドプールを作成する
  val executorService: ExecutorService = Executors.newFixedThreadPool(3)

  for (_ <- 0 to 5000) {
    val r = new Runnable {
      override def run(): Unit = {
        val id = Thread.currentThread.getId
        println(s"アクティブなスレッド数 = $getActiveThreadsCount")
        printf("タスクを開始しました: threadId = %d, task = %s\n", id, this)
        Thread.sleep(1000 * 20)
        printf("タスクが終了しました: threadId = %d, task = %s\n", id, this)
      }
    }
    // タスクの実行を依頼する
    executorService.submit(r)
  }

  // ExecutorServiceを終了する
  executorService.shutdown()

  // アクティブなスレッド数を取得する関数
  def getActiveThreadsCount: Int = {
    val group = Thread.currentThread().getThreadGroup
    group.activeCount()
  }

}
