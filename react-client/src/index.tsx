import React from "react";
import ReactDOM from "react-dom/client";
import "./index.css";
import App from "./App";
import { signer } from "./ethereum";

const root = ReactDOM.createRoot(
  document.getElementById("root") as HTMLElement
);
signer().then((signer) => {
  root.render(
    <React.StrictMode>
      <App signer={signer} />
    </React.StrictMode>
  );
});
