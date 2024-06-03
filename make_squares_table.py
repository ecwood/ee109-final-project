import json

MAX_ROOT = 64
MAX_SQUARE = pow(MAX_ROOT, 2)

def generate_squares():
	squares = dict()
	for x in range(0, MAX_ROOT + 1):
		squares[x] = pow(x, 2)

	return squares

def generate_closest_matches(squares):
	closest_matches = {0: 1, 1: 1}

	for x in range(2, MAX_SQUARE + 1):
		for root in range(MAX_ROOT, 0, -1):
			square = squares[root]
			if x >= square:
				closest_matches[x] = root
				break

	return closest_matches


if __name__ == '__main__':
	squares = generate_squares()
	closest_matches = generate_closest_matches(squares)

	for match in closest_matches:
		print(str(match) + "," + str(closest_matches[match]))