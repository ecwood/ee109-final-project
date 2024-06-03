import math

NOTHING = ["0"]
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
			  		 "sdiv": 12,
			  		 "less": 17,
			  		 "gte": 18}

IMM_OPERATIONS = {"addi": 13,
				  "vaddi.x": 14,
			  	  "vaddi.y": 15,
			  	  "vaddi.z": 16}

DELIM = ","


# Test Vectors
ZERO_REG = 0

def decimal_to_binary(dec, bits):
	binary = ["0"] * bits

	curr_val = dec

	for x in range(bits - 1, -1, -1):
		power = pow(2, x)
		div = int(curr_val / power)
		binary[bits - (x + 1)] = str(div)
		curr_val -= div * power

	return binary

def nonimm(operation, src1, src2, dest, comp=0):
	binary = NOTHING + decimal_to_binary(comp, 4) + decimal_to_binary(NONIMM_OPERATIONS[operation], OPERATIONS_BITS) + EMPTY_ON_NONIMM + decimal_to_binary(src1, 4) + decimal_to_binary(src2, 4) + decimal_to_binary(dest, 4)
	return DELIM.join(binary)

def imm(operation, imm, src2, dest, comp=0):
	binary = NOTHING + decimal_to_binary(comp, 4) + decimal_to_binary(IMM_OPERATIONS[operation], OPERATIONS_BITS) + decimal_to_binary(imm, 6) + decimal_to_binary(src2, 4) + decimal_to_binary(dest, 4)
	return DELIM.join(binary)

def load_vector(vec, reg):
	instructions = []
	(x, y, z) = vec

	# First, clear out any existing data
	instructions.append(nonimm("add", 0, 0, reg))
	if x < 0:
		instructions.append(imm("vaddi.x", -1 * x, reg, reg))
	if y < 0:
		instructions.append(imm("vaddi.y", -1 * y, reg, reg))
	if z < 0:
		instructions.append(imm("vaddi.z", -1 * z, reg, reg))
	instructions.append(nonimm("sub", 0, reg, reg))

	if x > 0:
		instructions.append(imm("vaddi.x", x, reg, reg))
	if y > 0:
		instructions.append(imm("vaddi.y", y, reg, reg))
	if z > 0:
		instructions.append(imm("vaddi.z", z, reg, reg))

	return instructions

def load_vector_normal(vec, reg):
	instructions = load_vector(vec, reg)
	instructions.append(nonimm("norm", reg, reg, reg))

	return instructions

def load_scalar(sca, reg):
	instructions = []

	if sca < 0:
		instructions.append(nonimm("sadd", 0, 0, reg))
		instructions.append(imm("addi", -1 * sca, reg, reg))
		instructions.append(nonimm("ssub", 0, reg, reg))
	else:
		instructions.append(imm("addi", sca, 0, reg))

	return instructions

def sub_vec(vec_a, vec_b):
	(a_x, a_y, a_z) = vec_a
	(b_x, b_y, b_z) = vec_b

	return (a_x - b_x, a_y - b_y, a_z - b_z)

def dot_vec(vec_a, vec_b):
	(a_x, a_y, a_z) = vec_a
	(b_x, b_y, b_z) = vec_b

	return a_x * b_x + a_y * b_y + a_z * b_z

def norm_vec(vec):
	(x, y, z) = vec
	mag = math.sqrt(pow(x, 2) + pow(y, 2) + pow(z, 2))

	return (x / mag, y / mag, z / mag)

def vmult(vec, sca):
	(x, y, z) = vec

	return (x * sca, y * sca, z * sca)

def expected_ray_hit_sphere(ray_origin, ray_direction, sphere_pos, sphere_rad):
	p = sub_vec(sphere_pos, ray_origin)
	d = ray_direction

	dp = dot_vec(d, p)
	pp = dot_vec(p, p)

	inner = dp * dp - (pp - sphere_rad * sphere_rad)
	print("p:", p)
	print("d:", d)
	print("dp:", dp)
	print("dp * dp:", dp * dp)
	print("pp - sphere_rad * sphere_rad:", pp - sphere_rad * sphere_rad)
	print("inner:", inner)

	if inner < 0:
		t = -1
		normal = norm_vec((1, 1, 1))
	else:
		t = dp - math.sqrt(inner)
		normal = norm_vec(sub_vec(vmult(d, t), p))

	return normal, t, inner < 0


def ray_hit_sphere(in_vreg_ray_origin,
				   in_vreg_ray_dir,
				   in_vreg_sphere_pos,
				   in_sreg_sphere_rad,
				   out_vreg_norm_vec,
				   out_sreg_t,
				   out_sreg_inner_valid,
				   free_vregs,
				   free_sregs):
	instructions = []
	assert(len(free_vregs) >= 2)
	assert(len(free_sregs) >= 4)

	[tmp_vreg_1, tmp_vreg_2] = free_vregs[0:2]
	[tmp_sreg_1, tmp_sreg_2, tmp_sreg_3, tmp_sreg_4] = free_sregs[0:4]


	# p = s.pos - r.pos => tmp_vreg_1 = in_vreg_sphere_pos - in_vreg_ray_dir
	instructions.append(nonimm("sub", in_vreg_sphere_pos, in_vreg_ray_origin, tmp_vreg_1))

	# dp = dot(d, p) => tmp_sreg_1 = in_vreg_ray_dir * tmp_vreg_1
	instructions.append(nonimm("dot", in_vreg_ray_dir, tmp_vreg_1, tmp_sreg_1))

	# pp = dot(p, p) => tmp_sreg_2 = tmp_vreg_1 * tmp_vreg_1
	instructions.append(nonimm("dot", tmp_vreg_1, tmp_vreg_1, tmp_sreg_2))

	# s.radius * s.radius -> tmp_sreg_3 = in_sreg_sphere_rad * in_sreg_sphere_rad
	instructions.append(nonimm("smult", in_sreg_sphere_rad, in_sreg_sphere_rad, tmp_sreg_3))

	# pp - s.radius * s.radius => tmp_sreg_3 = tmp_sreg_2 - tmp_sreg_3
	instructions.append(nonimm("ssub", tmp_sreg_2, tmp_sreg_3, tmp_sreg_3))

	# dp * dp => tmp_sreg_4 = tmp_sreg_1 * tmp_sreg_1
	instructions.append(nonimm("smult", tmp_sreg_1, tmp_sreg_1, tmp_sreg_4))

	# inner = dp * dp - (pp - s.radius * s.radius) => tmp_sreg_4 = tmp_sreg_4 - tmp_sreg_3
	instructions.append(nonimm("ssub", tmp_sreg_4, tmp_sreg_3, tmp_sreg_4))

	# compare inner against 0 => out_sreg_inner_valid = tmp_sreg_4 >= ZERO_REG
	instructions.append(nonimm("gte", tmp_sreg_4, ZERO_REG, out_sreg_inner_valid))

	# sqrt(inner) => tmp_sreg_4 = sqrt(tmp_sreg_4)
	instructions.append(nonimm("sqrt", ZERO_REG, tmp_sreg_4, tmp_sreg_4, out_sreg_inner_valid))

	# t = dp - sqrt(inner) => out_sreg_t = tmp_sreg_1 - tmp_sreg_4
	instructions.append(nonimm("ssub", tmp_sreg_1, tmp_sreg_4, out_sreg_t, out_sreg_inner_valid))

	# t * d => tmp_vreg_2 = in_vreg_ray_dir * out_sreg_t
	instructions.append(nonimm("vmult", in_vreg_ray_dir, out_sreg_t, tmp_vreg_2, out_sreg_inner_valid))

	# t * d - p => tmp_vreg_1 = tmp_vreg_2 - tmp_vreg_1
	instructions.append(nonimm("sub", tmp_vreg_2, tmp_vreg_1, tmp_vreg_1, out_sreg_inner_valid))

	# normal = normalize(t * d - p) => out_vreg_norm_vec = normalize(tmp_vreg_1)
	instructions.append(nonimm("norm", tmp_vreg_1, tmp_vreg_1, out_vreg_norm_vec, out_sreg_inner_valid))

	return instructions

def test_ray_hit_sphere():
	vreg_ray_origin = 2
	vreg_ray_dir = 3
	vreg_sphere_pos = 4
	sreg_sphere_rad = 2
	vreg_norm_vec = 1
	sreg_t = 1
	sreg_inner_valid = 3
	free_vregs = [5, 6]
	free_sregs = [4, 5, 6, 7]

	ray_origin = (4, 30, 10)
	ray_direction = (4, 4, 2)
	ray_direction_normal = (4 / 6, 4 / 6, 2 / 6)
	sphere_pos = (3, -9, 10)
	sphere_rad = 20

	norm_vec, t, inner_invalid = expected_ray_hit_sphere(ray_origin, ray_direction_normal, sphere_pos, sphere_rad)

	print("Expected: norm_vec = " + str(norm_vec) + "; t = " + str(t) + "; inner invalid: " + str(inner_invalid))

	instructions = []
	instructions += load_vector(ray_origin, vreg_ray_origin)
	instructions += load_vector_normal(ray_direction, vreg_ray_dir)
	instructions += load_vector(sphere_pos, vreg_sphere_pos)
	instructions += load_scalar(sphere_rad, sreg_sphere_rad)

	instructions += ray_hit_sphere(vreg_ray_origin,
								   vreg_ray_dir,
								   vreg_sphere_pos,
								   sreg_sphere_rad,
								   vreg_norm_vec,
								   sreg_t,
								   sreg_inner_valid,
								   free_vregs,
								   free_sregs)
	return instructions


if __name__ == '__main__':
	instructions = test_ray_hit_sphere()

	with open('unit_tests/shader.csv', "w+") as output_file:
		output_file.write("\n".join(instructions))
