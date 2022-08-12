# Ethereum Server Authentication
# Ethereum Server Authentication

This is an example of Ethereum wallet authentication usage in web2 back-end.

## Use case:

- We want to use web2 back-end features and web3 Ethereum from front-end
- We don't want to have 2 logins in our front-end app

## Technical flow:

1. `POST /nonce/{address}`, back-end:
   - generates 128-bit secure random nonce
   - stores ( address : nonce : issued_at ) relation
   - returns generated nonce
2. Front-end signs nonce using wallet.
3. Sends request to the server with auth header:

```
POST /hello
Authorization: Ethereum <address>.<signed 128-bit nonce as hex string>
```

4. Back-end:
   - retrieves nonce expected for address
   - parses signature and verifies nonce & signer
   - removes nonce for address
5. Back-end periodically (e.g. every 5 mins) expires (i.e. removes) nonces
