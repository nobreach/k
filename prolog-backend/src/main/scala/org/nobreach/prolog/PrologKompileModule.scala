// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.

package org.nobreach.prolog

import com.google.inject.AbstractModule
import com.google.inject.multibindings.MapBinder
import org.kframework.kore.compile.Backend

class PrologKompileModule extends AbstractModule {
  override def configure(): Unit = {
    val prologBackendBinder: MapBinder[String, Backend] = MapBinder.newMapBinder(binder, classOf[String], classOf[Backend])
    prologBackendBinder.addBinding("prolog").to(classOf[PrologBackend])
  }
}
