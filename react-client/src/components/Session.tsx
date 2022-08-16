import { JsonRpcSigner } from "@ethersproject/providers";
import React from "react";
import axios from "axios";

function Session({
  signer,
  serverUrl,
}: {
  signer: JsonRpcSigner;
  serverUrl: string;
}) {
  const [token, setToken] = React.useState("");
  const login = async () => {
    const address = await signer.getAddress();
    axios
      .post(`${serverUrl}/nonce/${address}`)
      .then((nonce) => signer.signMessage(nonce.data))
      .then((signature) =>
        axios.post(`${serverUrl}/login`, {
          address,
          signature,
        })
      )
      .then((resp) => setToken(resp.data.accessToken));
  };
  const logout = () => {
    setToken("");
    clean();
  };

  const [message, setMessage] = React.useState("");
  const greet = async () => {
    setMessage("Loading...");
    axios
      .get(`${serverUrl}/jwt/hello`, {
        headers: { Authorization: `Bearer ${token}` },
      })
      .then((resp) => setMessage(resp.data));
  };
  const clean = () => setMessage("");

  return (
    <tbody>
      <tr>
        <td>JWT session example</td>
      </tr>
      <tr>
        {token ? (
          <td>
            <button onClick={logout}>Logout</button>
          </td>
        ) : (
          <td>
            <button onClick={login}>Login</button>
          </td>
        )}
      </tr>
      <tr>
        <td>
          <button onClick={greet} disabled={!token || !!message}>
            Receive greeting
          </button>
        </td>
      </tr>
      <tr hidden={!message}>
        <td>{message}</td>
      </tr>
      <tr>
        <td>
          <button onClick={clean} disabled={!message}>
            Clean greeting
          </button>
        </td>
      </tr>
    </tbody>
  );
}

export default Session;
