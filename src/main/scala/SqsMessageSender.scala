import akka.actor.ActorSystem
import akka.stream.alpakka.sqs.scaladsl.SqsSink
import akka.stream.scaladsl.{ RestartSink, Source }
import akka.stream.{ ActorMaterializer, ThrottleMode }
import com.amazonaws.services.sqs.{ AmazonSQSAsync, AmazonSQSAsyncClientBuilder }
import io.circe.generic.auto._
import io.circe.syntax._

import scala.concurrent.duration._

object SqsMessageSender extends App {
  val queueUrl = "http://127.0.0.1:9324/queue/test-queue"

  implicit val sqsClient: AmazonSQSAsync = AmazonSQSAsyncClientBuilder
    .standard()
    .build()

  implicit val system: ActorSystem = ActorSystem()
  implicit val mat: ActorMaterializer = ActorMaterializer()

  val rawSink = SqsSink(queueUrl)
  val restartSink = RestartSink.withBackoff(
    minBackoff = 3.seconds,
    maxBackoff = 60.seconds,
    randomFactor = 0.2
  ) { () =>
    rawSink
  }

  Source.fromIterator(() => Iterator.from(1)).map(i => Entity(s"test$i").asJson.toString).throttle(1, 1.seconds, 0, ThrottleMode.shaping).runWith(restartSink)
}
