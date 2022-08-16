# Ethereum Server Authentication

This is an example of Ethereum wallet authentication usage in web2 back-end.

* [Use case](#use-case)
* [Technical flows](#technical-flows)
   * [Authentication per-request](#authentication-per-request)
   * [JWT session](#jwt-session)
* [Usage](#usage)

## Use case

- We want to use web2 back-end features and web3 Ethereum from front-end
- We don't want to have 2 logins in our front-end app

## Technical flows

These flows aren't contradicting ones. See corresponding pros&cons for each for details. 
For example, we can use JWT for all GET requests and per-request signature for state-changing requests. 

### Authentication per-request

We authenticate each user request with unique token. User has to sign each back-end request.

Pros:
* more explicit - done in the spirit of web3, 
user knows when (and possibly why) back-end is called and has the right to reject

Cons:
* less user-friendly, potentially a lot of times to confirm server interactions
* overkill for GET requests

Steps:

1. `POST /nonce/{address}`, back-end:
   - generates 128-bit secure random nonce
   - stores ( address : nonce : issued_at ) relation
   - returns generated nonce
2. Front-end signs nonce using wallet.
3. Sends request to the server with auth header:

```
POST /hello
Authorization: Ethereum <address>.<signed 128-bit nonce as hex string>(.<optional base64 encoded action explanation>)
```

4. Back-end:
   - retrieves nonce expected for address
   - parses signature and verifies nonce & signer
   - removes nonce for address

Notes:

* Back-end periodically (e.g. every 5 mins) expires (i.e. removes) nonces

### JWT session

We authenticate user once and use JWT for future interactions. 
Common paradigm of access/refresh tokens could be used as well for even better user experience 
(in this toy example I only show access token usage).

Pros:
* better user experience:
   * one-time login
   * usual login/logout flow

Cons:
* all server interactions are implicit for the users

Steps:

1. `POST /nonce/{address}`, back-end:
   - generates 128-bit secure random nonce
   - stores ( address : nonce : issued_at ) relation
   - returns generated nonce
2. Front-end signs nonce using wallet.
3. Sends login request to the server with auth body:

```
POST /login
{
   "address": "<address>",
   "signature": "<signed 128-bit nonce as hex string>",
   "message": "<optional base64 encoded action explanation>"
}
```

4. Back-end:
   - retrieves nonce expected for address
   - parses signature and verifies nonce & signer
   - removes nonce for address
   - generates and returns JWT token with address claim

5. Front-end uses `Bearer <access token>` auth for all following interactions

Notes:

* Back-end periodically (e.g. every 5 mins) expires (i.e. removes) nonces

## Usage

1. Start server:

```bash
cd ktor-server
./gradlew clean run
```

2. Start client:

```bash
cd react-client
npm start
```

3. Go to `http://localhost:3000/` and connect your wallet
