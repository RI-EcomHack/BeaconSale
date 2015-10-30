package de.commercetools.android_example;


import com.android.volley.Request;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import static java.util.Arrays.*;

/**
 * This class should help you to build the common requests to the sphere-api.
 * For the documentation on how to filter, sort, etc. see http://dev.sphere.io/http-api.html
 *
 * You can enter predicates in clear text like: request.where( "masterData(current(slug(en=\"peter-42\")))" );
 * Quotes have to be escaped.
 *
 * Note that for 'where' and 'expand' you can add multiple clauses, which will be appended, while for 'sort', 'query',
 * 'offset' and 'limit' previously defined values will be overwritten.
 */
public class SphereRequest {
    private static final String UTF_8 = "UTF-8";

    public final int method;
    public final String resource;
    public final String body;

    private String where;
    private String sort;
    private String query;
    private String expand;
    private String limit;
    private String offset;

    private SphereRequest(final int method, final String resource, final String body) {
        this.method = method;
        this.resource = resource;
        this.body = body;
    }

    public static SphereRequest get(final String resource) {
        return new SphereRequest(Request.Method.GET, resource, null);
    }

    public static SphereRequest post(final String resource, final String body) {
        return new SphereRequest(Request.Method.POST, resource, body);
    }

    public static SphereRequest delete(final String resource, final String body) {
        return new SphereRequest(Request.Method.DELETE, resource, body);
    }

    public String getUrl() {
        String params = "";
        for(final String s : asList(where, sort, query, expand, limit, offset)) {
            if (s == null) continue;
            params += params.equals("") ? s : "&" + s;
        }
        return resource + (!params.equals("") ? "?" + params : "");
    }

    public SphereRequest where(final String value) {
        String newWhereClause = "where=" + urlEncoded(value);
        where = where == null ? newWhereClause : where + "&" + newWhereClause;
        return this;
    }

    public SphereRequest sort(final String value) {
        sort = "sort=" + urlEncoded(value);
        return this;
    }

    public SphereRequest query(final String value) {
        query = "query=" + urlEncoded(value);
        return this;
    }

    public SphereRequest expand(final String value) {
        final String newExpandClause = "expand=" + urlEncoded(value);
        expand = expand == null ? newExpandClause : expand + "&" + newExpandClause;
        return this;
    }

    public SphereRequest limit(final int value) {
        limit = "limit=" + urlEncoded(String.valueOf(value));
        return this;
    }

    public SphereRequest offset(final int value) {
        offset = "offset=" + urlEncoded(String.valueOf(value));
        return this;
    }

    private String urlEncoded(final String value) {
        String urlEncoded = "";
        try {
            urlEncoded = URLEncoder.encode(value, UTF_8);
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 unknown");
        }
        return urlEncoded;
    }
}
