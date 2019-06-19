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
package org.springframework.social.connect.mem;

import org.junit.Before;
import org.springframework.social.connect.Connection;
import org.springframework.social.connect.ConnectionData;
import org.springframework.social.connect.UserScopedConnectionRepository;
import org.springframework.social.connect.UsersConnectionRepository;
import org.springframework.social.connect.jdbc.AbstractUsersUserScopedConnectionRepositoryTest;

public class InMemoryUsersUserScopedConnectionRepositoryTest extends AbstractUsersUserScopedConnectionRepositoryTest {

	private UserScopedConnectionRepository userScopedConnectionRepository;

	private InMemoryUsersConnectionRepository usersConnectionRepository;

	@Before
	public void setUp() {
		usersConnectionRepository = new InMemoryUsersConnectionRepository(getConnectionFactoryRegistry());
		userScopedConnectionRepository = usersConnectionRepository.createConnectionRepository(getUserId1());
	}
	

	
	@Override
	protected UserScopedConnectionRepository getUserScopedConnectionRepository() {
		return userScopedConnectionRepository;
	}

	@Override
	protected UsersConnectionRepository getUsersConnectionRepository() {
		return usersConnectionRepository;
	}

	// PRIVATE SUPPORT METHODS
	
	private void insertFacebookConnection(ConnectionData data, String userId) {
		Connection<TestFacebookApi> facebookConnection = getFacebookConnectionFactory().createConnection(data);
		usersConnectionRepository.createConnectionRepository(userId).addConnection(facebookConnection);
	}
	
	@Override
	protected void insertTwitterConnection() {
		Connection<TestTwitterApi> twitterConnection = getTwitterConnectionFactory().createConnection(TWITTER_DATA);
		userScopedConnectionRepository.addConnection(twitterConnection);
	}

	@Override
	protected void insertFacebookConnection1() {
		insertFacebookConnection(FACEBOOK_DATA_1, getUserId1());
	}

	@Override
	protected void insertFacebookConnection2() {
		insertFacebookConnection(FACEBOOK_DATA_2, getUserId1());
	}

	@Override
	protected void insertFacebookConnection3() {
		insertFacebookConnection(FACEBOOK_DATA_3, getUserId2());
	}

	@Override
	protected void insertFacebookConnectionSameFacebookUser() {
		insertFacebookConnection(FACEBOOK_DATA_1, getUserId2());
	}

	@Override
	protected String getUserId1() {
		return "1";
	}

	@Override
	protected String getUserId2() {
		return "2";
	}
}
