import { Web3Provider, JsonRpcSigner } from "@ethersproject/providers";

export async function signer(): Promise<JsonRpcSigner> {
  const provider = new Web3Provider(window.ethereum);
  await provider.send("eth_requestAccounts", []);
  return provider.getSigner();
}
