/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.social.connect.jdbc;

import java.sql.SQLException;
import java.sql.Statement;

import javax.sql.DataSource;

import org.h2.Driver;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseFactory;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.UserScopedConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;

public class JdbcUsersUserScopedConnectionRepositoryTest extends AbstractUsersUserScopedConnectionRepositoryTest {

	protected static class MySqlCompatibleH2DatabaseConfigurer implements EmbeddedDatabaseConfigurer {
		@Override
		public void shutdown(DataSource dataSource, String databaseName) {
			try {
				java.sql.Connection connection = dataSource.getConnection();
				Statement stmt = connection.createStatement();
				stmt.execute("SHUTDOWN");
			}
			catch (SQLException ex) {
			}
		}
		
		@Override
		public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
			properties.setDriverClass(Driver.class);
			properties.setUrl(String.format("jdbc:h2:mem:%s;MODE=MYSQL;DB_CLOSE_DELAY=-1", databaseName));
			properties.setUsername("sa");
			properties.setPassword("");
		}
	}
	
	private EmbeddedDatabase database;

	private boolean testMySqlCompatiblity;
	
	JdbcTemplate dataAccessor;

	JdbcUsersConnectionRepository usersConnectionRepository;

	UserScopedConnectionRepository userScopedConnectionRepository;


	@Override
	protected UsersConnectionRepository getUsersConnectionRepository() {
		return usersConnectionRepository;
	}

	@Override
	protected UserScopedConnectionRepository getUserScopedConnectionRepository() {
		return userScopedConnectionRepository;
	}

	@Before
	public void setUp() {
		EmbeddedDatabaseFactory factory = new EmbeddedDatabaseFactory();
		if (testMySqlCompatiblity) {
			factory.setDatabaseConfigurer(new MySqlCompatibleH2DatabaseConfigurer());	
		} else {
			factory.setDatabaseType(EmbeddedDatabaseType.H2);			
		}
		ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
		populator.addScript(new ClassPathResource(getSchemaSql(), getClass()));
		factory.setDatabasePopulator(populator);
		database = factory.getDatabase();
		dataAccessor = new JdbcTemplate(database);
		usersConnectionRepository = new JdbcUsersConnectionRepository(database, getConnectionFactoryRegistry(), Encryptors.noOpText());
		if (!getTablePrefix().equals("")) {
			usersConnectionRepository.setTablePrefix(getTablePrefix());
		}
		userScopedConnectionRepository = usersConnectionRepository.createConnectionRepository(getUserId1());
	}
	
	@After
	public void tearDown() {
		if (database != null) {
			database.shutdown();
		}
	}
	
	protected void insertFooConnection() {
		dataAccessor.update("insert into " + getTablePrefix() + "UserConnection (userId, providerId, providerUserId, rank, displayName, profileUrl, imageUrl, accessToken, secret, refreshToken, expireTime) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				getUserId1(), "foo", "123", 1, "james", null, null, "234", "123", null, System.currentTimeMillis() + 3600000);
	}
	
	private void insertConnection(ConnectionData data, String userId, int rank) {
		dataAccessor.update("insert into " + getTablePrefix() + "UserConnection (userId, providerId, providerUserId, rank, displayName, profileUrl, imageUrl, accessToken, secret, refreshToken, expireTime) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
				userId, data.getProviderId(), data.getProviderUserId(), rank, data.getDisplayName(), data.getProfileUrl(), data.getImageUrl(), data.getAccessToken(), data.getSecret(), data.getRefreshToken(), System.currentTimeMillis() + 3600000);
	}
	
	@Override
	protected String getUserId1() {
		return "1";
	}

	@Override
	protected String getUserId2() {
		return "2";
	}

	@Override
	protected void insertTwitterConnection() {
		insertConnection(TWITTER_DATA, getUserId1(), 1);
	}

	@Override
	protected void insertFacebookConnection1() {
		insertConnection(FACEBOOK_DATA_1, getUserId1(), 1);
	}
		
	@Override
	protected void insertFacebookConnection2() {
		insertConnection(FACEBOOK_DATA_2, getUserId1(), 2);
	}

	@Override
	protected void insertFacebookConnection3() {
		insertConnection(FACEBOOK_DATA_3, getUserId2(), 2);
	}

	@Override
	protected void insertFacebookConnectionSameFacebookUser() {
		insertConnection(FACEBOOK_DATA_1, getUserId2(), 1);
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void noSuchConnectionFactory() {
		insertFooConnection();
		getUserScopedConnectionRepository().findAllConnections();
	}
}
