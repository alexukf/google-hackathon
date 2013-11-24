package com.google.cloud.backend.android.sample.guestbook;

import java.util.Iterator;
import java.util.List;

import com.google.cloud.backend.android.sample.guestbook.model.LocationEntity;



public class Utils {
	public static LocationEntity findLocationEntityByClientId(
			List<LocationEntity> locations, String clientId) {

		Iterator<LocationEntity> iterator = locations.iterator();
		LocationEntity toIterate;
		while (iterator.hasNext()) {
			toIterate = iterator.next();
			if (toIterate.getUserId() != null
					&& toIterate.getUserId().equals(clientId)) {
				return toIterate;
			}
		}

		return null;
	}
}
