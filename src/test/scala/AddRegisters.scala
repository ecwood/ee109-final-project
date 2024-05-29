import spatial.dsl._

@spatial class AddRegisters extends SpatialTest {
  type RegType = FixPt[FALSE, _16, _0]
  type InstructionFixed = FixPt[FALSE, _24, _0]
  type InstBit = FixPt[FALSE, _8, _0]

  type OneBitType = FixPt[FALSE, _1, _0]
  type TwoBitsType = FixPt[FALSE, _2, _0]
  type RegLabelType = FixPt[FALSE, _4, _0]

  @struct class Instruction(
    nothing: FixPt[FALSE, _7, _0],
    a: OneBitType,
    gg: TwoBitsType,
    oo: TwoBitsType,
    src1: RegLabelType,
    src2: RegLabelType,
    dest: RegLabelType
  )

  @struct class Vector3(
    elem1: RegType,
    elem2: RegType,
    elem3: RegType
  )

  // Number of instructions in the file (need a way for this to be dynamic)
  val num_instructions = 4
  val num_vec_elements = 3
  val num_bits = 24
  val pixel_rows = 1
  val pixel_columns = 1
  val registers = 16
  val num_operations = 4

  def main(args: Array[String]): Unit = {
    val inst_host = loadCSV1D[InstBit](s"$DATA/add_registers.csv")
    val inst_dram = DRAM[InstBit](num_instructions, num_bits)

    setMem(inst_dram, inst_host)

    val out = DRAM[Int](num_instructions, num_vec_elements)

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

      val internal_out = SRAM[Int](num_instructions, num_vec_elements)

      val operations = SRAM[Vector3](num_operations)

      Foreach (0 until num_instructions) { i =>
        // Decoding the instruction and storing it as an instruction (for clarity)
        val a = inst_sram(i, 7)
        val gg = inst_sram(i, 8) * 2 + inst_sram(i, 9)
        val oo = inst_sram(i, 10) * 2 + inst_sram(i, 11)
        val src1 = inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)
        val src2 = inst_sram(i, 16) * 8 + inst_sram(i, 17) * 4 + inst_sram(i, 18) * 2 + inst_sram(i, 19)
        val dest = inst_sram(i, 20) * 8 + inst_sram(i, 21) * 4 + inst_sram(i, 22) * 2 + inst_sram(i, 23)
        val immediate = inst_sram(i, 10) * 32 + inst_sram(i, 11) * 16 + inst_sram(i, 12) * 8 + inst_sram(i, 13) * 4 + inst_sram(i, 14) * 2 + inst_sram(i, 15)

        val vec_reg_src1 = vec_regs(src1.to[Int])
        val vec_reg_src2 = vec_regs(src2.to[Int])
        val vec_reg_src1_elem1 = vec_reg_src1.elem1
        val vec_reg_src2_elem1 = vec_reg_src2.elem1
        val vec_reg_src1_elem2 = vec_reg_src1.elem2
        val vec_reg_src2_elem2 = vec_reg_src2.elem2
        val vec_reg_src1_elem3 = vec_reg_src1.elem3
        val vec_reg_src2_elem3 = vec_reg_src2.elem3

        val add_elem1 = vec_reg_src1_elem1 + vec_reg_src2_elem1
        val add_elem2 = vec_reg_src1_elem2 + vec_reg_src2_elem2
        val add_elem3 = vec_reg_src1_elem3 + vec_reg_src2_elem3
        val add_vectors = Vector3(add_elem1, add_elem2, add_elem3)

        val addi_elem1 = immediate + vec_reg_src2_elem1
        val addi_elem2 = immediate + vec_reg_src2_elem2
        val addi_elem3 = immediate + vec_reg_src2_elem3
        val addi_vector_x = Vector3(addi_elem1.to[RegType], vec_reg_src2_elem2, vec_reg_src2_elem3)
        val addi_vector_y = Vector3(vec_reg_src2_elem1, addi_elem2.to[RegType], vec_reg_src2_elem3)
        val addi_vector_z = Vector3(vec_reg_src2_elem1, vec_reg_src2_elem2, addi_elem3.to[RegType])

        operations(0) = add_vectors
        operations(1) = addi_vector_x
        operations(2) = addi_vector_y
        operations(3) = addi_vector_z

        vec_regs(dest.to[Int]) = operations(gg.to[Int])

        internal_out(i, 0) = vec_regs(1).elem1.to[Int]
        internal_out(i, 1) = vec_regs(1).elem2.to[Int]
        internal_out(i, 2) = vec_regs(1).elem3.to[Int]
      }

      out store internal_out
    }

    val result = getMem(out)

    // temporary for basic asm tests
    printArray(result)
    assert(0 == 0)
  }
}