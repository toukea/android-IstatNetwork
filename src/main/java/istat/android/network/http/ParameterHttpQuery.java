package istat.android.network.http;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import istat.android.network.http.interfaces.HttpSendable;
import istat.android.network.utils.ToolKits;

/**
 * Created by istat on 03/09/17.
 */

public abstract class ParameterHttpQuery<HttpQ extends HttpQuery<HttpQ>> extends HttpQuery<HttpQ> {
    ParameterHttpQuery() {

    }

    @Override
    public void setParameterHandler(ParameterHandler parameterHandler) {
        super.setParameterHandler(parameterHandler);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HttpQ putSendable(HttpSendable sendable) {
        sendable.onFillHttpQuery(this);
        return (HttpQ) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HttpQ putSendable(HttpSendable... sendableArray) {
        for (HttpSendable sendable : sendableArray) {
            sendable.onFillHttpQuery(this);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ putParams(HashMap<?, ?> nameValues) {
        return super.putParams(nameValues);
    }

    public HttpQ putParams(Object container) {
        return putParams(ToolKits.toHashMap(container, true, false, false));
    }

    public HttpQ putParams(Object container, boolean privateAndSuper) {
        return putParams(ToolKits.toHashMap(container, false, privateAndSuper, false));
    }

    public HttpQ putParams(Object container, String... ignoredFields) {
        return putParams(ToolKits.toHashMap(container, true, false, false, ignoredFields));
    }

    public HttpQ putParams(Object container, boolean privateAndSuper, String... ignoredFields) {
        return putParams(ToolKits.toHashMap(container, privateAndSuper, false, false, ignoredFields));
    }

    public boolean removeParam(String name) {
        boolean out = !parameters.isEmpty() || urlPramNames.isEmpty();
        parameters.remove(name);
        urlPramNames.remove(name);
        return out;
    }


    @SuppressWarnings("unchecked")
    public HttpQ putParam(String Name, String... values) {
        for (int i = 0; i < values.length; i++) {
            putParam(Name + "[" + i + "]", values[i]);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ putParam(String Name, HashMap<?, ?> values) {
        Iterator<?> iterator = values.keySet().iterator();
        while (iterator.hasNext()) {
            Object name = iterator.next();
            Object value = values.get(name);
            putParam(Name + "[" + name + "]", value + "");
        }
        return (HttpQ) this;
    }

    public HttpQ putParam(String Name, String Value, boolean urlParam) {
        return super.putParam(Name, Value, urlParam);
    }

    @SuppressWarnings("unchecked")
    public HttpQ putParam(String Name, String Value) {
        return super.putParam(Name, Value);
    }
}
