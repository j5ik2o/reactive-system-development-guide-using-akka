package example2

object ManyThreadTest extends App {

  for (_ <- 0 to 10000) {
    val r = new Runnable {
      override def run(): Unit = {
        val id = Thread.currentThread().getId
        println(s"アクティブなスレッド数 = $getActiveThreadsCount")
        printf("タスクを開始しました: threadId = %d, task = %s\n", id, this)
        Thread.sleep(1000 * 20)
        printf("タスクが終了しました: threadId = %d, task = %s\n", id, this)
      }
    }
    val t = new Thread(r)
    t.start()
  }

  // アクティブなスレッド数を取得する関数
  def getActiveThreadsCount: Int = {
    val group = Thread.currentThread().getThreadGroup
    group.activeCount()
  }

}
