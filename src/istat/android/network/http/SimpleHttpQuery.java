package istat.android.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/*
 * Copyright (C) 2014 Istat Dev.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
 * 
 * @author Toukea Tatsi (Istat)
 * 
 */
public class SimpleHttpQuery extends HttpQuery<SimpleHttpQuery> {

	@Override
	public InputStream doPost(String url) throws IOException {
		// TODO Auto-generated method stub
		
		return POST(url);
	}

	@Override
	public final SimpleHttpQuery addParams(HashMap<String, Object> nameValues) {
		// TODO Auto-generated method stub
		return super.addParams(nameValues);
	}

	@Override
	public void setParameterHandler(
			final istat.android.network.http.HttpQuery.ParameterHandler postHandler) {
		// TODO Auto-generated method stub

		ParameterHandler parameterHandler = new ParameterHandler() {

			@Override
			public String onStringifyQueryParams(String method,
					HashMap<String, String> params, String encoding) {
				// TODO Auto-generated method stub
				if ("GET".equalsIgnoreCase(method) || postHandler == null) {
					return ParameterHandler.DEFAULT_HANDLER
							.onStringifyQueryParams(method, params, encoding);
				}
				try {
					return postHandler.onStringifyQueryParams(method, params,
							encoding);
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}

		};
		super.setParameterHandler(parameterHandler);
	}

}
