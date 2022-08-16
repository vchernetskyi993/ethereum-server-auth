import { JsonRpcSigner } from "@ethersproject/providers";
import React from "react";
import axios from "axios";

function OneTime({
  signer,
  serverUrl,
}: {
  signer: JsonRpcSigner;
  serverUrl: string;
}) {
  const [greeting, setGreeting] = React.useState("");
  const greet = async (explanation: string) => {
    setGreeting("Loading...");
    const address = await signer.getAddress();
    axios
      .post(`${serverUrl}/nonce/${address}`)
      .then((nonce) => signer.signMessage(`${explanation}${nonce.data}`))
      .then((signature) =>
        axios.get(`${serverUrl}/hello`, {
          headers: {
            Authorization:
              `Ethereum ${address}.${signature}` +
              (explanation ? `.${btoa(explanation)}` : ""),
          },
        })
      )
      .then((resp) => setGreeting(resp.data));
  };
  const clean = () => {
    setGreeting("");
  };
  return (
    <tbody>
      <tr>
        <td>Signature per request example</td>
      </tr>
      <tr>
        <td>
          <button onClick={() => greet("")} disabled={!!greeting}>
            Receive greeting
          </button>
        </td>
      </tr>
      <tr>
        <td>
          <button
            onClick={() =>
              greet(
                "Sign this message if you agree to interact with a server and receive greeting from there. " +
                  "This action will not cost any gas fees.\n\nNonce: "
              )
            }
            disabled={!!greeting}
          >
            Receive with explanation
          </button>
        </td>
      </tr>
      <tr hidden={!greeting}>
        <td>{greeting}</td>
      </tr>
      <tr>
        <td>
          <button onClick={clean} disabled={!greeting}>
            Clean greeting
          </button>
        </td>
      </tr>
    </tbody>
  );
}

export default OneTime;
