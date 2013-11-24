package com.google.cloud.backend.android.sample.guestbook.model;

import com.google.cloud.backend.android.CloudEntity;

public class LocationEntity {

	public static final String hashtag = "Hackathlon";
	private CloudEntity entity;
	private double latitude;
	private double longitude;
	private String userId;
	private String userName;

	public static final String LAT_KEY = "lat_key";
	public static final String LONG_KEY = "long_key";
	public static final String USERNAME_KEY = "username_key";

	public LocationEntity() {
	}

	public static class Builder {
		private CloudEntity entity;
		private double latitude;
		private double longitude;
		private String userId;
		private String userName;

		public LocationEntity create() {

			if (entity != null) {
				latitude = Double.valueOf(entity.getProperties().get(LAT_KEY).toString());
				longitude = Double.valueOf( entity.getProperties().get(LONG_KEY).toString());
				userName = entity.getProperties().get(USERNAME_KEY).toString();
				userId = entity.getCreatedBy().toString();
			} else {
				entity = new CloudEntity(hashtag);
				entity.put(LAT_KEY, String.valueOf(latitude));
				entity.put(LONG_KEY, String.valueOf(longitude));
				entity.put(USERNAME_KEY, userName);
				entity.setCreatedBy(userId);

			}

			final LocationEntity lEn = new LocationEntity();
			lEn.entity = entity;
			lEn.latitude = latitude;
			lEn.longitude = longitude;
			lEn.userId = userId;
			lEn.userName = userName;
			return lEn;

		}

		public Builder setLatitude(double latitude) {
			this.latitude = latitude;
			return this;

		}

		public Builder setEntity(CloudEntity entity) {
			this.entity = entity;
			return this;

		}

		public Builder setLongitude(double longitude) {
			this.longitude = longitude;
			return this;
		}

		public Builder setUserId(String userId) {
			this.userId = userId;
			return this;
		}

		public Builder setUserName(String userName) {
			this.userName = userName;
			return this;
		}

	}

	public CloudEntity getEntity() {
		return entity;
	}

	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public String getUserId() {
		return userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
		if (entity != null) {
			entity.put(LAT_KEY, String.valueOf(latitude));
		}
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
		if (entity != null) {
			entity.put(LONG_KEY, String.valueOf(longitude));
		}

	}
}
