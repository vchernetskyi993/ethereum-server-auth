import { JsonRpcSigner } from "@ethersproject/providers";
import React from "react";
import "./App.css";
import axios from "axios";

function App({ signer }: { signer: JsonRpcSigner }) {
  const serverUrl =
    process.env.REACT_APP_SERVER_URL ??
    throwErr("REACT_APP_SERVER_URL is required");
  const [greeting, setGreeting] = React.useState("");
  const greet = async () => {
    setGreeting("Loading...");
    const address = await signer.getAddress();
    axios
      .post(`${serverUrl}/nonce/${address}`)
      .then((nonce) => {
        return signer.signMessage(
          Array.from(nonce.data as string).map((c) => c.charCodeAt(0))
        );
      })
      .then((signature) =>
        axios.get(`${serverUrl}/hello`, {
          headers: { Authorization: `Ethereum ${address}.${signature}` },
        })
      )
      .then((resp) => setGreeting(resp.data));
  };
  const clean = () => {
    setGreeting("");
  };
  return (
    <div className="App">
      <table>
        <thead>
          <tr>
            <td>
              <h4>Say hello to server authentication with Ethereum wallets!</h4>
            </td>
          </tr>
        </thead>
        <tbody>
          <tr>
            <td>
              <button onClick={greet} disabled={!!greeting}>
                Receive greeting
              </button>
            </td>
          </tr>
          <tr hidden={!greeting}>
            <td>
              {greeting}
            </td>
          </tr>
          <tr>
            <td>
              <button onClick={clean} disabled={!greeting}>
                Clean greeting
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>
  );
}

function throwErr(message: string): never {
  throw new Error(message);
}

export default App;
