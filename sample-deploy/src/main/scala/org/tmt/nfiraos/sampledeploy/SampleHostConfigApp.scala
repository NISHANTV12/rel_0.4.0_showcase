package org.tmt.nfiraos.sampledeploy

import csw.framework.deploy.hostconfig.HostConfig

object SampleHostConfigApp extends App {

  HostConfig.start("sample-host-config-app", args)

}
