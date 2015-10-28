package de.commercetools.android_example;

import android.os.Binder;

public class AuthenticationServiceBinder extends Binder {
    private final AuthenticationService service;

    public AuthenticationServiceBinder(final AuthenticationService service) {
        this.service = service;
    }

    public AuthenticationService getService() {
        return service;
    }
}
