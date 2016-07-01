// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.

package org.nobreach.prolog

import java.util
import collection.JavaConversions._

import com.google.inject.Module
import org.kframework.main.KModule

class PrologKModule extends KModule {
  override def getKDocModules: util.List[Module] = List()

  override def getKompileModules: util.List[Module] = List(new PrologKompileModule())

  override def getKastModules: util.List[Module] = List()

  override def getKRunModules(definitionSpecificModules: util.List[Module]): util.List[Module] = List()

  override def getDefinitionSpecificKRunModules: util.List[Module] = List(new PrologKRunModule())

  override def getKTestModules: util.List[Module] = List()
}
