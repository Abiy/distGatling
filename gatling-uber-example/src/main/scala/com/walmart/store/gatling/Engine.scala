/*
 *
 *   Copyright 2016 Walmart Technology
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 *
 */

package com.walmart.store.gatling

import io.gatling.app.Gatling
import io.gatling.core.config.GatlingPropertiesBuilder

object Engine extends App {

  val props = new GatlingPropertiesBuilder
  props.resultsDirectory(System.getProperty("resultsFolder"))
  props.dataDirectory(System.getProperty("dataFolder"))
  props.simulationClass(System.getProperty("simulationClass"))
  props.noReports()
  props.mute()
  Gatling.fromMap(props.build)
  sys.exit()

  /** val usage =
    """
      |Usage: java -jar gatling-1.0-SNAPSHOT.jar [similation name]
    """.stripMargin
  val props = new GatlingPropertiesBuilder
  props.dataDirectory("jar")
  if(args.length == 0) {
    println(usage)
  } else {
    props.simulationClass(args(0))
    Gatling.fromMap(props.build)
  }
  sys.exit()
    **/
}