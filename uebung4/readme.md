

cd src/main $ node server.js
//to build backend 
cd uebung_4/
$mvn clean compile
$mvn exec:java -Dexec.mainClass="uulm.in.vs.ex4.ChatServer"

only for js client:
download https://github.com/improbable-eng/grpc-web/releases
execute with following params: 
./grpcwebproxy-v0.15.0-win64.exe --backend_addr=localhost:5555 --run_tls_server=false --allow_all_origins --server_http_debug_port=8080 --server_http_max_read_timeout=0s --server_http_max_write_timeout=0s --use_websockets
build fronted js client
cd uebung4/src/main/web 
$ npx webpack 
start server for js client
cd /d/bin/uniulm/verteilte_systeme/uebung4/src/main && node server.js

start java client

mvn exec:java -Dexec.mainClass="uulm.in.vs.ex4.ChatClient"