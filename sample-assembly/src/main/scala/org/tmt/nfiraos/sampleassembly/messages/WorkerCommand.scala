package org.tmt.nfiraos.sampleassembly.messages

import csw.services.command.scaladsl.CommandService

sealed trait WorkerCommand
case class SendCommand(hcd: CommandService) extends WorkerCommand
