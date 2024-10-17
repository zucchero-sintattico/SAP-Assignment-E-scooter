package ride_service
import io.vertx.core.Vertx

object RideServiceApp {
  def main(args: Array[String]): Unit = {
    val app = new RideServiceApp()
    app.run()
  }
}

class RideServiceApp {
  private def run(): Unit = {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(new RideServiceVerticle())
  }
}