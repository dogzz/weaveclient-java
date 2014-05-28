package org.exfio.weave.client;

import org.exfio.weave.WeaveException;

public abstract class WeaveStorageContext {
	
	public abstract void init(WeaveClientParams params) throws WeaveException;

	public abstract WeaveApiClient getApiClient();

	public abstract WeaveBasicObject decryptWeaveBasicObject(WeaveBasicObject wbo, String collection) throws WeaveException;

	public abstract String decrypt(String payload, String collection) throws WeaveException;
	
	public abstract String encrypt(String plaintext, String collection) throws WeaveException;
	
	public abstract WeaveBasicObject get(String collection, String id, boolean decrypt) throws WeaveException;
	
	public WeaveBasicObject get(String collection, String id) throws WeaveException { return get(collection, id, true); }

	public abstract WeaveBasicObject[] getCollection(String collection, String[] ids, Double older, Double newer, Integer index_above, Integer index_below, Integer limit, Integer offset, String sort, String format, boolean decrypt) throws WeaveException;

	public WeaveBasicObject[] getCollection(String collection, String[] ids, Double older, Double newer, Integer index_above, Integer index_below, Integer limit, Integer offset, String sort, String format) throws WeaveException {
		return getCollection(collection, ids, older, newer, index_above, index_below, limit, offset, sort, format, true);
	}

	public abstract Double put(String collection, String id, WeaveBasicObject wbo, boolean encrypt) throws WeaveException;

	public Double put(String collection, String id, WeaveBasicObject wbo) throws WeaveException { return put(collection, id, wbo, true); }

	public abstract void delete(String collection, String id) throws WeaveException;
}
