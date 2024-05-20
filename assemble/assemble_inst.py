import argparse

KEY_ADD = "add"
KEY_SUBTRACT = "sub"
KEY_NORMALIZE = "normalize"
KEY_MAGNITUDE = "magnitude"
KEY_MAGNITUDE_SQUARED = "mag_s"
KEY_DOT_PRODUCT = "dot"
KEY_SCALAR_MULTIPY = "mult_s"
KEY_SCALAR_DIVIDE = "div_s"
KEY_SQUARE_ROOT = "sqrt"

VVV_BIN = "000"
VVS_BIN = "001"
VSV_BIN = "010"
VSS_BIN = "011"

keys_to_binary = {KEY_ADD: VVV_BIN + "00",
				  KEY_SUBTRACT: VVV_BIN + "01",
				  KEY_NORMALIZE: VVV_BIN + "10",
				  KEY_MAGNITUDE: VVS_BIN + "00",
				  KEY_MAGNITUDE_SQUARED: VVS_BIN + "01",
				  KEY_DOT_PRODUCT: VVS_BIN + "10",
				  KEY_SCALAR_MULTIPY: VSV_BIN + "00",
				  KEY_SCALAR_DIVIDE: VSV_BIN + "01",
				  KEY_SQUARE_ROOT: VSS_BIN + "00"}

registers_to_binary = {"$0": "0000",
					   "$1": "0001",
					   "$2": "0010",
					   "$3": "0011",
					   "$4": "0100",
					   "$5": "0101",
					   "$6": "0110",
					   "$7": "0111",
					   "$8": "1000",
					   "$9": "1001",
					   "$10": "1010",
					   "$11": "1011",
					   "$12": "1100",
					   "$13": "1101",
					   "$14": "1110",
					   "$15": "1111"}

def get_index(line, index, default):
	if len(line) > index:
		return line[index]

	return default


def translate(line):
	components = line.split()

	key = get_index(components, 0, "")
	src1 = get_index(components, 1, "$0")
	src2 = get_index(components, 2, "$0")
	dest = get_index(components, 3, "$0")

	key_binary = keys_to_binary[key]
	src1_binary = registers_to_binary[src1]
	src2_binary = registers_to_binary[src2]
	dest_binary = registers_to_binary[dest]

	return key_binary + src1_binary + src2_binary + dest_binary


def file_args():
	arg_parser = argparse.ArgumentParser()
	arg_parser.add_argument('inputFile', type=str)
	arg_parser.add_argument('outputFile', type=str)

	return arg_parser.parse_args()

if __name__ == '__main__':
	args = file_args()

	input_file_name = args.inputFile
	output_file_name = args.outputFile

	assembled_inst = list()

	with open(input_file_name) as input_file:
		for line in input_file:
			assembled_inst.append(translate(line))

	with open(output_file_name) as output_file:
		output_file.write("\n".join(assembled_inst))