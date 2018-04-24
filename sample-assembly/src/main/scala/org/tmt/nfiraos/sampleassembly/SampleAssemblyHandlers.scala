package org.tmt.nfiraos.sampleassembly

import akka.actor.typed.scaladsl.ActorContext
import csw.framework.scaladsl.{ComponentHandlers, CurrentStatePublisher}
import csw.messages.commands.{CommandResponse, ControlCommand}
import csw.messages.framework.ComponentInfo
import csw.messages.location.{AkkaLocation, LocationRemoved, LocationUpdated, TrackingEvent}
import csw.messages.scaladsl.TopLevelActorMessage
import csw.services.command.scaladsl.{CommandResponseManager, CommandService}
import csw.services.location.scaladsl.LocationService
import csw.services.logging.scaladsl.{Logger, LoggerFactory}
import org.tmt.nfiraos.sampleassembly.internal.Worker
import org.tmt.nfiraos.sampleassembly.messages.SendCommand

import scala.concurrent.{ExecutionContextExecutor, Future}

/**
 * Domain specific logic should be written in below handlers.
 * This handlers gets invoked when component receives messages/commands from other component/entity.
 * For example, if one component sends Submit(Setup(args)) command to SampleHcd,
 * This will be first validated in the supervisor and then forwarded to Component TLA which first invokes validateCommand hook
 * and if validation is successful, then onSubmit hook gets invoked.
 * You can find more information on this here : https://tmtsoftware.github.io/csw-prod/framework.html
 */
class SampleAssemblyHandlers(
    ctx: ActorContext[TopLevelActorMessage],
    componentInfo: ComponentInfo,
    commandResponseManager: CommandResponseManager,
    currentStatePublisher: CurrentStatePublisher,
    locationService: LocationService,
    loggerFactory: LoggerFactory
) extends ComponentHandlers(ctx, componentInfo, commandResponseManager, currentStatePublisher, locationService, loggerFactory) {

  implicit val ec: ExecutionContextExecutor = ctx.executionContext
  private val log: Logger                   = loggerFactory.getLogger
  val workerActor                           = new Worker(ctx, loggerFactory, componentInfo)

  //initialize
  override def initialize(): Future[Unit] = Future {
    log.info("[Assembly] Initializing ...")
    println("[Assembly] Initializing ...")
  }

  override def onShutdown(): Future[Unit] = Future {
    log.info("[Assembly] Shutting down...")
    println("[Assembly] Shutting down...")
  }

  //track-location
  override def onLocationTrackingEvent(trackingEvent: TrackingEvent): Unit = {
    log.info(s"[Assembly] Received tracking event : ${trackingEvent.connection.name}")
    println(s"[Assembly] Received tracking event : ${trackingEvent.connection.name}")
    trackingEvent match {
      case LocationUpdated(location) =>
        val hcd = new CommandService(location.asInstanceOf[AkkaLocation])(ctx.system)
        workerActor.commandSender ! SendCommand(hcd)
      case LocationRemoved(_) => log.info("HCD no longer available")
    }
  }

  override def validateCommand(controlCommand: ControlCommand): CommandResponse = ???

  override def onSubmit(controlCommand: ControlCommand): Unit = ???

  override def onOneway(controlCommand: ControlCommand): Unit = ???

  override def onGoOffline(): Unit = ???

  override def onGoOnline(): Unit = ???

}
