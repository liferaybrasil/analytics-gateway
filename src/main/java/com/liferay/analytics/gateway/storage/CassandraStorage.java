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

package com.liferay.analytics.gateway.storage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.datastax.driver.core.BatchStatement;
import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.datastax.driver.core.Statement;
import com.liferay.analytics.gateway.model.AnalyticsEventsMessage;

/**
 * @author Marcellus Tavares
 */
@Component
public class CassandraStorage {

	@PostConstruct
	protected void activate() {
		_cluster = Cluster.builder()
						  .addContactPoint(_node)
						  .withPort(_port)
						  .build();

		_session = _cluster.connect();
	}

	@PreDestroy
	protected void deactivate() {
		_session.close();

		_cluster.close();
	}

	public void addAnalyticsEvents(
		AnalyticsEventsMessage analyticsEventsMessage) {

		BatchStatement batch = new BatchStatement();

		Date now = new Date();

		for (AnalyticsEventsMessage.Event event :
				analyticsEventsMessage.getEvents()) {

			batch.add(
				createInsertStatement(
					event.getApplicationId(),
					analyticsEventsMessage.getAnalyticsKey(),
					event.getEventId(), now, event.getProperties()));
		}

		if (_log.isDebugEnabled()) {
			_log.debug(
				String.format(
					"Batched %d events for insertion.",
					batch.getStatements().size()));
		}

		_session.executeAsync(batch);
	}

	protected Statement createInsertStatement(
		String applicationId, String analyticskey, String eventId, Date date,
		Map<String, String> properties) {

		PreparedStatement ps = _session.prepare(
			"insert into Analytics.AnalyticsEvent ( " +
    			"partitionKey, analyticskey, applicationid, eventid, " +
					"createdate, eventproperties) values (?, ?, ?, ?, ?, ?) ");

    	return ps.bind(
			getPartitionKey(date), analyticskey, applicationId, eventId, date,
			properties);
	}

	protected String getPartitionKey(Date createDate) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH-");

		Calendar calendar = Calendar.getInstance();

		calendar.setTime(createDate);

		int x = calendar.get(Calendar.MINUTE) / 15;

		return dateFormat.format(createDate) + String.valueOf(x);
	}

	public Session getSession() {
		return _session;
	}

	private static final Logger _log = LoggerFactory.getLogger(
		CassandraStorage.class);

	@Value("${analytics.gateway.cassandra.node}")
	private String _node;

	@Value("${analytics.gateway.cassandra.port}")
	private int _port;

	private Cluster _cluster;
	private Session _session;

}