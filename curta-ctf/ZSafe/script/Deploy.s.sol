// SPDX-License-Identifier: UNLICENSED
pragma solidity ^0.8.20;

import "forge-std/Script.sol";
import "src/SafeCurta.sol";
import { console2 } from "forge-std/console2.sol";
import { stdJson } from "forge-std/StdJson.sol";

contract CurtaPuzzleDeploy is Script {
    using stdJson for string;

    function setUp() public {

    }

    function run() public {
        vm.startBroadcast((vm.envUint("DEPLOYER_KEY")));
        SafeCurta deployment = new SafeCurta();    
        vm.stopBroadcast();

        string memory data = "setup_data";
        string memory serialized = data.serialize("curta_puzzle", address(deployment));
        vm.writeJson(serialized, "./deployment.json");
 
    }
}

