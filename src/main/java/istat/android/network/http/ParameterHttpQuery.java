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
    @Override
    public void setParameterHandler(ParameterHandler parameterHandler) {
        super.setParameterHandler(parameterHandler);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HttpQ addSendable(HttpSendable sendable) {
        sendable.onFillHttpQuery(this);
        return (HttpQ) this;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public HttpQ addSendable(HttpSendable... sendableArray) {
        for (HttpSendable sendable : sendableArray) {
            sendable.onFillHttpQuery(this);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParams(HashMap<?, ?> nameValues) {
        return super.addParams(nameValues);
    }

    public HttpQ addParams(Object container) {
        return addParams(ToolKits.toHashMap(container, true, false, false));
    }

    public HttpQ addParams(Object container, boolean privateAndSuper) {
        return addParams(ToolKits.toHashMap(container, false, privateAndSuper, false));
    }

    public HttpQ addParams(Object container, String... ignoredFields) {
        return addParams(ToolKits.toHashMap(container, true, false, false, ignoredFields));
    }

    public HttpQ addParams(Object container, boolean privateAndSuper, String... ignoredFields) {
        return addParams(ToolKits.toHashMap(container, privateAndSuper, false, false, ignoredFields));
    }

    public void removeParam(String name) {
        parameters.remove(name);
        urlPramNames.remove(name);
    }


    @SuppressWarnings("unchecked")
    public HttpQ addParam(String Name, String... values) {
        for (int i = 0; i < values.length; i++) {
            addParam(Name + "[" + i + "]", values[i]);
        }
        return (HttpQ) this;
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParam(String Name, HashMap<?, ?> values) {
        Iterator<?> iterator = values.keySet().iterator();
        while (iterator.hasNext()) {
            Object name = iterator.next();
            Object value = values.get(name);
            addParam(Name + "[" + name + "]", value + "");
        }
        return (HttpQ) this;
    }

    public HttpQ addParam(String Name, String Value, boolean urlParam) {
        return super.addParam(Name, Value, urlParam);
    }

    @SuppressWarnings("unchecked")
    public HttpQ addParam(String Name, String Value) {
        return super.addParam(Name, Value);
    }
}
