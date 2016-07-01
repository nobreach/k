// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.

package org.nobreach.prolog

import java.util
import java.util.Optional

import collection.JavaConverters._
import org.kframework.RewriterResult
import org.kframework.attributes.Att
import org.kframework.definition._
import org.kframework.kil.{Attribute, Attributes}
import org.kframework.kore.Unapply.Sort
import org.kframework.kore._
import org.kframework.rewriter.{Rewriter, SearchType}
import org.kframework.tiny.Label

import collection._

object PrologRewriter {
  val self = this

  def apply(m: Module): PrologRewriter = new PrologRewriter(m)
}

class PrologRewriter(m: Module) extends org.kframework.rewriter.Rewriter {


  override def execute(k: K, depth: Optional[Integer]): RewriterResult = {
    println("Hello, and welcome to the Prolog backend :)")
    new RewriterResult(Optional.of(0), k)
  }

  override def `match`(k: K, rule: Rule): util.List[_ <: util.Map[_ <: KVariable, _ <: K]] = ???

  override def search(initialConfiguration: K, depth: Optional[Integer], bound: Optional[Integer], pattern: Rule, searchType: SearchType): util.List[_ <: util.Map[_ <: KVariable, _ <: K]] = ???

  override def executeAndMatch(k: K, depth: Optional[Integer], rule: Rule): (RewriterResult, util.List[_ <: util.Map[_ <: KVariable, _ <: K]]) = ???

  override def prove(rules: util.List[Rule]): util.List[K] = ???
}
