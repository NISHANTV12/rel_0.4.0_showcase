package org.tmt.nfiraos.samplehcd.messages

import csw.messages.params.models.Id

sealed trait WorkerCommand
case class Sleep(runId: Id, timeInMillis: Long) extends WorkerCommand
