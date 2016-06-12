// Copyright (c) 2015-2016 K Team. All Rights Reserved.
// Copyright (c) 2016 Nobreach Inc. All Rights Reserved.
package org.kframework.backend.prolog;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.MapBinder;
import org.kframework.krun.api.io.FileSystem;
import org.kframework.krun.ioserver.filesystem.portable.PortableFileSystem;

public class PrologBackendKompileModule extends AbstractModule {
    @Override
    protected void configure() {
        bind(FileSystem.class).to(PortableFileSystem.class);

        MapBinder<String, org.kframework.kore.compile.Backend> prologBackendBinder = MapBinder.newMapBinder(
                binder(), String.class, org.kframework.kore.compile.Backend.class);
        prologBackendBinder.addBinding("prolog").to(PrologBackend.class);

    }
}
