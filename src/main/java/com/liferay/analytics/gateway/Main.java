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

package com.liferay.analytics.gateway;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.liferay.analytics.gateway.data.binding.JSONObjectMapper;
import com.liferay.analytics.gateway.model.AnalyticsEventsMessage;
import com.liferay.analytics.gateway.storage.CassandraStorage;

/**
 * @author Marcellus Tavares
 */
@RestController
@EnableAutoConfiguration
@ComponentScan
public class Main {

    @RequestMapping(value = "/", method = RequestMethod.POST)
    void store(@RequestBody String payload) {

    	try {
			AnalyticsEventsMessage analyticsEventsMessage =
				_jsonObjectMapper.map(payload);

			_cassandraStorage.addAnalyticsEvents(
				analyticsEventsMessage);
		}
    	catch (IOException e) {
    		if (_log.isDebugEnabled()) {
    			_log.debug("Discarding paylod, unable to parse " + payload);
    		}
		}
    }

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Main.class, args);
    }

    private static final Logger _log = LoggerFactory.getLogger(Main.class);

    @Autowired
    private CassandraStorage _cassandraStorage;

    @Autowired
    private JSONObjectMapper<AnalyticsEventsMessage> _jsonObjectMapper;

}