import math

NOTHING = ["0"] * 3
EMPTY_ON_NONIMM_BITS = 4
EMPTY_ON_NONIMM = ["0"] * EMPTY_ON_NONIMM_BITS
OPERATIONS_BITS = 5
REGISTER_BITS = 5
IMM_BITS = REGISTER_BITS + EMPTY_ON_NONIMM_BITS
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

ZERO_REG = "zero"

BACKGROUND_COLOR = (2, 2, 2) # Gray
UNSEEN_COLOR = (0, 0, 0) # Black
BLUE_COLOR = (0, 0, 4) # Blue
RED_COLOR = (4, 0, 0)

PIXEL_ROWS = 50
PIXEL_COLS = 50

VECTOR_REGISTERS = {ZERO_REG: 0,
					"out_color": 1,
					"frag_coor": 2,
					"sphere_pos": 3,
					"sphere_color": 4,
					"uv": 5,
					"input_ray_pos": 6,
					"input_ray_dir": 7,
					"closest_info.color": 8,
					"closest_info.normal": 9,
					"closest_info.ray.pos": 10,
					"closest_info.ray.dir": 11,
					"light_pos": 12,
					"hit_pos": 13,
					"light_dir": 14,
					"light_ray.pos": 15,
					"light_ray.dir": 16,
					"light_info.color": 17,
					"light_info.normal": 18,
					"light_info.ray.pos": 19,
					"light_info.ray.dir": 20,
					"trace_ray_return": 21,
					"sphere_info.color": 22,
					"sphere_info.normal": 23,
					"sphere_info.ray.pos": 24,
					"sphere_info.ray.dir": 25,
					"ray_hit_sphere_p": 26,
					"vtmp1": 27,
					"vtmp2": 28,
					"vtmp3": 29,
					"vtmp4": 30,
					"vtmp5": 31}

SCALAR_REGISTERS = {ZERO_REG: 0,
					"sphere_rad": 1,
					"closest_info.t": 2,
					"light_info.t": 3,
					"sphere_info.t": 4,
					"ray_hit_sphere_valid": 5,
					"shoot_ray_updated": 6,
					"ray_hit_sphere_dp": 7,
					"ray_hit_sphere_dd": 8,
					"ray_hit_sphere_pp": 9,
					"ray_hit_sphere_inner": 10,
					"ray_hit_sphere_t": 11,
					"stmp1": 12,
					"stmp2": 13,
					"stmp3": 14,
					"stmp4": 15,
					"stmp5": 16,
					"stmp6": 17,
					"stmp7": 18,
					"stmp8": 19,
					"stmp9": 20,
					"stmp10": 21,
					"stmp11": 22,
					"stmp12": 23,
					"stmp13": 24,
					"stmp14": 25,
					"stmp15": 26,
					"stmp16": 27,
					"stmp17": 28,
					"stmp18": 29,
					"stmp19": 30,
					"stmp20": 31}

SPHERE = ("sphere_pos", "sphere_color", "sphere_rad")
SPHERE2 = ("vtmp4", "vtmp5", "stmp20")
INPUT_RAY = ("input_ray_pos", "input_ray_dir")
LIGHT_RAY = ("light_ray.pos", "light_ray.dir")
CLOSEST_INFO = ("closest_info.color", "closest_info.normal", "closest_info.ray.pos", "closest_info.ray.dir", "closest_info.t")
LIGHT_INFO = ("light_info.color", "light_info.normal", "light_info.ray.pos", "light_info.ray.dir", "light_info.t")
SPHERE_INFO = ("sphere_info.color", "sphere_info.normal", "sphere_info.ray.pos", "sphere_info.ray.dir", "sphere_info.t")

def decimal_to_binary(dec, bits):
	binary = ["0"] * bits

	curr_val = dec

	for x in range(bits - 1, -1, -1):
		power = pow(2, x)
		div = int(curr_val / power)
		binary[bits - (x + 1)] = str(div)
		curr_val -= div * power

	return binary

def binary_to_decimal(bits):
	val = 0
	length = len(bits)
	for x in range(length):
		multiplier = pow(2, length - 1 - x)
		val += int(bits[x] == "1") * multiplier
	return val


def decode_instruction(inst_str):
	inst = inst_str.split(DELIM)
	comp = binary_to_decimal(inst[3:8])
	op = binary_to_decimal(inst[8:13])
	src1 = binary_to_decimal(inst[17:22])
	src2 = binary_to_decimal(inst[22:27])
	dest = binary_to_decimal(inst[27:31])
	immediate = binary_to_decimal(inst[13:22])

	return {"comp": comp, "op": op, "src1": src1, "src2": src2, "dest": dest, "immediate": immediate}

def nonimm(operation, src1, src2, dest, comp=0):
	nothing = NOTHING
	comp_bits = decimal_to_binary(comp, REGISTER_BITS)
	op_bits = decimal_to_binary(NONIMM_OPERATIONS[operation], OPERATIONS_BITS)
	src1_bits = decimal_to_binary(src1, REGISTER_BITS)
	src2_bits = decimal_to_binary(src2, REGISTER_BITS)
	dest_bits = decimal_to_binary(dest, REGISTER_BITS)
	binary = NOTHING + comp_bits + op_bits + EMPTY_ON_NONIMM + src1_bits + src2_bits + dest_bits
	return DELIM.join(binary)

def imm(operation, imm_val, src2, dest, comp=0):
	nothing = NOTHING
	comp_bits = decimal_to_binary(comp, REGISTER_BITS)
	op_bits = decimal_to_binary(IMM_OPERATIONS[operation], OPERATIONS_BITS)
	imm_bits = decimal_to_binary(imm_val, IMM_BITS)
	src2_bits = decimal_to_binary(src2, REGISTER_BITS)
	dest_bits = decimal_to_binary(dest, REGISTER_BITS)
	binary = nothing + comp_bits + op_bits + imm_bits + src2_bits + dest_bits
	return DELIM.join(binary)

def add(src1, src2, dest, comp=ZERO_REG):
	op = "add"
	src1_reg = VECTOR_REGISTERS[src1]
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def sub(src1, src2, dest, comp=ZERO_REG):
	op = "sub"
	src1_reg = VECTOR_REGISTERS[src1]
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def norm(src2, dest, comp=ZERO_REG):
	op = "norm"
	src1_reg = SCALAR_REGISTERS[ZERO_REG]
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def mag(src2, dest, comp=ZERO_REG):
	op = "mag"
	src1_reg = SCALAR_REGISTERS[ZERO_REG]
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def mags(src2, dest, comp=ZERO_REG):
	op = "mags"
	src1_reg = SCALAR_REGISTERS[ZERO_REG]
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def dot(src1, src2, dest, comp=ZERO_REG):
	op = "dot"
	src1_reg = VECTOR_REGISTERS[src1]
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def vmult(src1, src2, dest, comp=ZERO_REG):
	op = "vmult"
	src1_reg = VECTOR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def vdiv(src1, src2, dest, comp=ZERO_REG):
	op = "vdiv"
	src1_reg = VECTOR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def sqrt(src2, dest, comp=ZERO_REG):
	op = "sqrt"
	src1_reg = SCALAR_REGISTERS[ZERO_REG]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def sadd(src1, src2, dest, comp=ZERO_REG):
	op = "sadd"
	src1_reg = SCALAR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def ssub(src1, src2, dest, comp=ZERO_REG):
	op = "ssub"
	src1_reg = SCALAR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def smult(src1, src2, dest, comp=ZERO_REG):
	op = "smult"
	src1_reg = SCALAR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def sdiv(src1, src2, dest, comp=ZERO_REG):
	op = "sdiv"
	src1_reg = SCALAR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def less(src1, src2, dest, comp=ZERO_REG):
	op = "less"
	src1_reg = SCALAR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def gte(src1, src2, dest, comp=ZERO_REG):
	op = "gte"
	src1_reg = SCALAR_REGISTERS[src1]
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [nonimm(op, src1_reg, src2_reg, dest_reg, comp_reg)]

def addi(imm_val, src2, dest, comp=ZERO_REG):
	op = "addi"
	src2_reg = SCALAR_REGISTERS[src2]
	dest_reg = SCALAR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [imm(op, imm_val, src2_reg, dest_reg, comp_reg)]

def vaddix(imm_val, src2, dest, comp=ZERO_REG):
	op = "vaddi.x"
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [imm(op, imm_val, src2_reg, dest_reg, comp_reg)]

def vaddiy(imm_val, src2, dest, comp=ZERO_REG):
	op = "vaddi.y"
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [imm(op, imm_val, src2_reg, dest_reg, comp_reg)]

def vaddiz(imm_val, src2, dest, comp=ZERO_REG):
	op = "vaddi.z"
	src2_reg = VECTOR_REGISTERS[src2]
	dest_reg = VECTOR_REGISTERS[dest]
	comp_reg = SCALAR_REGISTERS[comp]

	return [imm(op, imm_val, src2_reg, dest_reg, comp_reg)]

def load_vector(vec, reg, comp=ZERO_REG):
	instructions = []
	(x, y, z) = vec

	# First, clear out any existing data
	instructions += add(ZERO_REG, ZERO_REG, reg, comp)

	# Put the negative values in
	if x < 0:
		instructions += vaddix(-1 * x, reg, reg, comp)
	if y < 0:
		instructions += vaddiy(-1 * y, reg, reg, comp)
	if z < 0:
		instructions += vaddiz(-1 * z, reg, reg, comp)

	# Make them negative
	instructions += sub(ZERO_REG, reg, reg, comp)

	if x > 0:
		instructions += vaddix(x, reg, reg, comp)
	if y > 0:
		instructions += vaddiy(y, reg, reg, comp)
	if z > 0:
		instructions += vaddiz(z, reg, reg, comp)

	return instructions

def load_vector_normal(vec, reg):
	instructions = load_vector(vec, reg)
	instructions += norm(reg, reg)

	return instructions

def load_ray(ray, ray_regs):
	instructions = []

	(ray_pos, ray_dir) = ray
	(ray_pos_vreg, ray_dir_vreg) = ray_regs

	instructions += load_vector(ray_pos, ray_pos_vreg)
	instructions += load_vector_normal(ray_dir, ray_dir_vreg)

	return instructions

def load_sphere(sphere, sphere_regs):
	instructions = []

	(sphere_pos, sphere_color, sphere_rad) = sphere
	(sphere_pos_vreg, sphere_color_vreg, sphere_rad_sreg) = sphere_regs

	instructions += load_vector(sphere_pos, sphere_pos_vreg)
	instructions += load_vector(sphere_color, sphere_color_vreg)
	instructions += load_scalar(sphere_rad, sphere_rad_sreg)

	return instructions

def load_scalar(sca, reg):
	instructions = []

	if sca < 0:
		instructions += sadd(ZERO_REG, ZERO_REG, reg)
		instructions += addi(-1 * sca, reg, reg)
		instructions += ssub(ZERO_REG, reg, reg)
	else:
		instructions += addi(sca, ZERO_REG, reg)

	return instructions

def load_inf(reg):
	instructions = []

	# max_imm = pow(2, IMM_BITS) - 1
	max_imm = 256

	instructions += addi(max_imm, ZERO_REG, reg) # reg = 511
	# instructions += smult(reg, reg, reg) # reg = 261121
	# instructions += sadd(reg, reg, reg) # reg = 522242
	# instructions += sadd(reg, reg, reg) # reg = 1044484
	# instructions += sadd(reg, reg, reg) # reg = 2088968

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

def vmult_vec(vec, sca):
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
		normal = norm_vec(sub_vec(vmult_vec(d, t), p))

	return normal, t, (inner >= 0 and t >= 0)


def ray_hit_sphere(ray, sphere, hit_info, valid_sreg):
	instructions = []
	
	(ray_pos_vreg, ray_dir_vreg) = ray
	(sphere_pos_vreg, sphere_color_vreg, sphere_rad_sreg) = sphere
	(hit_info_color_vreg, hit_info_normal_vreg, hit_info_ray_pos_vreg, hit_info_ray_dir_vreg, hit_info_t_sreg) = hit_info

	p_vreg = "ray_hit_sphere_p"
	dp_sreg = "ray_hit_sphere_dp"
	dd_sreg = "ray_hit_sphere_dd"
	pp_sreg = "ray_hit_sphere_pp"
	inner_sreg = "ray_hit_sphere_inner"
	t_sreg = "ray_hit_sphere_t"
	srad_srad_sreg = "stmp1"
	pp_minus_srad2_sreg = "stmp2"
	dd_times_pp_minus_srad2_sreg = "stmp3"
	dp_dp_sreg = "stmp4"
	sqrt_inner_sreg = "stmp5"
	t_d_vreg = "vtmp1"
	t_d_minus_p_vreg = "vtmp2"

	# p = s.pos - r.pos => p_vreg = sphere_pos_vreg - ray_pos_vreg
	instructions += sub(sphere_pos_vreg, ray_pos_vreg, p_vreg)

	# dp = dot(d, p) => dp_sreg = ray_dir_vreg * p_vreg
	instructions += dot(ray_dir_vreg, p_vreg, dp_sreg)

	# pp = dot(p, p) => pp_sreg = p_vreg * p_vreg
	instructions += dot(p_vreg, p_vreg, pp_sreg)

	# dd = dot(d, d) => dd_sreg = ray_dir_vreg * ray_dir_vreg
	instructions += dot(ray_dir_vreg, ray_dir_vreg, dd_sreg)

	# s.radius * s.radius -> srad_srad_sreg = sphere_rad_sreg * sphere_rad_sreg
	instructions += smult(sphere_rad_sreg, sphere_rad_sreg, srad_srad_sreg)

	# pp - s.radius * s.radius => pp_minus_srad2_sreg = pp_sreg - srad_srad_sreg
	instructions += ssub(pp_sreg, srad_srad_sreg, pp_minus_srad2_sreg)

	# dd * (pp - s.radius * s.radius) => dd_times_pp_minus_srad2_sreg = dd_sreg * pp_minus_srad2_sreg
	instructions += smult(dd_sreg, pp_minus_srad2_sreg, dd_times_pp_minus_srad2_sreg)

	# dp * dp => dp_dp_sreg = dp_sreg * dp_sreg
	instructions += smult(dp_sreg, dp_sreg, dp_dp_sreg)

	# inner = dp * dp - dd * (pp - s.radius * s.radius) => inner_sreg = dp_dp_sreg - dd_times_pp_minus_srad2_sreg
	instructions += ssub(dp_dp_sreg, dd_times_pp_minus_srad2_sreg, inner_sreg)

	# compare inner against 0 => valid_sreg = inner_sreg >= ZERO_REG
	instructions += gte(inner_sreg, ZERO_REG, valid_sreg)

	# sqrt(inner) => sqrt_inner_sreg = sqrt(inner_sreg) if valid_sreg
	instructions += sqrt(inner_sreg, sqrt_inner_sreg, valid_sreg)

	# t = dp - sqrt(inner) => t_sreg = dp_sreg - sqrt_inner_sreg if valid_sreg
	instructions += ssub(dp_sreg, sqrt_inner_sreg, t_sreg, valid_sreg)

	# compare t against 0 => valid_sreg = t_sreg >= ZERO_REG if valid_sreg
	instructions += gte(t_sreg, ZERO_REG, valid_sreg, valid_sreg)

	# t * d => t_d_vreg = ray_dir_vreg * t_sreg if valid_sreg
	instructions += vmult(ray_dir_vreg, t_sreg, t_d_vreg, valid_sreg)

	# t * d - p => t_d_minus_p_vreg = t_d_vreg - p_vreg if valid_sreg
	instructions += sub(t_d_vreg, p_vreg, t_d_minus_p_vreg, valid_sreg)

	# normal = normalize(t * d - p) => hit_info_normal_vreg = normalize(t_d_minus_p_vreg) if valid_sreg
	instructions += norm(t_d_minus_p_vreg, hit_info_normal_vreg, valid_sreg)

	# info.color = s.color if valid_sreg => hit_info_color_vreg = sphere_color_vreg + 0 if valid_sreg
	instructions += add(sphere_color_vreg, ZERO_REG, hit_info_color_vreg, valid_sreg)

	# info.ray.pos = ray.pos if valid_sreg => hit_info_ray_pos_vreg = ray_pos_vreg + 0 if valid_sreg
	instructions += add(ray_pos_vreg, ZERO_REG, hit_info_ray_pos_vreg, valid_sreg)

	# info.ray.dir = ray.dir if valid_sreg => hit_info_ray_pos_vreg = ray_pos_vreg + 0 if valid_sreg
	instructions += add(ray_dir_vreg, ZERO_REG, hit_info_ray_dir_vreg, valid_sreg)

	# info.t = t if valid_sreg => hit_info_ray_pos_vreg = t_sreg + 0 if valid_sreg
	instructions += sadd(t_sreg, ZERO_REG, hit_info_t_sreg, valid_sreg)

	return instructions

def shoot_ray(ray, hit_info, spheres, hit_info_updated):
	instructions = []

	(hit_info_color_vreg, hit_info_normal_vreg, hit_info_ray_pos_vreg, hit_info_ray_dir_vreg, hit_info_t_sreg) = hit_info
	
	# bool updated = false => hit_info_updated = ZERO_REG + ZERO_REG
	instructions += sadd(ZERO_REG, ZERO_REG, hit_info_updated)

	for sphere in spheres:
		valid_hit_sreg = "ray_hit_sphere_valid"
		(sphere_info_color_vreg, sphere_info_normal_vreg, sphere_info_ray_pos_vreg, sphere_info_ray_dir_vreg, sphere_info_t_sreg) = SPHERE_INFO
		instructions += ray_hit_sphere(ray, sphere, SPHERE_INFO, valid_hit_sreg)

		# compare sphere_info.t against info.t using the same valid bit => valid_hit_sreg = sphere_info_t_sreg < hit_info_t_sreg
		instructions += less(sphere_info_t_sreg, hit_info_t_sreg, valid_hit_sreg, valid_hit_sreg)

		# if we want to save this value, valid_hit_sreg will be high
		# sphere info for movement:
		#	- sphere_info.color = sphere_info_color_vreg => hit_info_color_vreg
		#	- sphere_info.normal = sphere_info_normal_vreg => hit_info_normal_vreg
		#	- sphere_info.ray.origin = sphere_info_ray_pos_vreg => hit_info_ray_pos_vreg
		#	- sphere_info.ray.dir = sphere_info_ray_dir_vreg => hit_info_ray_dir_vreg
		#	- sphere_info.t = sphere_info_t_sreg => hit_info_t_sreg
		instructions += add(sphere_info_color_vreg, ZERO_REG, hit_info_color_vreg, valid_hit_sreg)
		instructions += add(sphere_info_normal_vreg, ZERO_REG, hit_info_normal_vreg, valid_hit_sreg)
		instructions += add(sphere_info_ray_pos_vreg, ZERO_REG, hit_info_ray_pos_vreg, valid_hit_sreg)
		instructions += add(sphere_info_ray_dir_vreg, ZERO_REG, hit_info_ray_dir_vreg, valid_hit_sreg)
		instructions += sadd(sphere_info_t_sreg, ZERO_REG, hit_info_t_sreg, valid_hit_sreg)

		# Set the updated register high if these changes were made
		instructions += addi(1, ZERO_REG, hit_info_updated, valid_hit_sreg)

	return instructions

def trace_ray(ray, time):
	instructions = []

	light_pos_vec = (0, 2, -5)

	shoot_ray_updated_sreg = "shoot_ray_updated"
	inf_sreg = "stmp6"
	ray_hit_sreg = "stmp7"
	ray_nohit_sreg = "stmp8"
	light_pos_vreg = "light_pos"
	hit_pos_vreg = "hit_pos"
	light_dir_vreg = "light_dir"
	trace_ray_color_vreg = "trace_ray_return"

	r_dir_time_closest_info_t_vreg = "vtmp1"
	light_minus_hit_pos_vreg = "vtmp2"
	norm_light_dir_dot_sreg = "stmp9"
	color_compare = "stmp10"
	color_scalar = "stmp11"

	(ray_pos_vreg, ray_dir_vreg) = ray
	light_ray = (hit_pos_vreg, light_dir_vreg)
	(closest_info_color_vreg, closest_info_normal_vreg, closest_info_ray_pos_vreg, closest_info_ray_dir_vreg, closest_info_t_sreg) = CLOSEST_INFO
	(light_info_color_vreg, light_info_normal_vreg, light_info_ray_pos_vreg, light_info_ray_dir_vreg, light_info_t_sreg) = LIGHT_INFO

	# closest_info.t = INF => load_inf(inf_sreg) then move to closest_info_t_sreg and light_info_t_sreg
	instructions += addi(100, ZERO_REG, inf_sreg)
	instructions += sadd(inf_sreg, ZERO_REG, closest_info_t_sreg)
	instructions += sadd(inf_sreg, ZERO_REG, light_info_t_sreg)

	# shoot_ray(r, closest_info)
	instructions += shoot_ray(ray, CLOSEST_INFO, [SPHERE, SPHERE2], shoot_ray_updated_sreg)

	# only continue if closest_info.t < INF
	instructions += less(closest_info_t_sreg, inf_sreg, ray_hit_sreg) # store the hit
	instructions += gte(closest_info_t_sreg, inf_sreg, ray_nohit_sreg) # store the no hit

	# Return the background color if there was no hit
	instructions += load_vector(BACKGROUND_COLOR, trace_ray_color_vreg, ray_nohit_sreg)

	# load light_pos if ray_hit_sreg
	instructions += load_vector(light_pos_vec, light_pos_vreg, ray_hit_sreg)

	# r.dir * closest_info.t => r_dir_time_closest_info_t_vreg = ray_dir_vreg * closest_info_t_sreg if ray_hit_sreg
	instructions += vmult(ray_dir_vreg, closest_info_t_sreg, r_dir_time_closest_info_t_vreg, ray_hit_sreg)

	# hit_pos = r.pos + r.dir*closest_info.t => hit_pos_vreg = ray_pos_vreg + r_dir_time_closest_info_t_vreg if ray_hit_sreg
	instructions += add(ray_pos_vreg, r_dir_time_closest_info_t_vreg, hit_pos_vreg, ray_hit_sreg)

	# light_pos - hit_pos => light_minus_hit_pos_vreg = light_pos_vreg - hit_pos_vreg if ray_hit_sreg
	instructions += sub(light_pos_vreg,  hit_pos_vreg, light_minus_hit_pos_vreg, ray_hit_sreg)

	# light_dir = norm(light_pos - hit_pos) => light_dir_vreg = norm(light_minus_hit_pos_vreg) if ray_hit_sreg
	instructions += norm(light_minus_hit_pos_vreg, light_dir_vreg, ray_hit_sreg)

	# dot(closest_info.normal, light_dir) => norm_light_dir_dot_vreg = closest_info_normal_vreg * light_dir_vreg if ray_hit_sreg
	instructions += dot(closest_info_normal_vreg, light_dir_vreg, norm_light_dir_dot_sreg, ray_hit_sreg)

	# initialize color_scalar at 0
	instructions += sadd(ZERO_REG, ZERO_REG, color_scalar, ray_hit_sreg)

	# color_compare = norm_light_dir_dot_sreg >= ZERO_REG
	instructions += gte(norm_light_dir_dot_sreg, ZERO_REG, color_compare, ray_hit_sreg)

	# color_scalar is determined by color_compare; it will stay at zero unless color_compare is high
	instructions += sadd(norm_light_dir_dot_sreg, ZERO_REG, color_scalar, color_compare)

	# closest_info.color = closest_info.color * color_scalar
	instructions += vmult(closest_info_color_vreg, color_scalar, closest_info_color_vreg, ray_hit_sreg)

	# # shoot_ray(light_ray, light_info)
	# instructions += shoot_ray(light_ray, LIGHT_INFO, [SPHERE], shoot_ray_updated_sreg)

	# # if shoot_ray_updated_sreg and ray_hit_sreg (from before), the output should be black
	# # start by adding this, then see if we should override
	# instructions += load_vector(UNSEEN_COLOR, trace_ray_color_vreg, ray_hit_sreg)

	# instructions += gte(ZERO_REG, shoot_ray_updated_sreg, ray_hit_sreg, ray_hit_sreg) # in this case, there was a real hit, return the closest color

	# if hit is still high, we return the color
	instructions += add(closest_info_color_vreg, trace_ray_color_vreg, trace_ray_color_vreg, ray_hit_sreg)

	return instructions

def main_image():
	instructions = []

	out_color_vreg = "out_color"
	frag_coor_vreg = "frag_coor"
	uv_vreg = "uv"
	one_by_one_vreg = "vtmp1"
	negative_1z_vreg = "vtmp2"
	vect_to_norm_vreg = "vtmp3"
	divisor = "stmp1"
	trace_ray_color_vreg = "trace_ray_return"

	sphere = ((1, -1, -5), BLUE_COLOR, 2)
	sphere2 = ((-3, 4, -7), RED_COLOR, 1)

	(input_ray_pos_vreg, input_ray_dir_vreg) = INPUT_RAY

	# initial color of black
	instructions += load_vector(UNSEEN_COLOR, out_color_vreg)

	# load the sphere
	instructions += load_sphere(sphere, SPHERE)
	instructions += load_sphere(sphere2, SPHERE2)

	# move the resolution divsor into a register
	instructions += load_scalar(PIXEL_ROWS, divisor)

	# create uv and add it to itself (2uv, so then can shift by 1)
	instructions += vdiv(frag_coor_vreg, divisor, uv_vreg)
	instructions += add(uv_vreg, uv_vreg, uv_vreg)
	instructions += load_vector((1, 1, 0), one_by_one_vreg)
	instructions += sub(uv_vreg, one_by_one_vreg, uv_vreg)

	# create (uv.x, uv.y, -1)
	instructions += load_vector((0, 0, -1), negative_1z_vreg)
	instructions += add(uv_vreg, negative_1z_vreg, vect_to_norm_vreg)
	instructions += norm(vect_to_norm_vreg, input_ray_dir_vreg)

	# initialize input_ray_pos_vreg at 0
	instructions += load_vector((0, 0, 0), input_ray_pos_vreg)

	# trace the ray now
	instructions += trace_ray(INPUT_RAY, 0)

	# move the trace_ray_color_vreg into out_color_vreg
	instructions += add(trace_ray_color_vreg, ZERO_REG, out_color_vreg)

	return instructions

def test_ray_hit_sphere():
	instructions = []

	ray_regs = INPUT_RAY
	sphere_regs = SPHERE
	hit_info_regs = SPHERE_INFO
	valid_sreg = "ray_hit_sphere_valid"

	ray_origin = (0, 0, 0)
	ray_direction = (4, 4, 2)
	ray = (ray_origin, ray_direction)
	ray_direction_normal = norm_vec(ray_direction)

	sphere_pos = (10, 9, 10)
	sphere_color = (1, 1, 1)
	sphere_rad = 6
	sphere = (sphere_pos, sphere_color, sphere_rad)

	normal_vec, t, inner_valid = expected_ray_hit_sphere(ray_origin, ray_direction_normal, sphere_pos, sphere_rad)

	print("Expected: normal_vec = " + str(normal_vec) + "; t = " + str(t) + "; inner valid: " + str(inner_valid))

	
	instructions += load_ray(ray, ray_regs)
	instructions += load_sphere(sphere, sphere_regs)
	instructions += ray_hit_sphere(ray_regs, sphere_regs, hit_info_regs, valid_sreg)

	return instructions

if __name__ == '__main__':
	instructions = main_image()

	print(decode_instruction("0,0,0,0,0,1,0,1,0,1,0,1,0,0,0,0,0,0,0,1,1,1,1,0,0,0,0,0,1,0,1,1"))

	with open('unit_tests/shader.csv', "w+") as output_file:
		output_file.write("\n".join(instructions))
