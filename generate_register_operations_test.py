NOTHING = ["0"] * 5
EMPTY_ON_NONIMM = ["0"] * 2
OPERATIONS_BITS = 5
NONIMM_OPERATIONS = {"add": 0,
			  		 "sub": 1,
			  		 "norm": 2,
			  		 "mag": 3,
			  		 "mags": 4,
			  		 "dot": 5,
			  		 "vmult": 6,
			  		 "vdiv": 7,
			  		 "sqrt": 8,
			  		 "sadd": 9,
			  		 "ssub": 10,
			  		 "smult": 11,
			  		 "sdiv": 12}

IMM_OPERATIONS = {"addi": 13,
				  "vaddi.x": 14,
			  	  "vaddi.y": 15,
			  	  "vaddi.z": 16}

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

def generate_nonimm_binary(operation, src1, src2, dest):
	binary = NOTHING + decimal_to_binary(NONIMM_OPERATIONS[operation], OPERATIONS_BITS) + EMPTY_ON_NONIMM + decimal_to_binary(src1, 4) + decimal_to_binary(src2, 4) + decimal_to_binary(dest, 4)
	return DELIM.join(binary)

def generate_imm_test(operation, imm, src2, dest):
	binary = NOTHING + decimal_to_binary(IMM_OPERATIONS[operation], OPERATIONS_BITS) + decimal_to_binary(imm, 6) + decimal_to_binary(src2, 4) + decimal_to_binary(dest, 4)
	return DELIM.join(binary)

def load_vector(x, y, z, vec):
	instructions = []

	# First, clear out any existing data by adding the x value with the 0 vector
	instructions.append(generate_imm_test("vaddi.x", x, 0, vec))
	instructions.append(generate_imm_test("vaddi.y", y, vec, vec))
	instructions.append(generate_imm_test("vaddi.z", z, vec, vec))

	return instructions

def generate_sub_tests():
	op = "sub"
	src1 = 2
	src2 = 3
	dest = 1
	instructions = []

	# Load (4, 10, 3) into vector 2
	instructions += load_vector(4, 10, 3, src2)

	# Load (6, 12, 1) into vector 3
	instructions += load_vector(6, 12, 1, src1)

	instructions.append(generate_nonimm_binary("sub", src1, src2, dest))

	return instructions

if __name__ == '__main__':
	instructions = generate_sub_tests()

	with open('unit_tests/register_operations.csv', "w+") as output_file:
		output_file.write("\n".join(instructions))
