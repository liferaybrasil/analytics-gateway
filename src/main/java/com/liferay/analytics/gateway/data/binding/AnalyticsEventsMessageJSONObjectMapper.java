/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.analytics.gateway.data.binding;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.liferay.analytics.gateway.model.AnalyticsEventsMessage;

/**
 * @author Marcellus Tavares
 */
@Component
public class AnalyticsEventsMessageJSONObjectMapper
	implements JSONObjectMapper<AnalyticsEventsMessage> {

	@Override
	public String map(AnalyticsEventsMessage analyticsEventsMessage)
		throws IOException {

		return _objectMapper.writeValueAsString(analyticsEventsMessage);
	}

	@Override
	public AnalyticsEventsMessage map(String jsonString) throws IOException {
		return _objectMapper.readValue(
			jsonString, AnalyticsEventsMessage.class);
	}

	private final ObjectMapper _objectMapper = new ObjectMapper();

	{
		_objectMapper.addMixIn(
			AnalyticsEventsMessage.class, AnalyticsEventsMessageMixIn.class);

		_objectMapper.addMixIn(
			AnalyticsEventsMessage.Event.class, EventMixIn.class);

		_objectMapper.configure(
			DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	private static final class AnalyticsEventsMessageMixIn {

		@JsonProperty("analyticsKey")
		private String _analyticsKey;

		@JsonProperty("context")
		private Map<String, String> _context;

		@JsonProperty("events")
		private List<?> _events;

		@JsonProperty("protocolVersion")
		private String _protocolVersion;

		@JsonProperty("userId")
		private String _userId;

	}

	private static final class EventMixIn {

		@JsonProperty("applicationId")
		private String _applicationId;

		@JsonProperty("eventId")
		private String _eventId;

		@JsonProperty("properties")
		private Map<String, String> _properties;

	}

}