package org.tmt.nfiraos.sampledeploy

import csw.framework.deploy.containercmd.ContainerCmd

object SampleContainerCmdApp extends App {

  ContainerCmd.start("sample-container-cmd-app", args)

}
