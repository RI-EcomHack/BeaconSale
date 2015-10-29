package de.commercetools.android_example;

import org.junit.Test;

import static org.junit.Assert.*;

public class SphereRequestTest {

    @Test
    public void where() {
        final SphereRequest request = SphereRequest.get("")
                .where("masterData(current(name(en=\"Test\")))")
                .where("masterData(current(slug(en=\"Test\")))");
        final String expected = "?where=masterData%28current%28name%28en%3D%22Test%22%29%29%29" +
                "&where=masterData%28current%28slug%28en%3D%22Test%22%29%29%29";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void sort() {
        final SphereRequest request = SphereRequest.get("")
                .sort("test")
                .sort("test2");
        final String expected = "?sort=test2";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void query() {
        final SphereRequest request = SphereRequest.get("")
                .query("test")
                .query("test2");
        final String expected = "?query=test2";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void expand() {
        final SphereRequest request = SphereRequest.get("")
                .expand("test1")
                .expand("test2");
        final String expected = "?expand=test1&expand=test2";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void limit() {
        final SphereRequest request = SphereRequest.get("")
                .limit(1)
                .limit(2);
        final String expected = "?limit=2";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void offset() {
        final SphereRequest request = SphereRequest.get("")
                .offset(1)
                .offset(2);
        final String expected = "?offset=2";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void url() {
        final SphereRequest request = SphereRequest.get("")
                .where("testWhere")
                .sort("testSort")
                .query("testQuery")
                .expand("testExpand")
                .limit(10)
                .offset(20);
        final String expected = "?where=testWhere&sort=testSort&query=testQuery&expand=testExpand&limit=10&offset=20";

        assertEquals(expected, request.getUrl());
    }

    @Test
    public void empty() {
        final SphereRequest request = SphereRequest.get("resource");
        final String expected = "resource";

        assertEquals(expected, request.getUrl());
    }
}