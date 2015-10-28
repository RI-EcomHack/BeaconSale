package de.commercetools.android_example;

import android.os.Binder;

public class SphereServiceBinder extends Binder {
    private final SphereService service;

    public SphereServiceBinder(final SphereService service) {
        this.service = service;
    }

    public SphereService getService() {
        return service;
    }
}
