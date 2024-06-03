import json

STARTING_LINE = "STARTING DATA"
ENDING_LINE = "ENDING DATA"

REPLACE_STRING = "[[34minfo[0m] "

FILENAME = "output.txt"

NUM_ROWS = 100
NUM_COLS = 100
NUM_ENTRIES = 4
NUM_INSTRUCTIONS = 11

def reformat(old_data):
	new_data = dict()
	for num_inst in range(1, NUM_INSTRUCTIONS + 1):
		new_data[num_inst] = [None] * NUM_ROWS
		for row in range(0, NUM_ROWS):
			new_data[num_inst][row] = [None] * NUM_COLS
			for col in range(0, NUM_COLS):
				new_data[num_inst][row][col] = [None] * NUM_ENTRIES
	
	overall_index = 0

	for row in range(0, NUM_ROWS):
		for col in range(0, NUM_COLS):
			for inst in range(1, NUM_INSTRUCTIONS + 1):
				for entry in range(0, NUM_ENTRIES):
					new_data[inst][row][col][entry] = old_data[overall_index]
					overall_index += 1

	return new_data

def print_array(formatted_data):
	for row in formatted_data:
		print_str = str()
		for col in row:
			print_str += str(col) + "\t"
		print(print_str) 


if __name__ == '__main__':
	save_lines = []
	started = False
	with open(FILENAME) as file:
		for line in file:
			cleaned_line = line.replace(REPLACE_STRING, "")
			cleaned_line = cleaned_line.strip()

			if not started and cleaned_line != STARTING_LINE:
				continue
			if cleaned_line == STARTING_LINE:
				started = True
				continue
			if cleaned_line == ENDING_LINE:
				break
			if len(cleaned_line) == 0:
				continue

			save_lines.append(cleaned_line)

	reformatted_data = reformat(save_lines)
	
	for inst in reformatted_data:
		print("Instruction", inst)
		print_array(reformatted_data[inst])