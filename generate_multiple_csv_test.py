import json

BITS = 24
DELIM = ","

def BinaryToDecimal(binary):
	if type(binary) == str:
		binary = binary.split(DELIM)
	decimal = 0
	for x in range(0, len(binary)):
		decimal += int(binary[x]) * pow(2, len(binary) - x - 1)
	return decimal

def BinaryToInstruction(binary):
	binary = binary.split(DELIM)
	nothing = binary[0:7]
	a = BinaryToDecimal(binary[7])
	gg = BinaryToDecimal(binary[8:10])
	oo = BinaryToDecimal(binary[10:12])
	src1 = BinaryToDecimal(binary[12:16])
	src2 = BinaryToDecimal(binary[16:20])
	dest = BinaryToDecimal(binary[20:24])

	return {"a": str(a), "gg": str(gg), "oo": str(oo), "src1": str(src1), "src2": str(src2), "dest": str(dest)}


def DecimalToBinary(dec):
	binary = ["0"] * BITS

	curr_val = dec

	for x in range(BITS - 1, -1, -1):
		power = pow(2, x)
		div = int(curr_val / power)
		binary[BITS - (x + 1)] = str(div)
		curr_val -= div * power

	return DELIM.join(binary)

def ListToArray(answer_list):
	return "Array[Int] (" + ",".join(answer_list) + ")"

if __name__ == '__main__':
	instructions = dict()
	binaries = list()
	src1s = list()
	for x in range(0, pow(2, BITS - 7), 1000):
		binary = DecimalToBinary(x)
		decimal = BinaryToDecimal(binary)
		assert(decimal == x)
		binaries.append(DecimalToBinary(x))
		instructions[x] = BinaryToInstruction(binary)
		src1s.append(instructions[x]["src1"])

	with open('unit_tests/load_csv_test_multiple.csv', "w") as output_file:
		output_file.write("\n".join(binaries))


	with open('unit_tests/load_csv_test_multiple_src1.txt', "w+") as output_file:
		output_file.write(ListToArray(src1s))