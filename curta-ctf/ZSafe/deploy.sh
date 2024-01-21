#!/bin/bash

export $(cat .env | xargs)

forge script script/Deploy.s.sol:CurtaPuzzleDeploy --rpc-url "https://base.llamarpc.com" --with-gas-price 90000 --broadcast --slow
