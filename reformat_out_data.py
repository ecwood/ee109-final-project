import json
import matplotlib.pyplot as plt
import matplotlib.image as mpimg

STARTING_LINE = "STARTING DATA"
ENDING_LINE = "ENDING DATA"

REPLACE_STRING = "[[34minfo[0m] "

FILENAME = "temp_output.txt"

NUM_ROWS = 1
NUM_COLS = 1
NUM_ENTRIES = 4
NUM_INSTRUCTIONS = 29

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
					new_data[inst][row][col][entry] = float(old_data[overall_index])
					overall_index += 1

	return new_data

def print_array(formatted_data):
	for row in formatted_data:
		print_str = str()
		for col in row:
			print_str += str(col) + "\t"
		print(print_str)

def make_test_array(b_val):
	SIZE = 256
	new_array = [None] * SIZE
	for row in range(0, 256):
		new_array[row] = [None] * SIZE
		for col in range(0, 256):
			new_array[row][col] = [row, col, b_val]

	return new_array

def make_test_stream():
	data_stream = list()
	for b_val in range(0, 256, 10):
		data_stream.append(make_test_array(b_val))
	return data_stream


def show_image(pixel_array):
	plt.imshow(make_test_array(100))
	plt.axis("off")
	plt.show()

def plot_images(data_stream):
	plt.ion()
	for item in data_stream:
		plt.axis("off")
		plt.imshow(item)
		plt.pause(0.01)
	
	plt.show()

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

	# plot_images(make_test_stream())