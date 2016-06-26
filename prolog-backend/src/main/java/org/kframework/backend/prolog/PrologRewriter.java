package org.kframework.backend.prolog;

// Copyright (c) 2015-2016 K Team. All Rights Reserved.

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kframework.kil.Attribute;
import org.kframework.kompile.KompileOptions;
import org.kframework.krun.KRunOptions;
import org.kframework.krun.api.KRunState;
import org.kframework.krun.api.io.FileSystem;
import org.kframework.main.GlobalOptions;
import org.kframework.rewriter.SearchType;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.inject.Builtins;
import org.kframework.utils.inject.DefinitionScoped;
import org.kframework.utils.inject.RequestScoped;
import org.kframework.utils.options.SMTOptions;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

package org.kframework.backend.java.symbolic;

import com.google.inject.Inject;
import com.google.inject.Provider;
import org.kframework.RewriterResult;
import org.kframework.backend.java.compile.KOREtoBackendKIL;
import org.kframework.backend.java.indexing.IndexingTable;
import org.kframework.backend.java.kil.ConstrainedTerm;
import org.kframework.backend.java.kil.Definition;
import org.kframework.backend.java.kil.GlobalContext;
import org.kframework.backend.java.kil.KItem;
import org.kframework.backend.java.kil.KLabelConstant;
import org.kframework.backend.java.kil.Term;
import org.kframework.backend.java.kil.TermContext;
import org.kframework.backend.java.kil.Variable;
import org.kframework.backend.java.util.JavaKRunState;
import org.kframework.definition.Module;
import org.kframework.definition.Rule;
import org.kframework.kil.Attribute;
import org.kframework.kompile.KompileOptions;
import org.kframework.kore.K;
import org.kframework.kore.KVariable;
import org.kframework.krun.KRunOptions;
import org.kframework.krun.api.KRunState;
import org.kframework.krun.api.io.FileSystem;
import org.kframework.main.GlobalOptions;
import org.kframework.rewriter.Rewriter;
import org.kframework.rewriter.SearchType;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;
import org.kframework.utils.inject.Builtins;
import org.kframework.utils.inject.DefinitionScoped;
import org.kframework.utils.inject.RequestScoped;
import org.kframework.utils.options.SMTOptions;
import scala.Tuple2;
import scala.collection.JavaConversions;

import java.lang.invoke.MethodHandle;
import java.math.BigInteger;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequestScoped
public class PrologRewriterInit implements Function<Module, Rewriter> {

    private final FileSystem fs;
    private final JavaExecutionOptions javaOptions;
    private final GlobalOptions globalOptions;
    private final KExceptionManager kem;
    private final SMTOptions smtOptions;
    private final Map<String, Provider<MethodHandle>> hookProvider;
    private final KompileOptions kompileOptions;
    private final KRunOptions krunOptions;
    private final FileUtil files;
    private final InitializeDefinition initializeDefinition;
    private static final int NEGATIVE_VALUE = -1;

    @Inject
    public PrologRewriterInit(
            FileSystem fs,
            JavaExecutionOptions javaOptions,
            GlobalOptions globalOptions,
            KExceptionManager kem,
            SMTOptions smtOptions,
            @Builtins Map<String, Provider<MethodHandle>> hookProvider,
            KompileOptions kompileOptions,
            KRunOptions krunOptions,
            FileUtil files,
            InitializeDefinition initializeDefinition) {
        this.fs = fs;
        this.javaOptions = javaOptions;
        this.globalOptions = globalOptions;
        this.kem = kem;
        this.smtOptions = smtOptions;
        this.hookProvider = hookProvider;
        this.kompileOptions = kompileOptions;
        this.krunOptions = krunOptions;
        this.files = files;
        this.initializeDefinition = initializeDefinition;
    }

    @Override
    public synchronized Rewriter apply(Module module) {
        TermContext initializingContext = TermContext.builder(new GlobalContext(fs, javaOptions, globalOptions, krunOptions, kem, smtOptions, hookProvider, files, Stage.INITIALIZING))
                .freshCounter(0).build();
        Definition evaluatedDef = initializeDefinition.invoke(module, kem, initializingContext.global());

        GlobalContext rewritingContext = new GlobalContext(fs, javaOptions, globalOptions, krunOptions, kem, smtOptions, hookProvider, files, Stage.REWRITING);
        rewritingContext.setDefinition(evaluatedDef);

        return new PrologRewriter(module, evaluatedDef, kompileOptions, javaOptions, initializingContext.getCounterValue(), rewritingContext, kem);
    }

    public static class PrologRewriter implements Rewriter {

        private SymbolicRewriter rewriter;
        public final Definition definition;
        public final Module module;
        private final BigInteger initCounterValue;
        public final GlobalContext rewritingContext;
        private final KExceptionManager kem;
        private final KompileOptions kompileOptions;
        private final JavaExecutionOptions javaOptions;

        public PrologRewriter(
                Module module,
                Definition definition,
                KompileOptions kompileOptions,
                JavaExecutionOptions javaOptions,
                BigInteger initCounterValue,
                GlobalContext rewritingContext,
                KExceptionManager kem) {
            this.kompileOptions = kompileOptions;
            this.javaOptions = javaOptions;
            this.rewriter = null;
            this.definition = definition;
            this.module = module;
            this.initCounterValue = initCounterValue;
            this.rewritingContext = rewritingContext;
            this.kem = kem;
        }

        @Override
        public RewriterResult execute(K k, Optional<Integer> depth) {

        }

        @Override
        public List<? extends Map<? extends KVariable,? extends K>> match(K k, org.kframework.definition.Rule rule) {

        }


        @Override
        public List<? extends Map<? extends KVariable, ? extends K>> search(K initialConfiguration, Optional<Integer> depth, Optional<Integer> bound, Rule pattern, SearchType searchType) {

        }

        @Override
        public List<K> prove(List<Rule> rules) {

        }
    }
}