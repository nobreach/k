// Copyright (c) 2015-2016 K Team. All Rights Reserved.
// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.
package org.kframework.backend.prolog;

import java.util.function.Function;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import org.kframework.krun.api.io.FileSystem;
import org.kframework.krun.ioserver.filesystem.portable.PortableFileSystem;

import org.kframework.rewriter.Rewriter;

public class PrologBackendKrunModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FileSystem.class).to(PortableFileSystem.class);

//        MapBinder<String, org.kframework.kore.compile.Backend> prologRewriterBinder = MapBinder.newMapBinder(
//                binder(), String.class, org.kframework.rewriter.Rewriter.class);
//        prologRewriterBinder.addBinding("prolog").to(PrologRewriter.class);

        MapBinder<String, Function<org.kframework.definition.Module, Rewriter>> rewriterBinder = MapBinder.newMapBinder(
            binder(), TypeLiteral.get(String.class), new TypeLiteral<Function<org.kframework.definition.Module, Rewriter>>() {});
        rewriterBinder.addBinding("prolog").to(PrologRewriterInit.class);
    }
}



