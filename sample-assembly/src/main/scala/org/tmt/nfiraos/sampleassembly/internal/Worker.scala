package org.tmt.nfiraos.sampleassembly.internal

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import akka.util.Timeout
import csw.messages.commands.CommandResponse.Accepted
import csw.messages.commands.{CommandName, CommandResponse, Setup}
import csw.messages.framework.ComponentInfo
import csw.messages.params.generics.{Key, KeyType, Parameter}
import csw.messages.params.models.{ObsId, Units}
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.CommandService
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.nfiraos.sampleassembly.messages.{SendCommand, WorkerCommand}

import scala.concurrent.duration.DurationDouble
import scala.concurrent.{ExecutionContext, Future}

class Worker(ctx: ActorContext[TopLevelActorMessage], loggerFactory: LoggerFactory, componentInfo: ComponentInfo) {

  private val log = loggerFactory.getLogger(ctx)

  private implicit val ec: ExecutionContext = ctx.executionContext

  val commandSender: ActorRef[WorkerCommand] =
    ctx.spawn(
      Behaviors.immutable[WorkerCommand]((_, msg) => {
        msg match {
          case command: SendCommand =>
            log.info(s"WorkerActor received SendCommand message.")
            handleCommand(command.hcd)
          case _ => log.error("Unsupported message type")
        }
        Behaviors.same
      }),
      "CommandSender"
    )

  private implicit val submitTimeout: Timeout = Timeout(1000.millis)

  def handleCommand(hcd: CommandService): Unit = {

    // Construct Setup command
    val sleepTimeKey: Key[Long]         = KeyType.LongKey.make("SleepTime")
    val sleepTimeParam: Parameter[Long] = sleepTimeKey.set(5000).withUnits(Units.millisecond)
    val setupCommand                    = Setup(componentInfo.prefix, CommandName("sleep"), Some(ObsId("2018A-001"))).add(sleepTimeParam)

    // Submit command, and handle validation response.  Final response is returned as a Future
    val submitCommandResponseF: Future[CommandResponse] = hcd.submit(setupCommand).flatMap {
      case _: Accepted =>
        // If valid, subscribe to the HCD's CommandResponseManager
        // This explicit timeout indicates how long to wait for completion
        hcd.subscribe(setupCommand.runId)(10000.seconds)
      case x =>
        log.error("Sleep command invalid")
        Future(x)
    }

    // Wait for final response, and log result
    submitCommandResponseF.foreach {
      case _: CommandResponse.Completed => log.info("Command completed successfully")
      case x: CommandResponse.Error     => log.error(s"Command Completed with error: ${x.message}")
      case _                            => log.error("Command failed")
    }
  }
}
