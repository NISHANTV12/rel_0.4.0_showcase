package org.tmt.nfiraos.samplehcd.internal

import akka.actor.typed.ActorRef
import akka.actor.typed.scaladsl.{ActorContext, Behaviors}
import csw.messages.commands.CommandResponse
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.CommandResponseManager
import csw.services.logging.scaladsl.LoggerFactory
import org.tmt.nfiraos.samplehcd.messages.{Sleep, WorkerCommand}

class Worker(
    ctx: ActorContext[TopLevelActorMessage],
    loggerFactory: LoggerFactory,
    commandResponseManager: CommandResponseManager
) {
  private val log = loggerFactory.getLogger(ctx)

  val actor: ActorRef[WorkerCommand] = ctx.spawn(
    Behaviors.immutable[WorkerCommand]((_, msg) => {
      msg match {
        case sleep: Sleep =>
          log.info(s"[HCD] WorkerActor received sleep command with time of ${sleep.timeInMillis} ms")
          println(s"[HCD] WorkerActor received sleep command with time of ${sleep.timeInMillis} ms")
          println()

          // simulate long running command
          Thread.sleep(sleep.timeInMillis)
          commandResponseManager.addOrUpdateCommand(sleep.runId, CommandResponse.Completed(sleep.runId))

        case _ => log.error("Unsupported message type")
      }
      Behaviors.same
    }),
    "WorkerActor"
  )

}
