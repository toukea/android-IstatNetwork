package istat.android.network.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
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

}
