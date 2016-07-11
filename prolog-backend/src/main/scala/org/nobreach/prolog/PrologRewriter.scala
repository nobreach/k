// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.

package org.nobreach.prolog

import java.util
import java.util.Optional

import collection.JavaConverters._
import org.kframework.RewriterResult
import org.kframework.attributes.Att
import org.kframework.definition._
import org.kframework.kil.{Attribute, Attributes}
import org.kframework.kore._
import org.kframework.parser.Term
import org.kframework.rewriter.{Rewriter, SearchType}
import org.kframework.tiny.And




object PrologRewriter {
  val self = this

  def apply(m: Module): PrologRewriter = new PrologRewriter(m)

  private def isEffectivelyAssoc(att: Att): Boolean =
    att.contains(Att.assoc) && !att.contains(Att.assoc) || att.contains(Att.bag)

  /*
  // todo: what type is "Label" ?!
  val hooks: Map[String, Label] = Map({
    "INT.Int" -> INT
  })
  */

}

class PrologRewriter(m: Module) extends org.kframework.rewriter.Rewriter {

  private val productionLike = m.sentences.collect({
    case p: Production => p
    case s: SyntaxSort => s
  })

  private val assocProductions = productionLike.filter(p => PrologRewriter.isEffectivelyAssoc(p.att))
  private val nonAssocProductions = productionLike &~ assocProductions

  override def execute(k: K, depth: Optional[Integer]): RewriterResult = {

    //println(rules.toString())
    rules.foreach(println)
    new RewriterResult(Optional.of(0), k)
  }



  def convert(body: K): Term = body match {
    case Unapply.KToken(s, sort) => ???
  }

  val rules = m.rules map {
    case Rule(body, requires, ensures, att) => body
  }


  override def `match`(k: K, rule: Rule): util.List[_ <: util.Map[_ <: KVariable, _ <: K]] = ???

  override def search(initialConfiguration: K, depth: Optional[Integer], bound: Optional[Integer], pattern: Rule, searchType: SearchType): util.List[_ <: util.Map[_ <: KVariable, _ <: K]] = ???

  override def executeAndMatch(k: K, depth: Optional[Integer], rule: Rule): (RewriterResult, util.List[_ <: util.Map[_ <: KVariable, _ <: K]]) = ???

  override def prove(rules: util.List[Rule]): util.List[K] = ???
}