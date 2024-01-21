import ecdsa
from ecpy.curves import Curve
from ecdsa.util import sigencode_string, sigdecode_string
from ecdsa.numbertheory import inverse_mod
from eth_account import Account
from sha3 import keccak_256
from eth_keys import KeyAPI, keys
import random
import sys
import json


def eth_addr_from_pkey(pkey):
    cv     = Curve.get_curve('secp256k1')
    pu_key = pkey * cv.generator # just multiplying the private key by generator point (EC multiplication)

    concat_x_y = pu_key.x.to_bytes(32, byteorder='big') + pu_key.y.to_bytes(32, byteorder='big')
    eth_addr = '0x' + keccak_256(concat_x_y).digest()[-20:].hex()
    return eth_addr

def recover_account_from_rs(msghash, r,s):
    vrs = (0,r,s)
    signature = KeyAPI.Signature(vrs=vrs)
    return signature.recover_public_key_from_msg_hash(msghash).to_address();

def msghash_and_rvs_pkey_recover(msghash, r, s, k, n):
    return ((((s*k)%n - msghash)%n) * (inverse_mod(r, n)))%n;


def msghash_attack(_msghash, s):
    msghash = bytes.fromhex(_msghash)
    msghash_int = int(_msghash, 16)
    cv = Curve.get_curve('secp256k1')
    while True:
        k = random.randint(0, 0xffffffffffffffff) 
        r = (k * cv.generator).x % cv.order
        privkey = msghash_and_rvs_pkey_recover(msghash_int, r, s, k, cv.order)
        eth_addr = eth_addr_from_pkey(privkey)
        recovered_addr = recover_account_from_rs(msghash, r, s)
        if eth_addr == recovered_addr:
            return (eth_addr, r, s, privkey)

def sign_hash(_msghash, privkey):
    pkey = keys.PrivateKey(privkey.to_bytes(32, byteorder='big'))
    while True:
        signed_deets = pkey.sign_msg_hash(bytes.fromhex(_msghash))
        return signed_deets


def full_sploit(s, msghash, msghash_to_sign):
    while True:
        addr, r1, s1, privkey = msghash_attack(msghash, s)
        (v, r2, s2) = sign_hash(msghash_to_sign, privkey).vrs
        if v == 1:
            continue
        return {"r2": r1,"s2": s1, "r1": r2,"s1": s2, "owner":addr}

if __name__ == '__main__':
    solved_challs = []

    for i in range(3):
        s = int(sys.argv[i*4+1], 16)
        s_orig = int(sys.argv[i*4+2], 16)
        msghash = sys.argv[i*4+3][2:]
        msghash_to_sign = sys.argv[i*4+4][2:]
        sol = full_sploit(s, msghash, msghash_to_sign)
        sol['orig_s'] = s_orig
        solved_challs.append(sol)
    solved_challs.sort(key=lambda x:x['r1'])
    for idx, solved in enumerate(solved_challs):
        with open(f"./solved.{idx}.json", "w") as f:
            json.dump(solved, f)

