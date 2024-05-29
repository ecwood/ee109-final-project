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


# Test Vectors
vec1 = (4, 10, 3)
vec2 = (6, 12, 1)
vec_easy_norm = (4, 2, 4)
vec3 = (7, 3, 10)
vec4 = (8, 15, 4)
vec5 = (13, 9, 2)
vec_zero = (0, 0, 0)

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

def load_vector(vec, reg):
	instructions = []
	(x, y, z) = vec

	# First, clear out any existing data by adding the x value with the 0 register
	instructions.append(generate_imm_test("vaddi.x", x, 0, reg))
	instructions.append(generate_imm_test("vaddi.y", y, reg, reg))
	instructions.append(generate_imm_test("vaddi.z", z, reg, reg))

	return instructions

def expected_sub(a, b):
	(a_x, a_y, a_z) = a
	(b_x, b_y, b_z) = b

	return (a_x - b_x, a_y - b_y, a_z - b_z)

def expected_dot(a, b):
	(a_x, a_y, a_z) = a
	(b_x, b_y, b_z) = b

	return a_x * b_x + a_y * b_y + a_z * b_z

def generate_sub_tests():
	op = "sub"
	src1 = 2
	src2 = 3
	dest = 1
	instructions = []

	# Load (4, 10, 3) into vector 2
	instructions += load_vector(vec1, src2)

	# Load (6, 12, 1) into vector 3
	instructions += load_vector(vec2, src1)

	instructions.append(generate_nonimm_binary("sub", src1, src2, dest))

	# Load (0, 0, 0) into vector 2
	instructions += load_vector(vec_zero, src2)

	# Load (0, 0, 0) into vector 3
	instructions += load_vector(vec_zero, src1)

	instructions.append(generate_nonimm_binary("sub", src1, src2, dest))

	return instructions

def generate_norm_tests():
	op = "norm"
	src = 2
	dest = 1
	instructions = []

	# First, we'll test with an example that comes out evenly
	instructions += load_vector(vec_easy_norm, src)

	instructions.append(generate_nonimm_binary("norm", 0, src, dest))

	# Now, we'll test with an example that comes out evenly
	instructions += load_vector(vec3, src)

	instructions.append(generate_nonimm_binary("norm", 0, src, dest))

	return instructions

def generate_dot_tests():
	op = "dot"
	src1 = 2
	src2 = 3
	dest = 1
	instructions = []

	# Load (8, 15, 4) into vector 2
	instructions += load_vector(vec4, src1)

	instructions += load_vector(vec5, src2)

	instructions.append(generate_nonimm_binary("dot", src1, src2, dest))

	print("Expected Value:", expected_dot(vec4, vec5))

	return instructions

if __name__ == '__main__':
	instructions = generate_sub_tests()

	with open('unit_tests/register_operations.csv', "w+") as output_file:
		output_file.write("\n".join(instructions))
