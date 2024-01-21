#!/bin/bash

export $(cat .env | xargs)

forge script script/SafeExploit.s.sol:SafeScriptSetup --skip-simulation --rpc-url "$RPC_URL" --broadcast --slow

forge script script/SafeExploit.s.sol:SafeScriptOne --skip-simulation --rpc-url "$RPC_URL" --broadcast --slow
forge script script/SafeExploit.s.sol:SafeScriptTwo --ffi --skip-simulation --rpc-url "$RPC_URL" --broadcast --slow
forge script script/SafeExploit.s.sol:SafeScriptThree --skip-simulation --rpc-url "$RPC_URL" --broadcast --slow

