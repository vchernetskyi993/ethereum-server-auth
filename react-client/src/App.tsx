import { JsonRpcSigner } from "@ethersproject/providers";
import React from "react";
import "./App.css";
import OneTime from "./components/OneTime";
import Session from "./components/Session";

function App({ signer }: { signer: JsonRpcSigner }) {
  const serverUrl =
    process.env.REACT_APP_SERVER_URL ??
    throwErr("REACT_APP_SERVER_URL is required");
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
        <OneTime signer={signer} serverUrl={serverUrl} />
        <Session signer={signer} serverUrl={serverUrl} />
      </table>
    </div>
  );
}

function throwErr(message: string): never {
  throw new Error(message);
}

export default App;
