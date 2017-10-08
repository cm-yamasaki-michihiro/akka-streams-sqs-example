
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.alpakka.sqs.scaladsl.{ SqsAckSink, SqsSource }
import akka.stream.alpakka.sqs.{ Delete, Ignore, MessageAction }
import akka.stream.scaladsl.{ Flow, Keep, RestartFlow, RestartSink, RestartSource, RunnableGraph, Sink, Source }
import akka.{ Done, NotUsed }
import com.amazonaws.services.sqs.model.Message
import com.amazonaws.services.sqs.{ AmazonSQSAsync, AmazonSQSAsyncClientBuilder }

import scala.concurrent.duration._
import scala.concurrent.{ ExecutionContext, Future }
import scala.util.Try

object Main extends App {

  val queueUrl = "http://127.0.0.1:9324/queue/test-queue"

  implicit val sqsClient: AmazonSQSAsync = AmazonSQSAsyncClientBuilder
    .standard()
    .build()

  implicit val system: ActorSystem = ActorSystem()
  implicit val ec: ExecutionContext = system.dispatcher
  implicit val mat: ActorMaterializer = ActorMaterializer(ActorMaterializerSettings(system))

  /*
  Source Sink Flow
  */

  val sqsSource: Source[Message, NotUsed] = SqsSource(queueUrl)

  val sqsAckSink: Sink[(Message, MessageAction), Future[Done]] = SqsAckSink(queueUrl)

  val messageProcessingFlow = Flow[Message].mapAsyncUnordered(10) { message =>
    processMessage(message).map(action => (message, action))
  }

  /*
  メッセージを実際に処理
   */

  def processMessage(message: Message): Future[MessageAction] = {

    def decoded = Future.fromTry(decodeMessage(message))

    def processed = for {
      entity <- decoded
      _ <- processEntity(entity)
    } yield ()

    processed
      .map(_ => Delete())
      .recover { case _ => Ignore() }
  }

  def decodeMessage(message: Message): Try[Entity] = {
    import io.circe._
    import io.circe.generic.auto._
    parser.decode[Entity](message.getBody).toTry
  }


  def processEntity(entity: Entity) = Future {
    println(entity)
  }


  /*
  処理が終了してしまったときに再起動ように実行
   */

  def restartingRefactored(): Unit = {
    val graphToRun: RunnableGraph[Future[Done]] =
      sqsSource.via(messageProcessingFlow).toMat(sqsAckSink)(Keep.right)

    RestartSource.withBackoff(
      minBackoff = 3.seconds,
      maxBackoff = 60.seconds,
      randomFactor = 0.2
    ) { () => Source.fromFuture(graphToRun.run()) }
      .runWith(Sink.ignore)
  }

  restartingRefactored()
}
