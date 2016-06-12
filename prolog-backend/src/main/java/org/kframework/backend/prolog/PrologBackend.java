// Copyright (c) 2014-2016 K Team. All Rights Reserved.
// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.
package org.kframework.backend.prolog;

import com.google.inject.Inject;
import org.kframework.attributes.Att;
import org.kframework.builtin.KLabels;
import org.kframework.compile.AddBottomSortForListsWithIdenticalLabels;
import org.kframework.compile.NormalizeKSeq;
import org.kframework.compile.ConfigurationInfoFromModule;
import org.kframework.definition.Constructors;
import org.kframework.definition.Definition;
import org.kframework.definition.DefinitionTransformer;
import org.kframework.definition.Module;
import org.kframework.definition.Rule;
import org.kframework.definition.Sentence;
import org.kframework.kompile.CompiledDefinition;
import org.kframework.kompile.Kompile;
import org.kframework.kompile.KompileOptions;
import org.kframework.kore.ADT;
import org.kframework.kore.KToken;
import org.kframework.kore.VisitK;
import org.kframework.kore.K;
import org.kframework.kore.KApply;
import org.kframework.kore.KORE;
import org.kframework.kore.KSequence;
import org.kframework.kore.KVariable;
import org.kframework.kore.SortedADT;
import org.kframework.kore.compile.AssocCommToAssoc;
import org.kframework.kore.compile.Backend;
import org.kframework.kore.compile.ConvertDataStructureToLookup;
import org.kframework.kore.compile.KTokenVariablesToTrueVariables;
import org.kframework.kore.compile.MergeRules;
import org.kframework.kore.compile.NormalizeAssoc;
import org.kframework.kore.compile.RewriteToTop;
import org.kframework.kore.TransformK;
import org.kframework.main.GlobalOptions;
import org.kframework.utils.errorsystem.KExceptionManager;
import org.kframework.utils.file.FileUtil;

import java.util.function.Function;

import static scala.compat.java8.JFunction.func;

public class PrologBackend implements Backend {

    private final KExceptionManager kem;
    private final FileUtil files;
    private final GlobalOptions globalOptions;
    private final KompileOptions kompileOptions;

    @Override
    public void accept(CompiledDefinition def) {
    }

    @Inject
    public PrologBackend(KExceptionManager kem, FileUtil files, GlobalOptions globalOptions, KompileOptions kompileOptions) {
        this.kem = kem;
        this.files = files;
        this.globalOptions = globalOptions;
        this.kompileOptions = kompileOptions;

        System.out.println(
                " _   _        _                         _     \n" +
                " | \\ | |     | |                       | |    \n" +
                " |  \\| | ___ | |__  _ __ ___  __ _  ___| |__  \n" +
                " | . ` |/ _ \\| '_ \\| '__/ _ \\/ _` |/ __| '_ \\ \n" +
                " | |\\  | (_) | |_) | | |  __/ (_| | (__| | | |\n" +
                " |_| \\_|\\___/|_.__/|_|  \\___|\\__,_|\\___|_| |_|\n" +
                "                                              \n" +
                "                                              ");
    }

    /**
     * @param the generic {@link Kompile}
     * @return the special steps for the Java backend
     */
    @Override
    public Function<Definition, Definition> steps(Kompile kompile) {
        DefinitionTransformer convertDataStructureToLookup = DefinitionTransformer.fromSentenceTransformer(func((m, s) -> new ConvertDataStructureToLookup(m, false).convert(s)), "convert data structures to lookups");

        return d -> (func((Definition dd) -> kompile.defaultSteps().apply(dd)))
                .andThen(DefinitionTransformer.fromRuleBodyTranformer(RewriteToTop::bubbleRewriteToTopInsideCells, "bubble out rewrites below cells"))
                .andThen(DefinitionTransformer.fromSentenceTransformer(new NormalizeAssoc(KORE.c()), "normalize assoc"))
                .andThen(DefinitionTransformer.from(AddBottomSortForListsWithIdenticalLabels.singleton(), "AddBottomSortForListsWithIdenticalLabels"))

                .andThen(DefinitionTransformer.fromSentenceTransformer(new NormalizeAssoc(KORE.c()), "normalize assoc"))
                .andThen(convertDataStructureToLookup)
                //.andThen(DefinitionTransformer.fromRuleBodyTranformer(JavaBackend::ADTKVariableToSortedVariable, "ADT.KVariable to SortedVariable"))
                //.andThen(DefinitionTransformer.fromRuleBodyTranformer(JavaBackend::convertKSeqToKApply, "kseq to kapply"))
                .andThen(DefinitionTransformer.fromRuleBodyTranformer(NormalizeKSeq.self(), "normalize kseq"))
                //.andThen(func(dd -> markRegularRules(dd)))
                //.andThen(DefinitionTransformer.fromSentenceTransformer(new AddConfigurationRecoveryFlags(), "add refers_THIS_CONFIGURATION_marker"))
                //.andThen(DefinitionTransformer.fromSentenceTransformer(JavaBackend::markSingleVariables, "mark single variables"))
                .andThen(DefinitionTransformer.from(new AssocCommToAssoc(KORE.c()), "convert assoc/comm to assoc"))
                .andThen(DefinitionTransformer.from(new MergeRules(KORE.c()), "generate matching automaton"))
                .apply(d);
    }
}
