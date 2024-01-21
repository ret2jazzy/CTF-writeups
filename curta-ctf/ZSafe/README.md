# ZSafe

The inspiration behind this challenge for [curta](https://curta.wtf/) was I really wanted to make a challenge involving CREATE2 and metamorphic deployments. 

The first time I learned about CREATE2, I was fascinated by all the possibilities, and I had countless ideas throughout the years where I tried shoehorning CREATE2. This was the only one that seemed cool enough to make a challenge out of it.

This write-up is going to be relatively brief, so please read the other two writeups by other players. They're much more detailed and tackle it from a solvers perspective.

- [write-up](https://philogy.github.io/posts/curta-zsafe-writeup/) by [Philogy](https://twitter.com/real_philogy)
- [write-up](https://hackmd.io/@kjsman/curta-zsafe) by [Jinseo](https://twitter.com/csapp3e)

## Part 0 - What is the challenge?

The challenge involves two main parts: A proxy and some ECDSA.

The challenge deploys `SafeProxy`, which is a ripped implementation from OpenZeppelin's UUPS proxy. Why? because we only want a subset of UUPS functionality

The proxy allows upgrades by owners, but there's a whitelist of codehash-es you can upgrade to.

The main challenge contract reads a few values off the proxy, performs magic transformations of the read values, and then tries to interpret it as an ECDSA signature.

## Part 1 - The Proxy

Why do we have a proxy in the first place? Because I want to abuse proxies with CREATE2 deployments.

```solidity
    function upgradeToAndCall(address newImplementation, bytes memory data) public payable virtual onlyProxy {
        _authorizeUpgrade(newImplementation);
        _upgradeToAndCallSafe(newImplementation, data);
    }

    function _checkProxy() internal view virtual {
        if (
            address(this) == __self ||
            ERC1967Utils.getImplementation() != __self
        ) {
            revert("No hacc");
        }
    }

    function _authorizeUpgrade(address newImplementation) internal {
        require(whitelist[newImplementation.codehash], "wtf no whitelisted no hacc pls");
    }
```

The `_authorizeUpgrade` requires the codehash of the new implementation to be whitelisted. 

What do we have whitelisted in here? `SafeSecret` and `SafeSecretAdmin`

So we can upgrade to any `SafeSecret` and `SafeSecretAdmin` implementations. What does that give us?
o
The way UUPS is supposed to work, the upgrade logic is part of the implementation itself. 

If we can upgrade to an implementation, somehow `SELFDESTRUCT` it and then re-initialize it with a new bytecode, we should be able to execute arbitrary bytecode.

```solidity
    function _checkProxy() internal view virtual {
        if (
            address(this) == __self ||
            ERC1967Utils.getImplementation() != __self
        ) {
            revert("No hacc");
        }
    }

```

We do have this pesky `_checkProxy` check tho. This intuitively only allows us to call `_upgradeToAndCall` only through a proxy address. 

But looking deeper, `__self` is an immutable variable, which means it's part of the bytecode (and by association, codehash) itself. 

This means if we redeploy the exact same bytecode, we can bypass the first check.

The second check can also be bypassed if we set the storage slots during deployment to the correct implementation. Now you can have arbitrary bytecode executing during the proxy execution.

The rest is just implenting it. Check out the solve script in [scripts/](./scripts/) for more details

## Part 2 - ECDSA 

The challenge till now is just a general CTF challenge. But the problem is that it's easily copy-able once the exploit is public.

Since this is for curta, the exploits will be public. So we have to add a generative aspect to it. This is where the ECDSA comes in.

The crux of the challenge is you have to generate a signature where the `s` value is a `keccack` hash. This means you can't directly control it.

It is also dependent on your `seed` that comes from curta, so everyone has a different `s` value they need to use as a part of their signature.

```solidity
    function check(bytes32 _r, bytes32 _s) internal {
        uint8 v = 27;
        address owner = proxy.owner();

        //--------

        bytes32 message1_hash = keccak256(abi.encodePacked(seed, address(0xdead)));
        bytes32 r1 = transform_r1(_r);
        bytes32 s1 = transform_s1(_s);

        address signer = ecrecover(message1_hash, v, r1, s1);
        require(signer != address(0), "no sig match :<");
        require(signer == owner, "no owner match :<");

        //---------

        bytes32 message2_hash = keccak256(abi.encodePacked(seed, address(0xbeef)));
        bytes32 r2 = transform_r2(_r);
        bytes32 s2 = transform_s2(_s);

        address signer2 = ecrecover(message2_hash, v, r2, s2);
        require(signer2 != address(0), "no sig match :<");
        require(signer2 == owner, "no owner match :<");

        //--------

    }

    function transform_r1(bytes32 r) internal pure returns (bytes32) {
        return r;
    }

    function transform_s1(bytes32 s) internal view returns (bytes32) {
        return bytes32(uint256(s) ^ proxy.p2());
    }

    function transform_r2(bytes32 r) internal view returns (bytes32) {
        unchecked{
            return bytes32(uint256(r) + proxy.p1());
        }
    }

    function transform_s2(bytes32 s) internal view returns (bytes32) {
        return keccak256(abi.encodePacked(uint256(s) ^ proxy.p2(), seed));
    }
```

At first glance, it may not seem possible as `s` value is depends on the private key and `r` value. 

But, we can calculate a private key that will correspond to the `s` and `r` value pair. 

Then all we need to do is sign another message with the same private key, and we're done.

There's also a small part where you need to change the `p1`, `p2` and `owner` values in the middle of the calls. But we're doing `STATICCALL`, so we need to use the warm slot trick to return different values per call.

For the final exploit, check out `deploy.sh` that deploys the challenge locally and `run.sh` that runs the exploit.

