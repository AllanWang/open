s = 'What is your name'
print s[11:2]
print s[11:2:]
print s[11:2:-2]

s1 = raw_input()
index = 0
s2 = ''

while index < len(s1) -1:
	if s1[index] > s1[index+1]:
		s2 += s1[index]
	else:
		s2 = s2*2
	index += 1

print s2
