import spatial.dsl._

@spatial class RegisterOperations extends SpatialTest {
  type RegType = FixPt[TRUE, _24, _8]
  type InstructionFixed = FixPt[FALSE, _24, _0]
  type InstBit = FixPt[FALSE, _8, _0]
  type SubType = FixPt[TRUE, _23, _8]

  @struct class Vector3(
    x: RegType,
    y: RegType,
    z: RegType
  )

  // Number of instructions in the file (need a way for this to be dynamic)
  val num_instructions = 3
  val num_vec_elements = 3
  val num_bits = 24
  val pixel_rows = 1
  val pixel_columns = 1
  val registers = 16
  val num_operations = 17

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[InstBit](s"$DATA/register_operations.csv")
    val inst_dram = DRAM[InstBit](num_instructions, num_bits)

    setMem(inst_dram, inst_host)

    val out = DRAM[RegType](num_instructions, num_vec_elements + 1)

    Accel {
      // Create the registers for each pixel
      val vec_regs = SRAM[Vector3](registers)
      val sca_regs = SRAM[RegType](registers)

      Foreach (0 until registers) { i =>
        vec_regs(i) = Vector3(0, 0, 0)
        sca_regs(i) = 0
      }

      val inst_sram = SRAM[InstBit](num_instructions, num_bits)
      inst_sram load inst_dram

      val internal_out = SRAM[RegType](num_instructions, num_vec_elements + 1)

      val vec_operations = SRAM[Vector3](num_operations)
      val sca_operations = SRAM[RegType](num_operations)

      Foreach (0 until num_instructions) { i =>
        // Decoding the instruction and storing it as an instruction (for clarity)
        val op = inst_sram(i, 5) * 16 + inst_sram(i, 6) * 8 + inst_sram(i, 7) * 4 + inst_sram(i, 8) * 2 + inst_sram(i, 9)
        val src1 = inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)
        val src2 = inst_sram(i, 16) * 8 + inst_sram(i, 17) * 4 + inst_sram(i, 18) * 2 + inst_sram(i, 19)
        val dest = inst_sram(i, 20) * 8 + inst_sram(i, 21) * 4 + inst_sram(i, 22) * 2 + inst_sram(i, 23)
        val immediate = inst_sram(i, 10) * 32 + inst_sram(i, 11) * 16 + inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)

        val vec_reg_src1 = vec_regs(src1.to[Int])
        val vec_reg_src2 = vec_regs(src2.to[Int])
        val sca_reg_src1 = sca_regs(src1.to[Int])
        val sca_reg_src2 = sca_regs(src2.to[Int])
        val immediate_regtype = immediate.to[RegType]

        val add_vectors = Vector3(vec_reg_src1.x + vec_reg_src2.x, vec_reg_src1.y + vec_reg_src2.y, vec_reg_src1.z + vec_reg_src2.z)

        val sub_vectors = Vector3((vec_reg_src1.x.to[SubType] - vec_reg_src2.x.to[SubType]).to[RegType], (vec_reg_src1.y.to[SubType] - vec_reg_src2.y.to[SubType]).to[RegType], (vec_reg_src1.z.to[SubType] - vec_reg_src2.z.to[SubType]).to[RegType])

        val src2_mag2 = vec_reg_src2.x * vec_reg_src2.x + vec_reg_src2.y * vec_reg_src2.y + vec_reg_src2.z * vec_reg_src2.z

        // Taylor Approximation of Square Root: https://math.libretexts.org/Bookshelves/Analysis/Supplemental_Modules_(Analysis)/Series_and_Expansions/Taylor_Expansion_II
        val src2_mag = sqrt(src2_mag2) //1 + (src2_mag2 - 1) / 2 - ((src2_mag2 - 1) * (src2_mag2 - 1)) / 8 + ((src2_mag2 - 1) * (src2_mag2 - 1) * (src2_mag2 - 1)) / 16

        val normalize_vector = Vector3((vec_reg_src2.x.to[SubType] / (src2_mag.to[SubType])).to[RegType], (vec_reg_src2.y.to[SubType] / src2_mag.to[SubType]).to[RegType], (vec_reg_src2.z.to[SubType] / src2_mag.to[SubType]).to[RegType])

        val dot_product = (vec_reg_src1.x.to[SubType] * vec_reg_src2.x.to[SubType] + vec_reg_src1.y.to[SubType] * vec_reg_src2.y.to[SubType] + vec_reg_src1.z.to[SubType] * vec_reg_src2.z.to[SubType]).to[RegType]

        val mult_vscalar = Vector3(vec_reg_src1.x * sca_reg_src2, vec_reg_src1.y * sca_reg_src2, vec_reg_src1.z * sca_reg_src2) 
        // val div_vscalar = Vector3(vec_reg_src1.x / sca_reg_src2, vec_reg_src1.y / sca_reg_src2, vec_reg_src1.z / sca_reg_src2)

        // var sq_scalar = sca_reg_src2 * sca_reg_src2
        // val sqrt_scalar =  1 + (sq_scalar - 1) / 2 - ((sq_scalar - 1) * (sq_scalar - 1)) / 8 + ((sq_scalar - 1) * (sq_scalar - 1) * (sq_scalar - 1)) / 16

        val add_scalar = (sca_reg_src1.to[SubType] + sca_reg_src2.to[SubType]).to[RegType]
        val sub_scalar = (sca_reg_src1.to[SubType] - sca_reg_src2.to[SubType]).to[RegType]
        // val mult_scalar = sca_reg_src1 * sca_reg_src2
        // val div_scalar = sca_reg_src1 / sca_reg_src2

        val addi_scalar = (sca_reg_src2.to[SubType] + immediate_regtype.to[SubType]).to[RegType]

        val addi_vector_x = Vector3(immediate_regtype + vec_reg_src2.x, vec_reg_src2.y, vec_reg_src2.z)
        val addi_vector_y = Vector3(vec_reg_src2.x, immediate_regtype + vec_reg_src2.y, vec_reg_src2.z)
        val addi_vector_z = Vector3(vec_reg_src2.x, vec_reg_src2.y, immediate_regtype + vec_reg_src2.z)

        vec_operations(0) = add_vectors
        vec_operations(1) = sub_vectors
        vec_operations(2) = normalize_vector
        vec_operations(3) = vec_regs(dest.to[Int])
        vec_operations(4) = vec_regs(dest.to[Int])
        vec_operations(5) = vec_regs(dest.to[Int])
        vec_operations(6) = mult_vscalar
        vec_operations(7) = vec_regs(dest.to[Int]) // div_vscalar
        vec_operations(8) = vec_regs(dest.to[Int])
        vec_operations(9) = vec_regs(dest.to[Int])
        vec_operations(10) = vec_regs(dest.to[Int])
        vec_operations(11) = vec_regs(dest.to[Int])
        vec_operations(12) = vec_regs(dest.to[Int])
        vec_operations(13) = vec_regs(dest.to[Int])
        vec_operations(14) = addi_vector_x
        vec_operations(15) = addi_vector_y
        vec_operations(16) = addi_vector_z

        sca_operations(0) = sca_regs(dest.to[Int])
        sca_operations(1) = sca_regs(dest.to[Int])
        sca_operations(2) = sca_regs(dest.to[Int])
        sca_operations(3) = src2_mag
        sca_operations(4) = src2_mag2
        sca_operations(5) = dot_product
        sca_operations(6) = sca_regs(dest.to[Int])
        sca_operations(7) = sca_regs(dest.to[Int])
        sca_operations(8) = sca_regs(dest.to[Int]) // sqrt_scalar
        sca_operations(9) = add_scalar
        sca_operations(10) = sub_scalar
        sca_operations(11) = sca_regs(dest.to[Int]) // mult_scalar
        sca_operations(12) = sca_regs(dest.to[Int]) // div_scalar
        sca_operations(13) = addi_scalar
        sca_operations(14) = sca_regs(dest.to[Int])
        sca_operations(15) = sca_regs(dest.to[Int])
        sca_operations(16) = sca_regs(dest.to[Int])

        vec_regs(dest.to[Int]) = vec_operations(op.to[Int])
        sca_regs(dest.to[Int]) = sca_operations(op.to[Int])

        internal_out(i, 0) = vec_regs(1).x
        internal_out(i, 1) = vec_regs(1).y
        internal_out(i, 2) = vec_regs(1).z
        internal_out(i, 3) = sca_regs(1)
      }

      out store internal_out
    }

    val result = getMem(out)

    // temporary for basic asm tests
    printArray(result)
    assert(1 == 1)
  }
}