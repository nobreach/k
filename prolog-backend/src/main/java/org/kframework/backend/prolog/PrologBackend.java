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
    }

    /**
     * @param the generic {@link Kompile}
     * @return the special steps for the Prolog backend
     */
    @Override
    public Function<Definition, Definition> steps(Kompile kompile) {
        return d -> (func((Definition dd) -> kompile.defaultSteps().apply(dd)))
            .apply(d);
    }
}
