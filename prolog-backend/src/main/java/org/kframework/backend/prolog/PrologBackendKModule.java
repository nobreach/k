// Copyright (c) 2015-2016 K Team. All Rights Reserved.
// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.
package org.kframework.backend.prolog;


import com.google.common.collect.ImmutableList;
import com.google.inject.Module;
import org.kframework.main.KModule;

import java.util.List;

public class PrologBackendKModule implements KModule {
    @Override
    public List<Module> getKDocModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getKompileModules() {
        return ImmutableList.of(new PrologBackendKompileModule());
    }

    @Override
    public List<Module> getKastModules() {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getKRunModules(List<Module> definitionSpecificModules) {
        return ImmutableList.of();
    }

    @Override
    public List<Module> getDefinitionSpecificKRunModules() {

        return ImmutableList.of(new PrologBackendKrunModule());

    }

    @Override
    public List<Module> getKTestModules() {
        return ImmutableList.of();
    }
}
