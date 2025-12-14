// server.js (at project root)
const express = require("express");
const path = require("path");

const app = express();
const PORT = 8081;

// paths based on your layout
const webDir = path.join(__dirname,  "web");
const genDir = path.join(__dirname, "generated");

// serve static frontend and generated JS
app.use("/generated", express.static(genDir));
app.use("/", express.static(webDir));

app.listen(PORT, () => {
  console.log(`Web frontend running at http://localhost:${PORT}`);
});
