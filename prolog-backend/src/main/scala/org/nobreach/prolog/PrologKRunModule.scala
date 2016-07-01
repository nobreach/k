// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.

package org.nobreach.prolog

import java.util.function.Function

import com.google.inject.{AbstractModule, TypeLiteral}
import com.google.inject.multibindings.MapBinder
import org.kframework.definition.Module
import org.kframework.rewriter.Rewriter

class PrologKRunModule extends AbstractModule {
  override def configure(): Unit = {
    val rewriterBinder: MapBinder[String, Function[Module, org.kframework.rewriter.Rewriter]] = MapBinder.newMapBinder(binder, TypeLiteral.get(classOf[String]), new TypeLiteral[Function[Module, org.kframework.rewriter.Rewriter]]() {})
    rewriterBinder.addBinding("prolog").toInstance(PrologRewriter.self.apply)
  }
}
