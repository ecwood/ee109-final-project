NOTHING = ["0"] * 7
A = ["0"]
OO = ["0", "0"]
GG_ADD = ["0", "0"]
GG_VADDI_X = ["0", "1"]
GG_VADDI_Y = ["1", "0"]
GG_VADDI_Z = ["1", "1"]

DELIM = ","

def decimal_to_binary(dec, bits):
	binary = ["0"] * bits

	curr_val = dec

	for x in range(bits - 1, -1, -1):
		power = pow(2, x)
		div = int(curr_val / power)
		binary[bits - (x + 1)] = str(div)
		curr_val -= div * power

	return binary

def generate_add_test(src1, src2, dest):
	binary = NOTHING + A + GG_ADD + OO + decimal_to_binary(src1, 4) + decimal_to_binary(src2, 4) + decimal_to_binary(dest, 4)
	return DELIM.join(binary)

def generate_vadd_test(gg, imm, src2, dest):
	binary = NOTHING + A + decimal_to_binary(gg, 2) + decimal_to_binary(imm, 6) + decimal_to_binary(src2, 4) + decimal_to_binary(dest, 4)
	return DELIM.join(binary)

if __name__ == '__main__':
	lines = []

	lines.append(generate_vadd_test(1, 3, 0, 1)) # Put 3 in vector 1 x
	lines.append(generate_vadd_test(2, 4, 1, 1)) # Put 4 in vector 1 y
	lines.append(generate_vadd_test(3, 9, 1, 1)) # Put 9 in vector 1 z

	lines.append(generate_add_test(1, 1, 1)) # Double vector 1

	with open('unit_tests/add_registers.csv', "w+") as output_file:
		output_file.write("\n".join(lines))
